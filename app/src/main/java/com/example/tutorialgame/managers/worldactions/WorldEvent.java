package com.example.tutorialgame.managers.worldactions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldEvent {
    private final List<WorldAction> actions = new ArrayList<>();

    public WorldEvent(WorldAction... initialActions) {
        actions.addAll(Arrays.asList(initialActions));
    }

    public void addAction(WorldAction action) {
        actions.add(action);
    }

    public void trigger() {
        for (WorldAction action : actions) {
            action.execute();
        }
    }
}
