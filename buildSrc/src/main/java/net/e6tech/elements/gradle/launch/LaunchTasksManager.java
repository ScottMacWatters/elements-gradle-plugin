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


    private static class LaunchTask {
        protected final LaunchElementsTask launchElements;
        protected final CreateElementsStartScriptTask startScripts;

        private LaunchTask(LaunchElementsTask launchElements, CreateElementsStartScriptTask startScripts) {
            this.launchElements = launchElements;
            this.startScripts = startScripts;
        }
    }

    public void applyLaunchExtension(Project project) {
        CreateStartScripts startScripts = (CreateStartScripts) project.getTasks().getByName("startScripts");
        startScripts.setEnabled(false);


        Map<String, LaunchTask> tasks = new HashMap<>();
        project.getExtensions().add("elementsLaunch", new ElementsLaunchExtension(ext ->
                manageLaunchTasks(tasks, project, ext)));

        manageLaunchTasks(tasks, project, project.getExtensions().getByType(ElementsLaunchExtension.class));
    }

    private void manageLaunchTasks(Map<String, LaunchTask> existing, Project project, ElementsLaunchExtension extension) {

        Set<String> shouldExist = getLaunchScripts(project, extension);

        shouldExist.forEach(name -> existing.computeIfAbsent(name, computeLaunchTask(project)));

        List<String> jvmArgs = extension.getJvmArgs().stream().map(ClosureUtils::s)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        existing.forEach((name, task) -> {
            task.launchElements.setClasspath(extension.getClassPath());
            task.launchElements.setGroup("launch elements");
            task.launchElements.setAdditionalArgs(extension.getLaunchArgs());
            task.launchElements.setHomeDir(project.getProjectDir());
            task.launchElements.setLaunchScriptBase(extension.getLaunchScriptBase());
            task.launchElements.setJvmArgs(jvmArgs);

            task.startScripts.setAdditionalArgs(extension.getLaunchArgs());
            task.startScripts.populateMainClassname();
            task.startScripts.setOutputDir(new File(project.getBuildDir(), "scripts"));
            task.startScripts.setLaunchScriptBase(extension.getLaunchScriptBase());
            task.startScripts.setDefaultJvmOpts(jvmArgs);

            task.startScripts.setClasspath(project.files("$APP_HOME/*"));
        });


        Task processResources = project.getTasks().getByName("processResources");
        existing.forEach((name, task) -> {
            if (shouldExist.contains(name)) {
                project.getTasks().add(task.launchElements);
                project.getTasks().add(task.startScripts);
                processResources.dependsOn(task.startScripts);
                task.startScripts.setEnabled(true);
            } else {
                project.getTasks().remove(task.launchElements);
                project.getTasks().remove(task.startScripts);
                task.startScripts.setEnabled(false);
            }
        });

    }

    private Function<String, LaunchTask> computeLaunchTask(Project project) {
        return name -> {
            String launchName = derriveTaskName("run", name);
            String scriptName = derriveTaskName("start", name);
            LaunchElementsTask launchTask = project.getTasks().create(launchName, LaunchElementsTask.class);
            launchTask.setLaunchScript(name);
            CreateElementsStartScriptTask startScripts = project.getTasks().create(scriptName, CreateElementsStartScriptTask.class);
            startScripts.setApplicationName(launchName);
            startScripts.setLaunchScript(name);
            return new LaunchTask(launchTask, startScripts);
        };
    }


    private String derriveTaskName(String prefix, String launchScriptName) {
        StringBuilder builder = new StringBuilder(prefix);


        boolean capitalizeNext = true;
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


    private Set<String> getLaunchScripts(Project project, ElementsLaunchExtension extension) {
        String provisionDir = project.getProjectDir().getAbsolutePath() + "/" + s(extension.getLaunchScriptBase()).orElse("") + "/";
        Path provisionDirPath = Paths.get(provisionDir);

        if (!Files.exists(provisionDirPath)) return Collections.emptySet();

        Set<String> output = new HashSet<>();

        List<String> include = convertClosureList(extension.getIncludeLaunchRegex());

        List<String> exclude = convertClosureList(extension.getExcludeLaunchRegex());

        recursiveScanSubDirs(provisionDirPath, filePath -> {
            String fullPath = filePath.toString();
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
