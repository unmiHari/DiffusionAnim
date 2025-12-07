package com.unmi.hari.diffusionanim;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DiffusionInfo implements Parcelable {

    public List<String> ignoredActivities;      // 忽略的界面
    public byte[] screenshotBytes;              // 截图
    public Object gradientInnerColor;           // 渐变扩散内的颜色
    public Object gradientOuterColor;           // 渐变扩散外的颜色
    public float gradientSpread;                // 渐变过渡范围因子
    public float currentGradientRadius;         // 梯度实际半径

    // 空构造
    public DiffusionInfo() {
    }

    // 带参构造
    public DiffusionInfo(List<String> ignoredActivities, byte[] aScreenshot,
                         Object gradientInnerColor, Object gradientOuterColor,
                         float gradientSpread, float currentGradientRadius) {
        this.ignoredActivities = ignoredActivities;
        this.screenshotBytes = aScreenshot;
        this.gradientInnerColor = gradientInnerColor;
        this.gradientOuterColor = gradientOuterColor;
        this.gradientSpread = gradientSpread;
        this.currentGradientRadius = currentGradientRadius;
    }

    // 从Parcel反序列化
    protected DiffusionInfo(Parcel in) {
        // 避免空指针
        ignoredActivities = new ArrayList<>();
        in.readStringList(ignoredActivities);

        // 读取截图
        screenshotBytes = in.createByteArray();

        // 读取Object类型
        gradientInnerColor = in.readValue(getClass().getClassLoader());
        gradientOuterColor = in.readValue(getClass().getClassLoader());

        // 读取浮点型
        gradientSpread = in.readFloat();
        currentGradientRadius = in.readFloat();
    }

    public static final Creator<DiffusionInfo> CREATOR = new Creator<DiffusionInfo>() {
        @Override
        public DiffusionInfo createFromParcel(Parcel in) {
            return new DiffusionInfo(in);
        }

        @Override
        public DiffusionInfo[] newArray(int size) {
            return new DiffusionInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    // 序列化
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeStringList(ignoredActivities);

        parcel.writeByteArray(screenshotBytes);

        parcel.writeValue(gradientInnerColor);
        parcel.writeValue(gradientOuterColor);

        parcel.writeFloat(gradientSpread);
        parcel.writeFloat(currentGradientRadius);
    }


    public DiffusionInfo setInfo(List<String> ignoredActivities, byte[] aScreenshot,
                                 Object gradientInnerColor, Object gradientOuterColor,
                                 float gradientSpread, float currentGradientRadius) {
        this.ignoredActivities = ignoredActivities;
        this.screenshotBytes = aScreenshot;
        this.gradientInnerColor = gradientInnerColor;
        this.gradientOuterColor = gradientOuterColor;
        this.gradientSpread = gradientSpread;
        this.currentGradientRadius = currentGradientRadius;
        return this;
    }
}