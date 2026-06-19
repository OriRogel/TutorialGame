package com.example.tutorialgame.engine.ui.circleframes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.cloud.document.CosmeticDoc;
import com.example.tutorialgame.cloud.document.ProgressDoc;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;

import java.util.List;

public class FramesAdapter extends RecyclerView.Adapter<FramesAdapter.FrameViewHolder> {
    public interface OnDataChangedListener {
        void onDataChanged(int seriesPos);
    }

    private final CircleFrameSeries series;
    private final List<CircleFrames> frames;
    private final String seriesName;
    private final int seriesPosition; 
    private final String selectedFrameName;
    private final FrameSeriesAdapter.FrameSelectionListener listener;
    private final OnDataChangedListener dataChangedListener;
    private final CosmeticDoc cosmeticDoc;
    private final ProgressDoc progressDoc;

    public FramesAdapter(CircleFrameSeries series, int seriesPosition, String selectedFrameName, FrameSeriesAdapter.FrameSelectionListener listener, OnDataChangedListener dataChangedListener, UserRepository userRepository) {
        this.series = series;
        this.frames = series.getFrames();
        this.seriesName = series.getSeriesName();
        this.seriesPosition = seriesPosition;
        this.selectedFrameName = selectedFrameName;
        this.listener = listener;
        this.dataChangedListener = dataChangedListener;
        this.cosmeticDoc = userRepository.getCosmetic();
        this.progressDoc = userRepository.getProgress();
    }

    public FrameUnlockCondition.Condition getConditionType() {
        return series.getCondition().getSelectedCondition();
    }

    /**
     * מרענן רק את הפריטים שמושפעים משינוי במטבעות/נתונים בשורות אחרות.
     */
    public void refreshLockedItemsStatus() {
        for (int i = 0; i < frames.size(); i++) {
            // אם הפריט נעול, מצבו הגרפי עשוי להשתנות (למשל צבע המנעול)
            if (!cosmeticDoc.isFrameAvailable(frames.get(i).name())) {
                notifyItemChanged(i);
            }
        }
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_frame, parent, false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
        CircleFrames frame = frames.get(position);
        boolean isSelected = frame.name().equals(selectedFrameName);
        holder.bind(frame, isSelected, position, listener);
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    public class FrameViewHolder extends RecyclerView.ViewHolder {
        private final ImageView frameImage;
        private final ImageView lockIcon;
        private final ImageView glowEffect;

        public FrameViewHolder(@NonNull View itemView) {
            super(itemView);
            frameImage = itemView.findViewById(R.id.iv_frame_image);
            lockIcon = itemView.findViewById(R.id.iv_lock_icon);
            glowEffect = itemView.findViewById(R.id.iv_glow_effect);
        }

        private void bind(CircleFrames frame, boolean isSelected, int framePosition, FrameSeriesAdapter.FrameSelectionListener listener) {
            lockIcon.animate().cancel();
            lockIcon.setRotation(0f);
            lockIcon.setTranslationY(0f);

            boolean isUnlocked = cosmeticDoc.isFrameAvailable(frame.name());
            frameImage.setImageBitmap(frame.getCircleFrame());
            Context context = itemView.getContext();

            glowEffect.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            if (isUnlocked) {
                lockIcon.setVisibility(View.GONE);
                frameImage.setAlpha(1f);
                itemView.setOnClickListener(v -> {
                    if (isSelected) return;
                    cosmeticDoc.setCurrentFrame(frame.name());
                    SoundManager.getInstance(itemView.getContext()).playSfx(R.raw.sfx_bloop);
                    BaseActivity.ButtonPressVibe();
                    listener.onFrameSelectionChanged(seriesPosition, framePosition);
                });
            } else {
                setupLockedState(framePosition, context);
                itemView.setOnClickListener(v -> {
                    showBalloonPopup(v, framePosition);
                    SoundManager.getInstance(context).playSfx(R.raw.sfx_bloop);
                });
            }
        }

        private void setupLockedState(int pos, Context context) {
            lockIcon.setVisibility(View.VISIBLE);
            lockIcon.setImageBitmap(CircleFrames.LOCK.getCircleFrame());

            if (series.checkCondition(pos) && (pos == 0 || cosmeticDoc.isFrameAvailable(frames.get(pos-1).name()))) {
                frameImage.setAlpha(1f);
                lockIcon.setAlpha(0.3f);
                if (series.getCondition().getSelectedCondition() == FrameUnlockCondition.Condition.PURCHASE)
                    lockIcon.setColorFilter(Color.GREEN);
                else lockIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_color_pressed));
            } else {
                frameImage.setAlpha(0.7f);
                lockIcon.setAlpha(1f);
                lockIcon.clearColorFilter();
            }
        }

