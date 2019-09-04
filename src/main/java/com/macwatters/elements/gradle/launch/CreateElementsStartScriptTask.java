package com.macwatters.elements.gradle.launch;

import com.macwatters.elements.gradle.ClosureUtils;
import org.gradle.api.tasks.application.CreateStartScripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateElementsStartScriptTask extends CreateStartScripts {

    private static final String NL = " \\\n";

    private Object launchScriptBase;
    private Object launchScript;
    private List<Object> additionalArgs = new ArrayList<>();


    @Override
    public void generate() {
        super.generate();
    }

    public void populateMainClassname() {
        setMainClassName(formatMainClassWithArgs(additionalArgs, launchScriptBase, launchScript));
    }

    private static String formatMainClassWithArgs(List<Object> args, Object launchScriptBase, Object launchScript) {
        StringBuilder builder = new StringBuilder("net.e6tech.elements.common.launch.Launch").append(NL);
        builder.append(LaunchElementsTask.format("home", "\"$APP_HOME\"")).append(NL);

        builder.append(LaunchElementsTask.format("launch", "\"", "$APP_HOME", "/", launchScriptBase, "/", launchScript, "\"")).append(NL);

        args.stream().map(ClosureUtils::s).filter(Optional::isPresent).map(Optional::get).forEach(arg -> builder.append(arg).append(NL));

        return builder.append("end").toString();
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

    public List<Object> getAdditionalArgs() {
        return additionalArgs;
    }

    public void setAdditionalArgs(List<Object> additionalArgs) {
        this.additionalArgs = additionalArgs;
    }
}
