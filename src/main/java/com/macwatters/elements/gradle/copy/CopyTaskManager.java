package com.macwatters.elements.gradle.copy;

import com.macwatters.elements.gradle.ElementsPlugin;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.Copy;

import java.util.function.Supplier;

public class CopyTaskManager {

    private Project project;
    private Copy copyConf;
    private Copy previousAlsoCopy;
    private int additionalCopyCount = 0;
    private ElementsPlugin plugin;

    public CopyTaskManager(ElementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void applyCopyExtension(Project project) {
        installDistCopyConf(project);

    }

    private void installDistCopyConf(Project project) {
        this.project = project;

        // This section of code is only relevant if the user is using the Application plugin
        project.getPlugins().withType(ApplicationPlugin.class, app -> {
            // Copy the conf directory
            copyConfFromBuild("installDist", copySpec -> copySpec.into("conf"));
            copyConfFromBuild("distTar", copyIntoDistDir());
            copyConfFromBuild("distZip", copyIntoDistDir());

            project.getExtensions().add("elementsDist", new ElementsConfCopyExtension(this::setupCopyConf, this::addNewCopyStep));

            setupCopyConf(copy -> {
                copy.from("conf");
                copy.into(project.getBuildDir() + "/conf");
            });
        });

    }

    private Action<CopySpec> copyIntoDistDir() {
        return copySpec -> copySpec.into(stringClosure(this::distDir));
    }

    private String distDir() {
        String tarDir = project.getName();
        project.getVersion();
        if (project.getVersion() != DefaultProject.DEFAULT_VERSION) {
            tarDir += "-" + project.getVersion().toString();
        }
        return tarDir + "/conf";
    }


    private Closure<String> stringClosure(Supplier<String> supplier) {
        return new Closure<String>(project) {
            @Override
            public String call() {
                return supplier.get();
            }
        };
    }

    private void copyConfFromBuild(String syncTaskName, Action<CopySpec> into) {
        AbstractCopyTask task = (AbstractCopyTask) project.getTasks().getByName(syncTaskName);
        task.from(project.getBuildDir() + "/conf", into);
    }

    private void setupCopyConf(Action<Copy> copyConf) {
        if (this.copyConf != null) {
            project.getTasks().remove(this.copyConf);
        }
        Copy copyConfTask = project.getTasks().create("elementsCopyConf", Copy.class, copyConf);
        processResourcesDependency(copyConfTask);
        this.copyConf = copyConfTask;
    }

    private void addNewCopyStep(Action<Copy> alsoCopy) {
        Task previous = this.previousAlsoCopy;
        this.previousAlsoCopy = project.getTasks().create("elementsCopy" + additionalCopyCount++, Copy.class, alsoCopy);
        processResourcesDependency(this.previousAlsoCopy);
        if (previous != null) {
            this.previousAlsoCopy.dependsOn(previous);
        }
    }

    private void processResourcesDependency(Task task) {
        try {
            project.getTasks().getByName("processResources").dependsOn(task);
        } catch (UnknownTaskException e) {
            plugin.getTaskListener().addActionByName("processResources", t -> t.dependsOn(task));
        }
    }


}
