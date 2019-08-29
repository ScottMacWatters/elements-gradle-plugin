package net.e6tech.elements.gradle.copy;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Sync;

public class CopyTaskManager {

    private Project project;
    private Copy copyConf;
    private Copy previousAlsoCopy;
    private int additionalCopyCount = 0;

    public void applyCopyExtension(Project project) {
        installDistCopyConf(project);

    }

    private void installDistCopyConf(Project project) {
        this.project = project;

        // Copy the conf directory
        Sync installDist = (Sync) project.getTasks().getByName("installDist");
        installDist.from(project.getBuildDir() + "/conf", copySpec -> copySpec.into("conf"));

        project.getExtensions().add("elementsDist", new ElementsConfCopyExtension(this::setupCopyConf, this::addNewCopyStep));

        setupCopyConf(copy -> {
            copy.from("conf");
            copy.into(project.getBuildDir() + "/conf");
        });

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
        project.getTasks().getByName("processResources").dependsOn(task);
    }


}
