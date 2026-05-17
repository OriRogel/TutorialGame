package com.example.tutorialgame.engine.ui.customviews.radiogroup;

import static android.content.Context.MODE_PRIVATE;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;
import androidx.core.content.ContextCompat;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleImages;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.Objects;

public class CustomRadioGroup {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(CustomRadioGroup group, int selectedIndex);
    }

    private OnSelectionChangedListener listener;
    private final Context context;
    private final CircleButton[] radioButtons;
    private CircleButton selectedButton;
    private int selectedIndex = -1;

    private final TextRenderer labelRenderer, titleRenderer;
    private final int colorNormal, colorSelected;

    private final SharedPreferences settingsPreferences;
    private final String key;
    private final RadioGroupList radioGroupEnum;
    private final NinePatchDrawable background;
    private final float spacing;

    public CustomRadioGroup(float x, float y, RadioGroupList radioGroupEnum) {
        this.context = BaseActivity.getContext();
        this.key = radioGroupEnum.getKey();
        this.radioGroupEnum = radioGroupEnum;

        radioButtons = new CircleButton[radioGroupEnum.getNamesID().length];
        
        // Initializing the first button to get its height for spacing calculation
        radioButtons[0] = new CircleButton(new PointF(x, y), CircleImages.RADIO, false);
        this.spacing = radioButtons[0].getHitbox().height() + 2 * SCALE_MULTIPLIER; // Tight spacing
        
        for (int i = 1; i < radioGroupEnum.getNamesID().length; i++) {
            radioButtons[i] = new CircleButton(new PointF(x, y + i * spacing), CircleImages.RADIO, false);
        }

        float textSize = 9 * SCALE_MULTIPLIER;

        // אתחול הרנדררים
        labelRenderer = new TextRenderer(textSize, R.color.floral_white);
        titleRenderer = new TextRenderer(textSize * 1.25f, R.color.floral_white);

        // שמירת ערכי הצבעים
        colorNormal = ContextCompat.getColor(context, R.color.floral_white);
        colorSelected = ContextCompat.getColor(context, R.color.text_color_pressed);

        // Initialize background
        background = (NinePatchDrawable) ContextCompat.getDrawable(context, R.drawable.interior_background);
        setBackground(x, y);

        // הגדרת מיקום הכותרת פעם אחת בבנאי
        float titleY = y - titleRenderer.getTextSize() / 1.5f;
        float titleX = Objects.requireNonNull(background).getBounds().left + (background.getBounds().width() - titleRenderer.measureText(radioGroupEnum.getTitle())) / 2;

        titleRenderer.setPosition(titleX, titleY);
        titleRenderer.setShadowColor(ContextCompat.getColor(context, R.color.dark_moon));
        titleRenderer.setShadowOffset(SCALE_MULTIPLIER, SCALE_MULTIPLIER);

        // Get SharedPreferences
        settingsPreferences = context.getSharedPreferences("settings", MODE_PRIVATE);
        setInitialButton();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    private void setBackground(float x, float y) {
        int[] namesID = radioGroupEnum.getNamesID();
        float width = 0;
        for (int i = 0; i < namesID.length; i++) {
            width = Math.max(width, labelRenderer.measureText(radioGroupEnum.getName(i)));
        }

        // Add padding for the radio buttons and text
        width += radioButtons[0].getHitbox().width() + 10 * SCALE_MULTIPLIER;
        float height = (radioButtons.length - 1) * spacing + radioButtons[0].getHitbox().height() + 7 * SCALE_MULTIPLIER;

        float bgX = x - radioButtons[0].getHitbox().width() / 2 - 5 * SCALE_MULTIPLIER;
        float bgY = y - radioButtons[0].getHitbox().height() / 2 - 5 * SCALE_MULTIPLIER;

        background.setBounds((int) bgX, (int) bgY, (int) (bgX + width), (int) (bgY + height));
    }

    private void setInitialButton() {
        int initialIndex = settingsPreferences.getInt(key, -1);
        if (initialIndex >= 0 && initialIndex < radioButtons.length) {
            selectedIndex = initialIndex;
            selectedButton = radioButtons[selectedIndex];
            selectedButton.setEnabled(false);
        }
    }

    public void render(Canvas c) {
        background.draw(c);
        drawTitle(c);
        drawButtons(c);
    }

    private void drawTitle(Canvas c) {
        titleRenderer.drawWithShadow(radioGroupEnum.getTitle(), c);
    }

    private void drawButtons(Canvas c) {
        for (int i = 0; i < radioButtons.length; i++) {
            CircleButton button = radioButtons[i];
            button.draw(c);

            float textX = button.getHitbox().right + 3 * SCALE_MULTIPLIER;
            float textY = button.getHitbox().centerY() + 3 * SCALE_MULTIPLIER;

            labelRenderer.setPosition(textX, textY);
            labelRenderer.setColor(button == selectedButton ? colorSelected : colorNormal);
            labelRenderer.drawText(radioGroupEnum.getName(i), c);
        }
    }

    public void eventHandler(MotionEvent event) {
        for (int i = 0; i < radioButtons.length; i++) {
            CircleButton button = radioButtons[i];
            if (button.eventHandler(event)) {
                if (selectedButton != null)
                    selectedButton.setEnabled(true);

                selectedButton = button;
                selectedIndex = i;
                selectedButton.setEnabled(false);
                
                if (listener != null) {
                    listener.onSelectionChanged(this, selectedIndex);
                }

                SoundManager.getInstance(context).playSfx(R.raw.sfx_bloop);
                break;
            }
        }
    }

    public Rect getHitbox() {
        return background.getBounds();
    }

    /**
     * Refreshes the localized title of the radio group.
     */
    public void refreshStrings() {
        float titleX = Objects.requireNonNull(background).getBounds().left + (background.getBounds().width() - titleRenderer.measureText(radioGroupEnum.getTitle())) / 2;
        titleRenderer.setX(titleX);
    }
}
