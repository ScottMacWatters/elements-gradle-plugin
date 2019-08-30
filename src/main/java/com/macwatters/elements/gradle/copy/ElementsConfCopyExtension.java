package com.macwatters.elements.gradle.copy;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.tasks.Copy;

import java.util.function.Consumer;

import static com.macwatters.elements.gradle.ClosureUtils.closureBackedAction;

public class ElementsConfCopyExtension {

    private final Consumer<Action<Copy>> onCopyConf;
    private final Consumer<Action<Copy>> onAlsoCopy;

    public ElementsConfCopyExtension(Consumer<Action<Copy>> onCopyConf, Consumer<Action<Copy>> onAlsoCopy) {
        this.onCopyConf = onCopyConf;
        this.onAlsoCopy = onAlsoCopy;
    }

    public void copyConf(Closure copyConf) {
        onCopyConf.accept(closureBackedAction(copyConf));
    }

    public void alsoCopy(Closure alsoCopy) {
        onAlsoCopy.accept(closureBackedAction(alsoCopy));
    }
}
