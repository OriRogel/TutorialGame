package com.example.tutorialgame.engine.ui.circleframes;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum CircleFrames {
    FRAME_00(0, 0, 112, 112),
    FRAME_01(112, 0, 112, 112),
    FRAME_02(224, 0, 112, 112),
    FRAME_03(336, 0, 112, 112),
    FRAME_04(448, 0, 112, 112),
    FRAME_05(560, 0, 112, 112),
    FRAME_06(672, 0, 112, 112),
    FRAME_07(784, 0, 112, 112),
    FRAME_08(896, 0, 112, 112),
    FRAME_09(1008, 0, 112, 112),
    FRAME_10(1120, 0, 112, 112),
    FRAME_11(1232, 0, 112, 112),
    FRAME_12(1344, 0, 112, 112),
    FRAME_13(1456, 0, 112, 112),

    FRAME_14(0, 112, 112, 112),
    FRAME_15(112, 112, 112, 112),
    FRAME_16(224, 112, 112, 112),
    FRAME_17(336, 112, 112, 112),
    FRAME_18(448, 112, 112, 112),
    FRAME_19(560, 112, 112, 112),
    FRAME_20(672, 112, 112, 112),
    FRAME_21(784, 112, 112, 112),
    FRAME_22(896, 112, 112, 112),
    FRAME_23(1008, 112, 112, 112),
    FRAME_24(1120, 112, 112, 112),
    FRAME_25(1232, 112, 112, 112),
    FRAME_26(1344, 112, 112, 112),
    FRAME_27(1456, 112, 112, 112),

    FRAME_28(0, 224, 112, 112),
    FRAME_29(112, 224, 112, 112),
    FRAME_30(224, 224, 112, 112),
    FRAME_31(336, 224, 112, 112),
    FRAME_32(448, 224, 112, 112),
    FRAME_33(560, 224, 112, 112),
    FRAME_34(672, 224, 112, 112),
    FRAME_35(784, 224, 112, 112),
    FRAME_36(896, 224, 112, 112),
    FRAME_37(1008, 224, 112, 112),
    FRAME_38(1120, 224, 112, 112),
    FRAME_39(1232, 224, 112, 112),
    FRAME_40(1344, 224, 112, 112),
    FRAME_41(1456, 224, 112, 112),

    FRAME_42(0, 336, 112, 112),
    FRAME_43(112, 336, 112, 112),
    FRAME_44(224, 336, 112, 112),
    FRAME_45(336, 336, 112, 112),
    FRAME_46(448, 336, 112, 112),
    FRAME_47(560, 336, 112, 112),
    FRAME_48(672, 336, 112, 112),
    FRAME_49(784, 336, 112, 112),
    FRAME_50(896, 336, 112, 112),
    FRAME_51(1008, 336, 112, 112),
    FRAME_52(1120, 336, 112, 112),
    FRAME_53(1232, 336, 112, 112),
    FRAME_54(1344, 336, 112, 112),
    FRAME_55(1456, 336, 112, 112),

    FRAME_56(0, 448, 112, 112),
    FRAME_57(112, 448, 112, 112),
    FRAME_58(224, 448, 112, 112),
    FRAME_59(336, 448, 112, 112),
    FRAME_60(448, 448, 112, 112),
    FRAME_61(560, 448, 112, 112),
    FRAME_62(672, 448, 112, 112),
    FRAME_63(784, 448, 112, 112),
    FRAME_64(896, 448, 112, 112),
    FRAME_65(1008, 448, 112, 112),
    FRAME_66(1120, 448, 112, 112),

    BACKGROUND(1232, 448, 112, 112),
    LOCK(1344, 448, 112, 112);

    private final Bitmap circleFrame;

    CircleFrames(int x, int y, int width, int height) {
        circleFrame = BitmapManager.getBitmapRegion(R.drawable.atl_circleframes, x, y, width, height, 40/112f, true);
    }

    public Bitmap getCircleFrame() {
        return circleFrame;
    }
}
