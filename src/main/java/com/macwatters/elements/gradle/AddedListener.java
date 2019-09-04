package com.macwatters.elements.gradle;

import org.gradle.api.Action;

import java.util.*;
import java.util.function.Function;

public class AddedListener<T> implements Action<T> {

    private final Map<Class, List<Action<T>>> actionsByType = new HashMap<>();
    private final Map<String, List<Action<T>>> actionsByName = new HashMap<>();
    private final Function<T, String> nameFunc;

    public AddedListener(Function<T, String> nameFunc) {
        this.nameFunc = nameFunc;
    }

    @Override
    public void execute(T obj) {
        List<Class<? extends T>> typesToRemove = new ArrayList<>();
        actionsByType.forEach((cls, actions) -> {
            if (cls.isAssignableFrom(obj.getClass())) {
                actions.forEach(a -> a.execute(obj));
                typesToRemove.add(cls);
            }
        });
        typesToRemove.forEach(actionsByType::remove);

        actionsByName.getOrDefault(nameFunc.apply(obj), Collections.emptyList()).forEach(a -> a.execute(obj));
        actionsByName.remove(nameFunc.apply(obj));
    }

    public <S extends T> void addAction(Class<S> objClass, Action<S> action) {
        actionsByType.computeIfAbsent(objClass, c -> new ArrayList<>()).add((Action<T>) action);
    }

    public void addActionByName(String name, Action<T> action) {
        actionsByName.computeIfAbsent(name, n -> new ArrayList<>()).add(action);
    }
}
