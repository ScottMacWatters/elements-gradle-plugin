package com.macwatters.elements.gradle;

import com.macwatters.elements.gradle.copy.CopyTaskManager;
import com.macwatters.elements.gradle.launch.LaunchTasksManager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ElementsPlugin implements Plugin<Project> {


    private LaunchTasksManager launchTasksManager = new LaunchTasksManager();
    private CopyTaskManager copyTaskManager = new CopyTaskManager();

    @Override
    public void apply(Project project) {
        copyTaskManager.applyCopyExtension(project);
        launchTasksManager.applyLaunchExtension(project);
    }
}
