package com.msiejak.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ManualFlagOverride extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyIfAvailable(this);
        setContentView(com.msiejak.internal.R.layout.activity_manual_flag_override);

        findViewById(R.id.dummyData).setOnClickListener(v -> {
            try {
                InputStream is = getAssets().open("barcode-data/barcodes.json");
                File file = new File(getFilesDir(), "barcodes.json");
                file.delete();
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                for (int result = bis.read(); result != -1; result = bis.read()) {
                    buf.write((byte) result);
                }
                fileWriter.write(buf.toString("UTF-8"));
                fileWriter.close();
                is.close();
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.exportData).setOnClickListener(v -> {
            createFile();
        });
    }

    private static final int CREATE_FILE = 1;

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "barcodes.json");
        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == RESULT_OK) {
            try {
                File source = new File(getFilesDir(), "barcodes.json");
                Uri uri = data.getData();
                InputStream is;
                OutputStream os;
                is = new FileInputStream(source);
                os = getContentResolver().openOutputStream(uri);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
                Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


