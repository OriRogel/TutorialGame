package com.example.tutorialgame.engine.ui.circleframes;

import com.example.tutorialgame.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data repository class for all circular frame series.
 * Uses a static initializer block to define the game's cosmetic content.
 */
public class FrameData {

    private static final List<CircleFrameSeries> allSeries = new ArrayList<>();

    static {
        allSeries.add(new CircleFrameSeries(R.string.series_a_new_start,
                Arrays.asList(
                        CircleFrames.FRAME_00,
                        CircleFrames.FRAME_10,
                        CircleFrames.FRAME_11,
                        CircleFrames.FRAME_12,
                        CircleFrames.FRAME_42,
                        CircleFrames.FRAME_43)
                , new FrameUnlockCondition(FrameUnlockCondition.Condition.LEVEL, Arrays.asList(
                        0, 3, 9, 27, 36, 72))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_king_pink, Arrays.asList(
                CircleFrames.FRAME_16,
                CircleFrames.FRAME_17,
                CircleFrames.FRAME_18,
                CircleFrames.FRAME_49,
                CircleFrames.FRAME_50),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_monster_slayer, Arrays.asList(
                CircleFrames.FRAME_13,
                CircleFrames.FRAME_14,
                CircleFrames.FRAME_15,
                CircleFrames.FRAME_44),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.ENEMIES_DEFEATED, Arrays.asList(
                        1, 5, 15, 30
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_time_keeper, Arrays.asList(
                CircleFrames.FRAME_04,
                CircleFrames.FRAME_05,
                CircleFrames.FRAME_06,
                CircleFrames.FRAME_38,
                CircleFrames.FRAME_39),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.DAYS, Arrays.asList(
                        1, 30, 60, 180, 360
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_dragon_fist, Arrays.asList(
                CircleFrames.FRAME_62,
                CircleFrames.FRAME_63,
                CircleFrames.FRAME_64,
                CircleFrames.FRAME_65,
                CircleFrames.FRAME_66),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.BOSSES_DEFEATED, Arrays.asList(
                        1, 2, 3, 4, 5
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_frost_demon, Arrays.asList(
                CircleFrames.FRAME_19,
                CircleFrames.FRAME_20,
                CircleFrames.FRAME_21,
                CircleFrames.FRAME_51,
                CircleFrames.FRAME_52),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_forest_guardian, Arrays.asList(
                CircleFrames.FRAME_22,
                CircleFrames.FRAME_23,
                CircleFrames.FRAME_24,
                CircleFrames.FRAME_53,
                CircleFrames.FRAME_54),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.series_legendary_rainbow, Arrays.asList(
                CircleFrames.FRAME_07,
                CircleFrames.FRAME_08,
                CircleFrames.FRAME_09,
                CircleFrames.FRAME_40,
                CircleFrames.FRAME_41),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.frames, Arrays.asList(
                CircleFrames.FRAME_31,
                CircleFrames.FRAME_32,
                CircleFrames.FRAME_33,
                CircleFrames.FRAME_58,
                CircleFrames.FRAME_59),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.frames, Arrays.asList(
                CircleFrames.FRAME_45,
                CircleFrames.FRAME_46,
                CircleFrames.FRAME_47,
                CircleFrames.FRAME_48),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.frames, Arrays.asList(
                CircleFrames.FRAME_34,
                CircleFrames.FRAME_35,
                CircleFrames.FRAME_36,
                CircleFrames.FRAME_60,
                CircleFrames.FRAME_61),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.frames, Arrays.asList(
                CircleFrames.FRAME_28,
                CircleFrames.FRAME_29,
                CircleFrames.FRAME_30,
                CircleFrames.FRAME_56,
                CircleFrames.FRAME_57),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30, 40
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.frames, Arrays.asList(
                CircleFrames.FRAME_25,
                CircleFrames.FRAME_26,
                CircleFrames.FRAME_27,
                CircleFrames.FRAME_55),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30
                ))
        ));

        allSeries.add(new CircleFrameSeries(R.string.frames, Arrays.asList(
                CircleFrames.FRAME_01,
                CircleFrames.FRAME_02,
                CircleFrames.FRAME_03,
                CircleFrames.FRAME_37),
                new FrameUnlockCondition(FrameUnlockCondition.Condition.PURCHASE, Arrays.asList(
                        5, 10, 20, 30
                ))
        ));
    }

    public static List<CircleFrameSeries> getAllSeries() {
        return allSeries;
    }
}