        private void showBalloonPopup(View anchor, int pos) {
            Balloon balloon = new Balloon.Builder(itemView.getContext())
                    .setLayout(R.layout.balloon_bubble_frame)
                    .setArrowSize(10)
                    .setArrowOrientation(ArrowOrientation.BOTTOM)
                    .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                    .setCornerRadius(12f)
                    .setBackgroundColorResource(R.color.floral_white)
                    .setBalloonAnimation(BalloonAnimation.ELASTIC)
                    .setDismissWhenClicked(true)
                    .setLifecycleOwner((androidx.lifecycle.LifecycleOwner) itemView.getContext())
                    .build();

            TextView title = balloon.getContentView().findViewById(R.id.tv_bubble_text);
            TextView desc = balloon.getContentView().findViewById(R.id.tv_progress_desc);
            TextView progress = balloon.getContentView().findViewById(R.id.tv_progress);
            Button btnUnlock = balloon.getContentView().findViewById(R.id.btn_unlock);

            title.setText(getTitle(itemView.getContext(), pos));
            
            if (pos > 0 && !cosmeticDoc.isFrameAvailable(frames.get(pos-1).name())) {
                desc.setText(R.string.unlock_prev_message);
                progress.setVisibility(View.GONE);
                btnUnlock.setVisibility(View.GONE);
            } else {
                progress.setVisibility(View.VISIBLE);
                btnUnlock.setVisibility(View.VISIBLE);
                desc.setText(getProgressDescription(itemView.getContext()));
                progress.setText(getProgress(pos));

                btnUnlock.setOnClickListener(v -> {
                    if (series.checkCondition(pos)) {
                        handleUnlock(pos);
                        balloon.dismiss();
                    }
                });
            }
            balloon.showAsDropDown(anchor);
        }

        private void handleUnlock(int pos) {
            String frameName = frames.get(pos).name();
            lockIcon.clearColorFilter();
            
            // 1. ביצוע הרכישה לוגית (חשוב לפני הדיווח)
            if (series.getCondition().getSelectedCondition() == FrameUnlockCondition.Condition.PURCHASE) {
                if (cosmeticDoc.purchase(series.getCondition().getPriceArr().get(pos))) {
                    SoundManager.getInstance(itemView.getContext()).playSfx(R.raw.sfx_coin_drop);
                } else {
                    //TODO: add dialog
                }
            }
            
            cosmeticDoc.addAvailableFrame(frameName);
            SoundManager.getInstance(itemView.getContext()).playSfx(R.raw.sfx_unlock);

            // 3. אנימציה מקומית
            lockIcon.animate()
                    .rotation(90f)
                    .translationY(100f)
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        lockIcon.setVisibility(View.GONE);
                        notifyItemChanged(pos);
                        dataChangedListener.onDataChanged(seriesPosition);
                        if (pos + 1 < frames.size()) notifyItemChanged(pos + 1);
                    })
                    .start();
        }
    }

    private String getProgressDescription(Context ctx) {
        switch (series.getCondition().getSelectedCondition()) {
            case PURCHASE: return ctx.getString(R.string.con_purchase);
            case LEVEL: return ctx.getString(R.string.con_level);
            case ENEMIES_DEFEATED: return ctx.getString(R.string.con_enemies);
            case BOSSES_DEFEATED: return ctx.getString(R.string.con_boss);
            case DAYS: return ctx.getString(R.string.con_days);
            default: return "";
        }
    }
    private String getProgress(int pos) {
        switch (series.getCondition().getSelectedCondition()) {
            case PURCHASE: return cosmeticDoc.getCoinsLeft() + "/" + series.getCondition().getPriceArr().get(pos);
            case LEVEL: return progressDoc.getLevel() + "/" + series.getCondition().getPriceArr().get(pos);
            case ENEMIES_DEFEATED: return progressDoc.getEnemiesDefeated() + "/" + series.getCondition().getPriceArr().get(pos);
            case DAYS: return progressDoc.getDaysLoggedIn() + "/" + series.getCondition().getPriceArr().get(pos);
            default: return "";
        }
    }
    private String getTitle(Context ctx, int framePosition) {
        String status = !series.checkCondition(framePosition) ? ctx.getString(R.string.locked) : 
                       (framePosition == 0 || cosmeticDoc.isFrameAvailable(frames.get(framePosition-1).name())) ?
                       ctx.getString(R.string.available) : ctx.getString(R.string.locked);

        return seriesName + ": " + ctx.getString(R.string.grade) + " " + (framePosition + 1) + " - " + status;
    }
}
