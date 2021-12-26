package com.msiejak.internal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

public class ManualFlagOverride extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyIfAvailable(this);
        setContentView(com.msiejak.internal.R.layout.activity_manual_flag_override);
    }

}
