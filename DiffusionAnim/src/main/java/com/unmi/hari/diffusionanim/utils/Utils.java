package com.unmi.hari.diffusionanim.utils;

import android.content.Context;

public class Utils {

    /**
     * dp转换px
     *
     * @param dp dp值
     * @return 转换后的px值
     */
    public static int dp2Px(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

}
