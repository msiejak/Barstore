package com.msiejak.barstore;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ColoredBottomSheetDialog extends BottomSheetDialog {

    public ColoredBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();
        int color = getCurrentSurfaceColor(getContext());
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(color);
    }

    public int getCurrentSurfaceColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        return typedValue.getComplexUnit();
    }
}
