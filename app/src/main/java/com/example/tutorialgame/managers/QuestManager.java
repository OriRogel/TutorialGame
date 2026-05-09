package com.example.tutorialgame.managers;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.environments.maploder.ObjectData;
import com.example.tutorialgame.quest.ComplexQuest;
import com.example.tutorialgame.quest.Quest;
import com.example.tutorialgame.quest.QuestType;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class QuestManager {
    private static final List<ComplexQuest> mainStoryLine = new ArrayList<>();
    private static ComplexQuest currentComplexQuest;
    private static final Paint lineBase, lineShadow;
    private static final TextRenderer titleRenderer;
    private static final TextRenderer taskRenderer;
    private static final float startX, lineY, lineEnd;

    static {
        titleRenderer = new TextRenderer(SCALE_MULTIPLIER*7.5f, R.color.floral_white);
        taskRenderer = new TextRenderer(SCALE_MULTIPLIER*4.5f, R.color.floral_white);
        titleRenderer.setPosition(SCALE_MULTIPLIER*7f, TILE_SIZE*3);
        taskRenderer.setPosition(SCALE_MULTIPLIER*7f, TILE_SIZE*3.6f);

        startX = SCALE_MULTIPLIER*10.5f;
        lineY = titleRenderer.getY() + 1.5f*SCALE_MULTIPLIER;
        lineEnd = startX + TILE_SIZE*3f;

        lineBase = new Paint();
        lineBase.setColor(BaseActivity.getContext().getColor(R.color.floral_white));
        lineBase.setStrokeWidth(SCALE_MULTIPLIER);

        lineShadow = new Paint();
        lineShadow.setColor(Color.BLACK);
        lineShadow.setStrokeWidth(SCALE_MULTIPLIER);

        initQuests();
    }

    private QuestManager() {}

    /**
     * פעולת עזר ליצירת Quest בצורה מקוצרת.
     */
    private static Quest quest(int taskRes, String id, QuestType.Type type, String target, int coins, int xp, Runnable action) {
        return new Quest(taskRes, id, new QuestType(type, target), coins, xp, action);
    }

    /**
     * פעולת עזר להוספת משימה מורכבת לעלילה הראשית.
     */
    private static void addStory(int titleRes, Quest... subQuests) {
        mainStoryLine.add(new ComplexQuest(titleRes, Arrays.asList(subQuests)));
    }

    private static void initQuests() {
        mainStoryLine.clear();

        // 1. לדבר עם אבא
        addStory(R.string.quest,
                quest(R.string.talk_to_father, "event_player_talkedToFather", QuestType.Type.DIALOGUE_WITH, "FATHER", 1, 20, null)
        );

        // 2. מצא את הנפח וקבל חרב
        addStory(R.string.quest,
                quest(R.string.find_blacksmith, "event_player_talkedToFriend", QuestType.Type.DIALOGUE_WITH, "BEST_FRIEND", 0, 10,
                        () -> WorldEventManager.triggerEvent("TALKED_TO_FRIEND_FIRST_TIME")),
                quest(R.string.find_blacksmith, "event_blacksmith_finishedFirstTalk", QuestType.Type.DIALOGUE_WITH, "BLACKSMITH", 0, 5,
                        () -> Game.setNextGameState(Game.GameState.CUTSCENE)),
                quest(R.string.find_blacksmith, "event_player_receivedWeapon", QuestType.Type.DIALOGUE_WITH, "BLACKSMITH", 3, 15,
                        () -> {
                            MyApp.getWorldStateDoc().setCurrentWeapon(Weapons.BIG_SWARD.name());
                            WorldEventManager.triggerEvent("RECEIVED_SWORD_FROM_BLACKSMITH");
                        })
        );

        // 3. מה הוא רוצה?
        addStory(R.string.quest,
                quest(R.string.what_he_wants, "event_player_talkedToFriend2", QuestType.Type.DIALOGUE_WITH, "BEST_FRIEND", 0, 15,
                        () -> WorldEventManager.triggerEvent("TALKED_TO_FRIEND_IN_SHOP"))
        );

        // 4. חיפוש רמזים
        addStory(R.string.quest,
                quest(R.string.find_clues, "event_player_talkedToWhiteKnight", QuestType.Type.DIALOGUE_WITH, "WHITE_KNIGHT", 0, 30,
                        () -> WorldEventManager.triggerEvent("TALKED_TO_WHITE_KNIGHT"))
        );

        // 5. המבוך
        addStory(R.string.quest,
                quest(R.string.maybe_strong_enough, "event_enter_maze", QuestType.Type.ENTER_ZONE, "maze.tmx", 0, 0,
                        () -> WorldEventManager.triggerEvent("ENTER_MAZE")),
                quest(R.string.reach_center, "event_enter_maze_center", QuestType.Type.ENTER_ZONE, "MAZE_CENTER", 0, 0,
                        () -> Game.setNextGameState(Game.GameState.CUTSCENE)),
                quest(R.string.run, "event_escaped_from_maze", QuestType.Type.ENTER_ZONE, "chief_home.tmx", 5, 20, null)
        );

        update();
    }

    /**
     * עדכון המשימה הנוכחית.
     */
    public static void update() {
        currentComplexQuest = null;
        for (ComplexQuest cq : mainStoryLine) {
            if (!cq.isAllCompleted()) {
                currentComplexQuest = cq;
                break;
            }
        }
    }

    /**
     * מחזיר את ה-"Step" (ה-Quest הבודד) שצריך לבצע כרגע.
     */
    private static Quest getCurrentStep() {
        return (currentComplexQuest != null) ? currentComplexQuest.getCurrentSubQuest() : null;
    }

    /**
     * כשדיאלוג מסתיים, בודקים אם הוא השלים את השלב הנוכחי.
     */
    public static void onDialogueFinished(String characterId) {
        Quest step = getCurrentStep();
        if (step != null && step.getQuestType().type == QuestType.Type.DIALOGUE_WITH) {
            if (step.getQuestType().targetId.equals(characterId)) {
                completeCurrentStep();
            }
        }
    }

    /**
     * בודק אם השחקן נכנס למפה מסוימת או לאזור (Trigger) מסוים.
     * @param zoneName שם המפה (אם מדובר במעבר מפות) או null לבדיקת טריגרים פנימיים.
     */
    public static void onEnterZone(String zoneName) {
        Quest step = getCurrentStep();
        if (step == null || step.getQuestType().type != QuestType.Type.ENTER_ZONE) return;

        String target = step.getQuestType().targetId;

        // בדיקה 1: האם זה שם המפה שנכנסנו אליה כרגע
        if (target.equals(zoneName)) {
            completeCurrentStep();
            return;
        }

        // בדיקה 2: האם השחקן נוגע ב-Trigger עם השם הזה במפה הנוכחית
        GameMap currentMap = MapManager.getCurrentMap();
        if (currentMap != null && currentMap.getMapLoadData().triggerLayer != null) {
            Player player = currentMap.getPlayer();
            if (player != null) {
                for (ObjectData trigger : currentMap.getMapLoadData().triggerLayer) {
                    if (trigger.name.equals(target) && RectF.intersects(trigger.bounds, player.getHitBox())) {
                        completeCurrentStep();
                        break;
                    }
                }
            }
        }
    }

    /**
     * כשחפץ נאסף.
     */
    public static void onItemCollected(String itemId) {
        Quest step = getCurrentStep();
        if (step == null) return;

        QuestType condition = step.getQuestType();
        if (condition != null && condition.type == QuestType.Type.COLLECT_ITEM) {
            if (condition.targetId.equals(itemId)) {
                completeCurrentStep();
            }
        }
    }

    /**
     * השלמת השלב הנוכחי ורענון המנהל.
     */
    public static void completeCurrentStep() {
        Quest step = getCurrentStep();
        if (step != null) {
            step.complete(); 
            update(); 
        }
    }

    public static void draw(Canvas c) {
        if (currentComplexQuest == null) return;

        String title = BaseActivity.getContext().getString(currentComplexQuest.getTitleRes());
        titleRenderer.drawWithShadow(title, c);

        c.drawLine(startX-SCALE_MULTIPLIER*5f, lineY+SCALE_MULTIPLIER*0.7f, lineEnd, lineY+SCALE_MULTIPLIER*0.7f, lineShadow);
        c.drawLine(startX-SCALE_MULTIPLIER*5f, lineY, lineEnd, lineY, lineBase);

        Quest step = getCurrentStep();
        if (step != null) {
            taskRenderer.drawWithShadow(step.getTaskString(), c);
        }
    }
}