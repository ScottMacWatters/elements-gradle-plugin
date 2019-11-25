# elements-gradle-plugin
Elements Gradle Plugin

This plugin provides gradle extensions to support 2 distinct capabilities:

1. Running specific provisions from gradle directly 
2. Distributing an Elements project using the application plugin

No configurations are required, but some are very useful for configuring which provisions are provided when a distribution is built. Here is a full list of all configuration options available
```groovy

elementsDist {
    copyConf {
        // Copy Spec configuration -- This is the default behavior
        from "conf"
        into project.buildDir + "/conf"
    }
    alsoCopy {
        // Copy Spec configuration 
        from ("some/other/dir") {
            exclude "something"
        }
        into project.buildDir + "/conf/specific/dir"
    }
    // Unlimited number of "alsoCopy" configurations are allowed
    // They execute after copyConf sequentially in the order configured
}

elementsLaunch {
    // Configure the class path used when launching gradle tasks
    // This is the default:
    classPath sourceSets.main.runtimeClasspath
    
    // This configures what provisions are turned into Gradle tasks
    // that can be used to start the system in dev
    dev {
        // launchScriptBase defines the folder to recursively look for launch scripts within
        // The default is "conf/provisioning"
        launchScriptBase "conf/provisioning"
        
        // When launch scripts are found, they are prefixed with this. 
        // This can be configured to be an empty string to have no prefix
        // "run" is the default value
        prefix "run"
        
        // include and exclude regex strings can be used to exclude and include paths and 
        // directories from being used gradle task generation
        // By default, the following options are provided, but they are overridden the first time 
        // anything is set.
        // By default, these will publish gradle tasks for any task in /dev or /prod.
        includeRegex ".*/etc/.*"
        excludeRegex ".*/(dev|prod)/.*"
        
        // Launch args can sometimes be provided here. 
        // Launch args are used for gradle and start scripts
        // By default, none are provided
        launchArgs "something=something", "asdf=1234"
    }
    
    // This configures what provisions are turned into start scripts 
    // and distributed along with the application bundle
    dist {
        // Same as the "dev.launchScriptBase" property above
        // Note that with this configuration, it will only look in the prod directory
        launchScriptBase "conf/provisioning/prod"
        
        // This is the same as "dev.prefix" property above
        // Note that with this configuration, any script will be prefixed with "launch"
        prefix "launch"
        
        // These are the same as dev.includeRegex and dev.excludeRegex
        // Note with this configuration, only scripts that match "core" exactly are generated
        // meaning the bin directory will create a "launchCore" script at most.
        // The "asdf" part here is unecessary as "asdf" will never match "core"
        // but it is included for completeness. 
        includeRegex "core"
        excludeRegex "asdf"
        
        // Same as the above dev.launchArgs section
        launchArgs "something=differentSomething", "asdf=6543"
           
    }
    
    // jvmArgs can be configured for dev tasks and start scripts using jvmArgs
    // The default is picked up from applicationDefaultJvmArgs
    jvmArgs applicationDefaultJvmArgs
}

```

