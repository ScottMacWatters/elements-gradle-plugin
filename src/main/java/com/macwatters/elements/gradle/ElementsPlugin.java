package com.macwatters.elements.gradle;

import com.macwatters.elements.gradle.copy.CopyTaskManager;
import com.macwatters.elements.gradle.launch.LaunchTasksManager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class ElementsPlugin implements Plugin<Project> {


    private final AddedListener<Task> taskListener = new AddedListener<>(Task::getName);
    private final AddedListener<Plugin> pluginListener = new AddedListener<>(plugin -> "");
    private final LaunchTasksManager launchTasksManager = new LaunchTasksManager(this);
    private final CopyTaskManager copyTaskManager = new CopyTaskManager(this);

    @Override
    public void apply(Project project) {
        project.getTasks().whenObjectAdded(taskListener);
        project.getPlugins().whenObjectAdded(pluginListener);
        copyTaskManager.applyCopyExtension(project);
        launchTasksManager.applyLaunchExtension(project);
    }

    public AddedListener<Task> getTaskListener() {
        return taskListener;
    }

    public AddedListener<Plugin> getPluginListener() {
        return pluginListener;
    }
}
