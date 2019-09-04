package com.macwatters.elements.gradle.launch;

import com.macwatters.elements.gradle.ClosureUtils;
import groovy.lang.Closure;
import org.gradle.api.file.FileCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ElementsLaunchExtension {

    private FileCollection classPath;

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

    public void dev(Closure devFilter) {
        ClosureUtils.closureBackedAction(devFilter).execute(dev);
    }

    public void dist(Closure distFilter) {
        ClosureUtils.closureBackedAction(distFilter).execute(dist);
    }

    public FileCollection getClassPath() {
        return classPath;
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
