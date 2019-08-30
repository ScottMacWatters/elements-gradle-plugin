package com.macwatters.elements.gradle.launch;

import org.gradle.api.tasks.JavaExec;

import java.util.ArrayList;
import java.util.List;

import static com.macwatters.elements.gradle.ClosureUtils.s;

public class LaunchElementsTask extends JavaExec {

    private Object launchScriptBase;
    private Object launchScript;
    private Object homeDir;
    private List<Object> additionalArgs = new ArrayList<>();

    @Override
    public void exec() {
        setMain("net.e6tech.elements.common.launch.Launch");

        List<String> args = new ArrayList<>();
        args.add(format("home", homeDir));
        args.add(format("launch", homeDir, "/", launchScriptBase, "/", launchScript ));
        additionalArgs.forEach(arg -> {
            if (arg == null) return;
            args.add(s(arg).orElse(""));
        });
        setArgs(args);

        super.exec();
    }

    public static String format(String key, Object... valuesToConcat) {
        StringBuilder builder = new StringBuilder(key).append("=");
        for (Object val : valuesToConcat) {
            builder.append(s(val).orElse(""));
        }
        return builder.toString();
    }

    public Object getLaunchScriptBase() {
        return launchScriptBase;
    }

    public void setLaunchScriptBase(Object launchScriptBase) {
        this.launchScriptBase = launchScriptBase;
    }

    public Object getLaunchScript() {
        return launchScript;
    }

    public void setLaunchScript(Object launchScript) {
        this.launchScript = launchScript;
    }

    public Object getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(Object homeDir) {
        this.homeDir = homeDir;
    }

    public List<Object> getAdditionalArgs() {
        return additionalArgs;
    }

    public void setAdditionalArgs(List<Object> additionalArgs) {
        this.additionalArgs = additionalArgs;
    }
}
