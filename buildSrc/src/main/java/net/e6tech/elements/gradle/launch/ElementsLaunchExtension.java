package net.e6tech.elements.gradle.launch;

import groovy.lang.Closure;
import org.gradle.api.file.FileCollection;
import org.gradle.util.ClosureBackedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ElementsLaunchExtension {

    private FileCollection classPath;

    // For individual launch tasks
    private List<Object> launchArgs = new ArrayList<>();

    private final LaunchScriptFilter dev;
    private final LaunchScriptFilter dist;

    private List<Object> jvmArgs = new ArrayList<>();

    private final Consumer<ElementsLaunchExtension> updated;

    public ElementsLaunchExtension(Consumer<ElementsLaunchExtension> updated) {
        this.updated = updated;
        dev = new LaunchScriptFilter(updated, this);
        dist = new LaunchScriptFilter(updated, this);
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

    public void dev(Closure devFilter) {
        ClosureBackedAction.of(devFilter).execute(dev);
    }

    public void dist(Closure distFilter) {
        ClosureBackedAction.of(distFilter).execute(dist);
    }

    public FileCollection getClassPath() {
        return classPath;
    }

    public List<Object> getLaunchArgs() {
        return launchArgs;
    }

    public List<Object> getJvmArgs() {
        return jvmArgs;
    }

    public LaunchScriptFilter getDev() {
        return dev;
    }

    public LaunchScriptFilter getDist() {
        return dist;
    }
}
