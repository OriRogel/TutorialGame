package com.example.tutorialgame.quest;

import androidx.annotation.StringRes;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Represents a single quest in the game world.
 * Manages quest status, rewards, and completion logic.
 */
public class Quest {
    private final @StringRes int taskRes;
    private final String internalId; // Unique internal identifier for the quest (used as a flag in DB)
    private final QuestType questType; // The type and target of the quest
    private final Runnable onCompleteAction; // Custom logic to run upon completion
    private final int coins, xp; // Rewards for completing the quest

    /**
     * Constructs a new Quest instance.
     * @param taskRes String resource for the quest description.
     * @param internalId Unique ID used for tracking completion in the cloud.
     * @param questType The logical type of the quest (e.g., DIALOGUE).
     * @param coins Amount of coins rewarded upon completion.
     * @param xp Amount of experience points rewarded upon completion.
     * @param onCompleteAction A callback to execute when the quest is finished.
     */
    public Quest(@StringRes int taskRes, String internalId, QuestType questType, int coins, int xp, Runnable onCompleteAction) {
        this.taskRes = taskRes;
        this.internalId = internalId;
        this.questType = questType;
        this.coins = coins;
        this.xp = xp;
        this.onCompleteAction = onCompleteAction;
    }

    /**
     * Checks if the quest has already been completed by looking up its ID in the world state.
     * @return true if completed, false otherwise.
     */
    public boolean isCompleted() {
        return MyApp.getWorldStateDoc().getCheckPoint(internalId);
    }

    /**
     * Finalizes the quest. Updates cloud data, grants rewards, and triggers the callback.
     * Includes a safety check to prevent double completion.
     */
    public void complete() {
        if (isCompleted()) return;

        // Mark as completed in cloud
        MyApp.getWorldStateDoc().setCheckPoint(internalId);
        
        // Update player progress and rewards
        MyApp.getProgress().increaseQuestsCompleted();
        MyApp.getCosmetic().increaseCoins(coins);
        MyApp.getProgress().updateXp(xp);

        // Execute post-completion logic
        if (onCompleteAction != null) {
            onCompleteAction.run();
        }
    }

    /**
     * @return The localized description of the quest.
     */
    public String getTaskString() {
        return BaseActivity.getContext().getString(taskRes);
    }

    /**
     * @return The logic type of this quest.
     */
    public QuestType getQuestType() {
        return questType;
    }
}
