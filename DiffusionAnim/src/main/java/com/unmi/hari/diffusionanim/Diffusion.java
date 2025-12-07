package com.unmi.hari.diffusionanim;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unmi.hari.diffusionanim.utils.Utils;
import com.unmi.hari.diffusionanim.view.EllipseGradientView;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Diffusion {

    private OnAnimSetEndListener mOnAnimSetEndListener;

    private List<String> ignoredActivities;                // 来自这个界面的不执行动画，填写你的界面的名称

    private Bitmap aScreenshot;                            // 截图信息

    private Object gradientInnerColor = "#66FFFFFF";       // 渐变扩散内的颜色

    private Object gradientOuterColor = "#FFFFFF";         // 渐变扩散外的颜色

    private float gradientSpread = 0.7f;                   // 渐变过渡范围因子

    private float currentGradientRadius = 1f;              // 梯度实际半径

    public Diffusion setFromEvent(List<String> fromEvent) {
        this.ignoredActivities = fromEvent;
        return this;
    }

    public Diffusion setScreenshotBitmap(Bitmap aScreenshot) {
        this.aScreenshot = aScreenshot;
        return this;
    }

    public Diffusion setGradientInnerColor(Object gradientInnerColor) {
        this.gradientInnerColor = gradientInnerColor;
        return this;
    }

    public Diffusion setGradientOuterColor(Object gradientOuterColor) {
        this.gradientOuterColor = gradientOuterColor;
        return this;
    }

    public Diffusion setGradientSpread(float gradientSpread) {
        this.gradientSpread = gradientSpread;
        return this;
    }

    public Diffusion setCurrentGradientRadius(float currentGradientRadius) {
        this.currentGradientRadius = currentGradientRadius;
        return this;
    }

    /**
     * 第一个界面跳转设置包装
     *
     * @param context 上下文
     * @param toClass 目标界面
     * @return 返回Intent，需要自己使用 startActivity()
     */
    public Intent start(@NonNull Context context, Class<?> toClass) {
        if (!isContextContainActivity(context)) return null;

        Activity targetActivity = getActivityFromContext(context);
        if (targetActivity == null) return null;

        if (aScreenshot == null) {
            View rootView = targetActivity.getWindow().getDecorView();
            // 截取根视图
            aScreenshot = Bitmap.createBitmap(
                    rootView.getWidth(),
                    rootView.getHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(aScreenshot);
            rootView.draw(canvas);
        }

        // Bitmap 转 byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        aScreenshot.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] screenshotBytes = baos.toByteArray();

        DiffusionInfo mDiffusionInfo = new DiffusionInfo().setInfo(
                ignoredActivities,
                screenshotBytes,
                gradientInnerColor,
                gradientOuterColor,
                gradientSpread,
                currentGradientRadius
        );


        // 开始跳转
        if (toClass != null) {
            Intent intent = new Intent(context, toClass);
            intent.putExtra("DAT_DIFFUSION_INFO", mDiffusionInfo);
            intent.putExtra("DAT_IGNORED_ACTIVITIES", context.getClass().getSimpleName());
            return intent;
        }
        return null;
    }


    /**
     * 第二个界面载入动画
     *
     * @param context              上下文
     * @param animationAmplitude   动画幅度（1f为默认样式）
     * @param animationAddDuration 动画时长增加（0为不增加）
     * @param listener             动画结束监听（可为null）
     */
    public void end(@NonNull Context context, float animationAmplitude, int animationAddDuration, OnAnimSetEndListener listener) {
        if (!isContextContainActivity(context)) return;

        Activity targetActivity = getActivityFromContext(context);
        if (targetActivity == null) return;

        Intent intent = targetActivity.getIntent();
        if (intent != null) {
            this.mOnAnimSetEndListener = listener;

            DiffusionInfo mDiffusionInfo = intent.getParcelableExtra("DAT_DIFFUSION_INFO");

            if (mDiffusionInfo == null) return;
            if (mDiffusionInfo.ignoredActivities != null)
                if (mDiffusionInfo.ignoredActivities.contains(intent.getStringExtra("DAT_IGNORED_ACTIVITIES")))
                    return;

            // 添加视图
            View contentView = targetActivity.findViewById(android.R.id.content);
            if (!(contentView instanceof ViewGroup)) return;
            ViewGroup contentViewGroup = (ViewGroup) contentView;

            // 移除旧视图，避免重复添加
            View oldOverlayView = contentViewGroup.findViewById(R.id.dat_body);
            if (oldOverlayView != null) contentViewGroup.removeView(oldOverlayView);

            // 添加视图
            View overlayView = LayoutInflater.from(context).inflate(R.layout.dat_transition, (ViewGroup) contentView, false);
            ((ViewGroup) contentView).addView(overlayView);

            // 设置视图
            ViewGroup.LayoutParams params = overlayView.getLayoutParams();
            params.height = params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            overlayView.setLayoutParams(params);

            byte[] screenshotBytes = mDiffusionInfo.screenshotBytes;
            Bitmap aScreenshot = BitmapFactory.decodeByteArray(screenshotBytes, 0, screenshotBytes.length);
            ImageView overlay_img = overlayView.findViewById(R.id.dat_body_bg);
            overlay_img.setImageBitmap(aScreenshot);

            EllipseGradientView ellipse = ((EllipseGradientView) overlayView.findViewById(R.id.dat_body_egv))
                    .setGradientInnerOuterColor(mDiffusionInfo.gradientInnerColor, mDiffusionInfo.gradientOuterColor)
                    .setGradientSpread(mDiffusionInfo.gradientSpread)
                    .setCurrentGradientRadius(mDiffusionInfo.currentGradientRadius)
                    .setAnimAddDuration(animationAddDuration);

            // 动画
            float dat_transition_trY = Utils.dp2Px(context, 30) * animationAmplitude;
            float content_trY = Utils.dp2Px(context, 50) * animationAmplitude;

            overlay_img.animate()
                    .translationY(Utils.dp2Px(context, 15f) * animationAmplitude)
                    .alpha(0)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(150)
                    .withEndAction(() -> {
                        contentView.setTranslationY(Utils.dp2Px(context, 10f) * animationAmplitude);
                        overlayView.setTranslationY(0f);
                        contentView.animate()
                                .translationY(0)
                                .setDuration(200)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    })
                    .start();

            // 布局监听
            View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    ellipse.startAnimSet();

                    v.removeOnLayoutChangeListener(this);
                }
            };
            ellipse.addOnLayoutChangeListener(layoutChangeListener);
            ellipse.setOnAnimSetEndListener(() -> {
                ellipse.removeOnLayoutChangeListener(layoutChangeListener);
                if (overlayView.getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) overlayView.getParent();
                    parent.removeView(overlayView);
                }

                if (mOnAnimSetEndListener != null) mOnAnimSetEndListener.onAnimSetEnd();
            });
        }
    }


    /**
     * 安全获取Context中的Activity
     */
    @Nullable
    private Activity getActivityFromContext(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getActivityFromContext(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }


    private boolean isContextContainActivity(Context context) {
        if (context == null) return false;
        if (context instanceof Activity) return true;
        if (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            return isContextContainActivity(baseContext);
        }
        return false;
    }


    public interface OnAnimSetEndListener {
        void onAnimSetEnd();
    }
}