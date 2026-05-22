package com.example.tutorialgame.managers.worldactions;

/**
 * Interface for a single atomic change to the game world.
 * Matches WorldActions.Action but kept as a standalone file for easier referencing
 * if needed by other components without importing the whole library.
 */
public interface WorldAction {
    void execute();
}
