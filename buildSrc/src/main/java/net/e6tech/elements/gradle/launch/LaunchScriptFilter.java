package net.e6tech.elements.gradle.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class LaunchScriptFilter {

    private final Consumer<ElementsLaunchExtension> updated;
    private final ElementsLaunchExtension parent;

    public LaunchScriptFilter(Consumer<ElementsLaunchExtension> updated, ElementsLaunchExtension parent) {
        this.updated = updated;
        this.parent = parent;
    }

    protected void onUpdate() {
        updated.accept(parent);
    }

    // To include/exclude directories or scripts from being generated as new tasks
    private List<Object> includeRegex = new ArrayList<>();
    private List<Object> excludeRegex = new ArrayList<>();
    private Object launchScriptBase = "conf/provisioning";
    private Object prefix = "run";

    public void includeRegex(Object... includeRegex) {
        this.includeRegex.addAll(Arrays.asList(includeRegex));
        onUpdate();
    }

    public void excludeRegex(Object... excludeRegex) {
        this.excludeRegex.addAll(Arrays.asList(excludeRegex));
        onUpdate();
    }

    public void launchScriptBase(Object base) {
        this.launchScriptBase = base;
        onUpdate();
    }

    public void prefix(Object prefix) {
        this.prefix = prefix;
        onUpdate();
    }

    public List<Object> getIncludeRegex() {
        return includeRegex;
    }

    public List<Object> getExcludeRegex() {
        return excludeRegex;
    }

    public Object getLaunchScriptBase() {
        return launchScriptBase;
    }

    public Object getPrefix() {
        return prefix;
    }

}
