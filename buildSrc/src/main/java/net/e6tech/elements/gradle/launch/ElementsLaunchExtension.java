package net.e6tech.elements.gradle.launch;

import org.gradle.api.file.FileCollection;

import java.util.*;
import java.util.function.Consumer;

public class ElementsLaunchExtension {

    private FileCollection classPath;

    // For individual launch tasks
    private List<Object> launchArgs = new ArrayList<>();
    private Object launchScriptBase = "conf/provisioning";

    // To include/exclude directories or scripts from being generated as new tasks
    private List<Object> includeLaunchRegex = new ArrayList<>();
    private List<Object> excludeLaunchRegex = new ArrayList<>();

    private List<Object> jvmArgs = new ArrayList<>();

    private final Consumer<ElementsLaunchExtension> updated;

    public ElementsLaunchExtension(Consumer<ElementsLaunchExtension> updated) {
        this.updated = updated;
    }

    private void onUpdate() {
        updated.accept(this);
    }

    public void jvmArgs(Object... jvmArgs) {
        this.jvmArgs.addAll(Arrays.asList(jvmArgs));
        onUpdate();
    }

    public void classPath(FileCollection classPath) {
        this.classPath = classPath;
        onUpdate();
    }

    public void launchArgs(Object... launchArgs) {
        this.launchArgs.addAll(Arrays.asList(launchArgs));
        onUpdate();
    }

    public void launchScriptBase(Object launchScriptBase) {
        this.launchScriptBase = launchScriptBase;
        onUpdate();
    }

    public void includeLaunchRegex(Object... includeLaunchRegex) {
        this.includeLaunchRegex.addAll(Arrays.asList(includeLaunchRegex));
        onUpdate();
    }

    public void excludeLaunchRegex(Object... excludeLaunchRegex) {
        this.excludeLaunchRegex.addAll(Arrays.asList(excludeLaunchRegex));
        onUpdate();
    }

    public FileCollection getClassPath() {
        return classPath;
    }

    public List<Object> getLaunchArgs() {
        return launchArgs;
    }

    public Object getLaunchScriptBase() {
        return launchScriptBase;
    }

    public List<Object> getIncludeLaunchRegex() {
        return includeLaunchRegex;
    }

    public List<Object> getExcludeLaunchRegex() {
        return excludeLaunchRegex;
    }

    public List<Object> getJvmArgs() {
        return jvmArgs;
    }
}
