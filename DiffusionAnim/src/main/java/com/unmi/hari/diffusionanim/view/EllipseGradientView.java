package com.unmi.hari.diffusionanim.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class EllipseGradientView extends View {
    private final Paint ellipsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float baseEllipseW = 3850f;
    private final float baseEllipseH = 4012f;
    private float ellipseW, ellipseH;
    private float centerX, centerY;

    private float gradientRadius = 1f;       // 实际半径
    private float gradientSpread = 0.5f;     // 因子 0~1，控制过渡范围
    private int alpha = 255;

    // 默认内外颜色
    private int gradientInnerColor = 0x66FFFFFF;
    private int gradientOuterColor = 0xFFFFFFFF;

    private float targetRadius;
    private boolean isSizeReady = false;

    private RadialGradient gradientShader; // 缓存 Shader

    private static final int RADIUS_ANIM_DURATION = 250;
    private static final int ALPHA_ANIM_DURATION = 250;
    private static final int ALPHA_ANIM_DELAY = 100;

    private int add_anim_duration = 0;

    public interface OnAnimSetEndListener {
        void onAnimSetEnd();
    }

    private OnAnimSetEndListener mOnAnimSetEndListener;

    public EllipseGradientView(Context context) {
        super(context);
        init();
    }

    public EllipseGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ellipsePaint.setStyle(Paint.Style.FILL);
    }

    // 屏幕适配
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = 0f;

        float scaleW = w / 1080f;
        float scaleH = h / 1920f;
        float ellipseScale = Math.max(scaleW, scaleH);

        ellipseW = baseEllipseW * ellipseScale;
        ellipseH = baseEllipseH * ellipseScale;

        float ellipseEdgeDist = Math.max(ellipseW, ellipseH) / 2f;
        this.targetRadius = ellipseEdgeDist * 2.5f;

        // 根据 spread 计算实际半径
        setGradientSpread(gradientSpread);

        isSizeReady = true;
    }

    // 绘制（只用缓存的 shader）
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        ellipsePaint.setAlpha(alpha);
        canvas.drawOval(
                centerX - ellipseW / 2f,
                centerY - ellipseH / 2f,
                centerX + ellipseW / 2f,
                centerY + ellipseH / 2f,
                ellipsePaint
        );
    }

    // 更新 Shader（复用 onDraw）
    private void updateShader() {
        float validRadius = Math.max(gradientRadius, 0.01f);
        gradientShader = new RadialGradient(
                centerX, centerY,
                validRadius,
                gradientInnerColor,
                gradientOuterColor,
                Shader.TileMode.CLAMP
        );
        ellipsePaint.setShader(gradientShader);
    }

    // 启动动画
    public void startAnimSet() {
        if (!isSizeReady) {
            throw new IllegalStateException("请在视图尺寸初始化后调用 startAnimSet()");
        }

        ValueAnimator radiusAnim = ValueAnimator.ofFloat(0.1f, Math.max(ellipseW, ellipseH) / 2f, targetRadius);
        radiusAnim.setDuration(RADIUS_ANIM_DURATION + add_anim_duration);
        radiusAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        radiusAnim.addUpdateListener(anim -> {
            this.gradientRadius = (float) anim.getAnimatedValue();
            updateShader();   // 刷新 Shader
            invalidate();
        });

        ValueAnimator alphaAnim = ValueAnimator.ofInt(255, 0);
        alphaAnim.setStartDelay(ALPHA_ANIM_DELAY + add_anim_duration);
        alphaAnim.setDuration(ALPHA_ANIM_DURATION + add_anim_duration);
        alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnim.addUpdateListener(anim -> {
            this.alpha = (int) anim.getAnimatedValue();
            invalidate();
        });

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(radiusAnim, alphaAnim);

        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnAnimSetEndListener != null) {
                    mOnAnimSetEndListener.onAnimSetEnd();
                }
            }
        });

        animSet.start();
    }


    // ---- 接口 ----

    /**
     * 获取梯度实际半径
     *
     * @return 半径浮点值
     */
    public float getCurrentGradientRadius() {
        return gradientRadius;
    }

    /**
     * 设置梯度实际半径
     *
     * @param gradientRadius 半径浮点值
     * @return 链式设置
     */
    public EllipseGradientView setCurrentGradientRadius(float gradientRadius) {
        this.gradientRadius = gradientRadius;
        return this;
    }


    /**
     * 获取透明度
     *
     * @return 透明度
     */
    public int getCurrentAlpha() {
        return alpha;
    }

    /**
     * 设置动画结束监听
     *
     * @param listener 事件
     * @return 链式设置
     */
    public EllipseGradientView setOnAnimSetEndListener(OnAnimSetEndListener listener) {
        this.mOnAnimSetEndListener = listener;
        return this;
    }

    public EllipseGradientView setGradientInnerOuterColor(Object inner, Object outer) {
        if (inner instanceof Intent) {
            setGradientInnerColor((int) inner);
        } else if (inner instanceof String) {
            setGradientInnerColor((String) inner);
        }

        if (outer instanceof Intent) {
            setGradientOuterColor((int) outer);
        } else if (outer instanceof String) {
            setGradientOuterColor((String) outer);
        }
        return this;
    }

    /**
     * 设置渐变扩散内的颜色
     *
     * @param color 色值
     * @return 链式设置
     */
    public EllipseGradientView setGradientInnerColor(@ColorInt int color) {
        this.gradientInnerColor = color;
        updateShader();
        invalidate();
        return this;
    }

    /**
     * 设置渐变扩散外的颜色
     *
     * @param color 色值
     * @return 链式设置
     */
    public EllipseGradientView setGradientOuterColor(@ColorInt int color) {
        this.gradientOuterColor = color;
        updateShader();
        invalidate();
        return this;
    }

    // 重构：使用HEX颜色值
    public EllipseGradientView setGradientInnerColor(String colorHex) {
        this.gradientInnerColor = parseHexColorTo0x(colorHex);
        updateShader();
        invalidate();
        return this;
    }

    public EllipseGradientView setGradientOuterColor(String colorHex) {
        this.gradientOuterColor = parseHexColorTo0x(colorHex);
        updateShader();
        invalidate();
        return this;
    }

    public EllipseGradientView setAnimAddDuration(int duration) {
        this.add_anim_duration = duration;
        return this;
    }

    /**
     * 设置渐变过渡范围因子（0 ~ 1）
     *
     * @param factor 浮点值
     * @return 链式设置
     */
    public EllipseGradientView setGradientSpread(float factor) {
        if (factor < 0f) factor = 0f;
        if (factor > 1f) factor = 1f;
        this.gradientSpread = factor;

        float maxR = Math.max(ellipseW, ellipseH) / 2f;
        this.gradientRadius = maxR * factor;

        updateShader();
        invalidate();
        return this;
    }

    private int parseHexColorTo0x(String colorHex) {
        try {
            return Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            Log.e("DiffusionAnim.EllipseGradientView", "异常，未知错误：" + colorHex, e);
            return 0xFFFFFFFF;
        }
    }
}
