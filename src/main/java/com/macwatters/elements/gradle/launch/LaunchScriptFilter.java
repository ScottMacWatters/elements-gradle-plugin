package com.macwatters.elements.gradle.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class LaunchScriptFilter {

    private static final List<Object> DEFAULT_EXCLUDE_REGEX = Collections.singletonList(".*/etc/.*");
    private static final List<Object> DEFAULT_INCLUDE_REGEX = Collections.singletonList(".*/(dev|prod)/.*");

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
    private List<Object> includeRegex = DEFAULT_INCLUDE_REGEX;
    private List<Object> excludeRegex = DEFAULT_EXCLUDE_REGEX;
    private Object launchScriptBase = "conf/provisioning";
    private Object prefix = "run";

    public void includeRegex(Object... includeRegex) {
        if (this.includeRegex == DEFAULT_INCLUDE_REGEX) this.includeRegex = new ArrayList<>();
        this.includeRegex.addAll(Arrays.asList(includeRegex));
        onUpdate();
    }

    public void excludeRegex(Object... excludeRegex) {
        if (this.excludeRegex == DEFAULT_EXCLUDE_REGEX) this.excludeRegex = new ArrayList<>();
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
