package com.unmi.hari.diffusion.animation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unmi.hari.diffusionanim.Diffusion;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        findViewById(R.id.btn).setOnClickListener(this::setClick);
    }

    private void setClick(View view) {
        Intent intent = new Diffusion()
                /*.setFromEvent(Arrays.asList("BActivity", "CActivity"))
                .setGradientInnerColor("#66FFFFFF")
                .setGradientOuterColor("#FFFFFF")
                .setCurrentGradientRadius(1f)
                .setGradientSpread(0.7f)
                .setScreenshotBitmap(null)*/
                .start(this, BActivity.class);
        if (intent != null) startActivity(intent);
        overridePendingTransition(0, 0);
    }
}