package com.macwatters.elements.gradle;

import groovy.lang.Closure;
import org.gradle.api.Action;

import java.util.Optional;

public class ClosureUtils {

    public static Closure<String> closureOf(String returnValue) {
        return new Closure<String>(null) {
            private static final long serialVersionUID = -4791660809093573636L;

            @Override
            public String call() {
                return returnValue;
            }
        };
    }

    public static Optional<String> s(Object stringOrClosureReturnString) {
        if (stringOrClosureReturnString instanceof Closure) {
            Object result = ((Closure) stringOrClosureReturnString).call();
            return result == null ? Optional.empty() : Optional.ofNullable(result.toString());
        } else if (stringOrClosureReturnString != null) {
            return Optional.ofNullable(stringOrClosureReturnString.toString());
        } else {
            return Optional.empty();
        }
    }

    public static <T> Action<T> closureBackedAction(Closure closure) {
        Closure copy = (Closure)closure.clone();
        copy.setResolveStrategy(Closure.DELEGATE_FIRST);
        return delegate -> {
            copy.setDelegate(delegate);
            if (copy.getMaximumNumberOfParameters() == 0) {
                copy.call();
            } else {
                copy.call(delegate);
            }
        };
    }

    private ClosureUtils() {
    }
}
