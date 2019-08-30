package com.macwatters.elements.gradle.launch;

import com.macwatters.elements.gradle.ClosureUtils;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
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

import static com.macwatters.elements.gradle.ClosureUtils.s;

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
        List<String> elementsConfiguredArgs = extension.getJvmArgs().stream().map(ClosureUtils::s)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        if (elementsConfiguredArgs.isEmpty())
            elementsConfiguredArgs = applicationPluginJvmArgs().orElse(Collections.emptyList());
        return elementsConfiguredArgs;
    }

    private void manageDevTasks(ElementsLaunchExtension extension) {
        Set<String> shouldExist = getLaunchScripts(extension.getDev());

        String prefix = s(extension.getDev().getPrefix()).orElse("run");

        Map<String, String> tasksByName = shouldExist.stream()
                .collect(Collectors.toMap(name -> derriveTaskName(prefix, name), Function.identity(), (a, b) -> b));


        tasksByName.forEach((name, launchName) -> existingDev.computeIfAbsent(name, n -> computeDev(n, launchName)));

        List<String> jvmArgs = computeJvmArgs(extension);

        FileCollection classPath = extension.getClassPath() == null ? computeJavaSourceSetsClasspath().orElse(null) : null;

        existingDev.forEach((name, task) -> {
            task.setClasspath(classPath);
            task.setGroup("launch elements");
            task.setAdditionalArgs(extension.getDev().getLaunchArgs());
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

    public Optional<FileCollection> computeJavaSourceSetsClasspath() {
        try {
            SourceSetContainer sets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
            for (SourceSet set : sets) {
                if ("main".equals(set.getName())) {
                    return Optional.of(set.getRuntimeClasspath());
                }
            }
        } catch (IllegalStateException | NullPointerException notFound) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Optional<List<String>> applicationPluginJvmArgs() {
        try {
            List<String> output = new ArrayList<>();
            project.getConvention().getPlugin(ApplicationPluginConvention.class).getApplicationDefaultJvmArgs().forEach(output::add);
            return Optional.of(output);
        } catch (IllegalStateException | NullPointerException notFound) {
            return Optional.empty();
        }
    }

    private void manageDistTasks(ElementsLaunchExtension extension) {
        project.getPlugins().withType(ApplicationPlugin.class, app -> {

            Set<String> shouldExist = getLaunchScripts(extension.getDist());

            shouldExist.forEach(name -> existingDist.computeIfAbsent(name, this::computeDist));

            List<String> jvmArgs = computeJvmArgs(extension);

            Task processResources = project.getTasks().getByName("processResources");
            existingDist.forEach((name, task) -> {
                task.setAdditionalArgs(extension.getDist().getLaunchArgs());
                task.setLaunchScriptBase(extension.getDist().getLaunchScriptBase());
                task.populateMainClassname();
                task.setOutputDir(new File(project.getBuildDir(), "scripts"));
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
