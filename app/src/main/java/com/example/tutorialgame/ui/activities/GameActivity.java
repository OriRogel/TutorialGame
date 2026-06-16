package com.example.tutorialgame.ui.activities;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.FrameLayout;
import com.example.tutorialgame.engine.core.GamePanel;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.ui.base.BaseActivity;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * GameActivity היא ה"מארחת" של חוויית המשחק.
 * היא מנהלת את הקשר בין מחזור החיים של אנדרואיד לבין מנוע המשחק,
 * וכוללת מאזין לשינויים במצב הטלפון כדי להבטיח עצירה בטוחה בשיחה נכנסת.
 */
@AndroidEntryPoint
public class GameActivity extends BaseActivity {
    private GamePanel gamePanel;
    private BroadcastReceiver receiveCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.gamePanel = new GamePanel(getContext(), userRepository);
        musicManager.setLooping(true);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                Gravity.CENTER
        );

        FrameLayout layout = new FrameLayout(this);
        layout.addView(gamePanel, params);
        layout.setBackgroundColor(Color.BLACK);
        setContentView(layout);

        // יצירת המאזין לשיחות טלפון
        receiveCall = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (gamePanel == null || gamePanel.getGame() == null) return;

                // אם השחקן כבר במסך מוות, אין טעם לשנות מצב
                if (gamePanel.getGame().getCurrentGameState() == State.DEATH_SCREEN) return;

                if (intent != null && TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) ||
                            TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                        gamePanel.getGame().changeState(State.MENU);
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamePanel.resumeGame();

        // רישום המאזין ברגע שהמסך חוזר לפעילות
        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(receiveCall, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePanel.pauseGame();

        // לוגיקת גיבוי למעבר לתפריט ביציאה רגילה מהאפליקציה
        if (!isFinishing()) {
            State currentState = gamePanel.getGame().getCurrentGameState();
            if (currentState != State.DEATH_SCREEN && currentState != State.MENU) {
                gamePanel.getGame().changeState(State.MENU);
            }
        }

        // ביטול רישום המאזין למניעת דליפות זיכרון
        try {
            unregisterReceiver(receiveCall);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gamePanel != null && gamePanel.getGame() != null) {
            gamePanel.getGame().stopGameLoop();
            gamePanel.getGame().onDestroy(); // ניקוי ExecutorServices ומשאבים פנימיים
        }
    }
}
