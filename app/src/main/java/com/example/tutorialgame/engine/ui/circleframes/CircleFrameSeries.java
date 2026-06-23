package com.example.tutorialgame.engine.ui.circleframes;

import androidx.annotation.StringRes;

import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.List;

public class CircleFrameSeries {
    private final @StringRes int seriesName;
    private final List<CircleFrames> frames;
    private final FrameUnlockCondition condition;

    public CircleFrameSeries(@StringRes int seriesName, List<CircleFrames> frames, FrameUnlockCondition condition) {
        this.seriesName = seriesName;
        this.frames = frames;
        this.condition = condition;
    }

    public String getSeriesName() {
        return BaseActivity.getContext().getString(seriesName);
    }

    public List<CircleFrames> getFrames() {
        return frames;
    }

    public boolean checkCondition(int i, UserRepository userRepository) {
        return (condition.getPriceArr().get(i) <= condition.getCurrentAmount(userRepository));
    }
    public FrameUnlockCondition getCondition() {
        return condition;
    }
}
