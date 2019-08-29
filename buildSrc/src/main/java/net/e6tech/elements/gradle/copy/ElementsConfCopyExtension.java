package net.e6tech.elements.gradle.copy;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.tasks.Copy;
import org.gradle.util.ClosureBackedAction;

import java.util.function.Consumer;

public class ElementsConfCopyExtension {

    private final Consumer<Action<Copy>> onCopyConf;
    private final Consumer<Action<Copy>> onAlsoCopy;

    public ElementsConfCopyExtension(Consumer<Action<Copy>> onCopyConf, Consumer<Action<Copy>> onAlsoCopy) {
        this.onCopyConf = onCopyConf;
        this.onAlsoCopy = onAlsoCopy;
    }

    public void copyConf(Closure copyConf) {
        onCopyConf.accept(ClosureBackedAction.of(copyConf));
    }

    public void alsoCopy(Closure alsoCopy) {
        onAlsoCopy.accept(ClosureBackedAction.of(alsoCopy));
    }
}
