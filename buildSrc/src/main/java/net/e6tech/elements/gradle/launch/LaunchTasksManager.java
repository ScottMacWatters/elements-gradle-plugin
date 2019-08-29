package net.e6tech.elements.gradle.launch;

import net.e6tech.elements.gradle.ClosureUtils;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.application.CreateStartScripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.e6tech.elements.gradle.ClosureUtils.s;

public class LaunchTasksManager {

    private Project project;
    private Map<String, LaunchElementsTask> existingDev = new HashMap<>();
    private Map<String, CreateElementsStartScriptTask> existingDist = new HashMap<>();

    public void applyLaunchExtension(Project project) {
        this.project = project;

        CreateStartScripts startScripts = (CreateStartScripts) project.getTasks().getByName("startScripts");
        startScripts.setEnabled(false);

        ElementsLaunchExtension extension = new ElementsLaunchExtension(this::manageLaunchTasks);
        manageLaunchTasks(extension);
        project.getExtensions().add("elementsLaunch", extension);
    }

    private List<String> computeJvmArgs(ElementsLaunchExtension extension) {
        return extension.getJvmArgs().stream().map(ClosureUtils::s)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private void manageDevTasks(ElementsLaunchExtension extension) {
        Set<String> shouldExist = getLaunchScripts(extension.getDev());

        String prefix = s(extension.getDev().getPrefix()).orElse("run");

        Map<String,String> tasksByName = shouldExist.stream()
                .collect(Collectors.toMap(name -> derriveTaskName(prefix, name), Function.identity(), (a,b) -> b));


        tasksByName.forEach((name, launchName) -> existingDev.computeIfAbsent(name, n -> computeDev(n, launchName)));

        List<String> jvmArgs = computeJvmArgs(extension);

        existingDev.forEach((name, task) -> {
            task.setClasspath(extension.getClassPath());
            task.setGroup("launch elements");
            task.setAdditionalArgs(extension.getLaunchArgs());
            task.setHomeDir(project.getProjectDir());
            task.setLaunchScriptBase(extension.getDev().getLaunchScriptBase());
            task.setJvmArgs(jvmArgs);

            if (tasksByName.containsKey(name)) {
                project.getTasks().add(task);
            } else {
                project.getTasks().remove(task);
            }
        });
    }

    private void manageDistTasks(ElementsLaunchExtension extension) {
        Set<String> shouldExist = getLaunchScripts(extension.getDist());

        shouldExist.forEach(name -> existingDist.computeIfAbsent(name, this::computeDist));

        List<String> jvmArgs = computeJvmArgs(extension);

        Task processResources = project.getTasks().getByName("processResources");
        existingDist.forEach((name, task) -> {
            task.setAdditionalArgs(extension.getLaunchArgs());
            task.populateMainClassname();
            task.setOutputDir(new File(project.getBuildDir(), "scripts"));
            task.setLaunchScriptBase(extension.getDist().getLaunchScriptBase());
            task.setDefaultJvmOpts(jvmArgs);
            task.setClasspath(project.files("$APP_HOME/*"));
            String launchName = derriveTaskName(s(extension.getDist().getPrefix()).orElse("run"), name);
            task.setApplicationName(launchName);

            if (shouldExist.contains(name)) {
                project.getTasks().add(task);
                processResources.dependsOn(task);
                task.setEnabled(true);
            } else {
                project.getTasks().remove(task);
                task.setEnabled(false);
            }
        });
    }

    private void manageLaunchTasks(ElementsLaunchExtension extension) {
        manageDevTasks(extension);
        manageDistTasks(extension);
    }

    private LaunchElementsTask computeDev(String taskName, String launchScript) {
        LaunchElementsTask launchTask = project.getTasks().create(taskName, LaunchElementsTask.class);
        launchTask.setLaunchScript(launchScript);
        return launchTask;
    }

    private CreateElementsStartScriptTask computeDist(String name) {
        String scriptName = derriveTaskName("start", name);
        CreateElementsStartScriptTask startScripts = project.getTasks().create(scriptName, CreateElementsStartScriptTask.class);
        startScripts.setLaunchScript(name);
        return startScripts;
    }


    private String derriveTaskName(String prefix, String launchScriptName) {
        StringBuilder builder = new StringBuilder(prefix);


        boolean capitalizeNext = !prefix.isEmpty();
        for (int i = 0; i < launchScriptName.length(); i++) {
            if (launchScriptName.substring(i).equals("groovy")) break;

            char ch = launchScriptName.charAt(i);

            if (!Character.isAlphabetic(ch)) {
                capitalizeNext = true;
                continue;
            }

            if (capitalizeNext) {
                builder.append(Character.toUpperCase(ch));
                capitalizeNext = false;
            } else {
                builder.append(ch);
            }

        }

        return builder.toString();
    }


    private Set<String> getLaunchScripts(LaunchScriptFilter extension) {
        String provisionDir = project.getProjectDir().getAbsolutePath() + "/" + s(extension.getLaunchScriptBase()).orElse("") + "/";
        Path provisionDirPath = Paths.get(provisionDir);

        if (!Files.exists(provisionDirPath)) return Collections.emptySet();

        Set<String> output = new HashSet<>();

        List<String> include = convertClosureList(extension.getIncludeRegex());

        List<String> exclude = convertClosureList(extension.getExcludeRegex());

        recursiveScanSubDirs(provisionDirPath, filePath -> {
            String fullPath = filePath.toString();
            if (!fullPath.endsWith(".groovy")) return;

            String specificPath = fileNameNoExtension(fullPath.substring(provisionDir.length()));

            if (exclude != null && !exclude.isEmpty()) {
                for (String excl : exclude) {
                    if (specificPath.matches(excl)) {
                        return; // Skipping
                    }
                }
            }
            if (include != null && !include.isEmpty()) {
                for (String incl : include) {
                    if (specificPath.matches(incl)) {
                        output.add(specificPath);
                    }
                }
            } else {
                output.add(specificPath);
            }
        });

        return output;
    }


    private List<String> convertClosureList(List<Object> closures) {
        if (closures == null) return null;
        return closures.stream().map(ClosureUtils::s)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void recursiveScanSubDirs(Path pathToScan, Consumer<Path> onFile) {

        try (Stream<Path> contents = Files.list(pathToScan)) {
            contents.forEach(path -> {
                if (path.toFile().isDirectory()) {
                    recursiveScanSubDirs(path, onFile);
                } else {
                    onFile.accept(path);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fileNameNoExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }


}
