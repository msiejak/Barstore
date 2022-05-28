package com.msiejak.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ManualFlagOverride extends AppCompatActivity {

    private static final int CREATE_FILE = 1;
    private static final int PICK_JSON_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyIfAvailable(this);
        setContentView(com.msiejak.internal.R.layout.activity_manual_flag_override);

        findViewById(R.id.importData).setOnClickListener(v -> importData());

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


        findViewById(R.id.exportData).setOnClickListener(v -> createFile());
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "barcodes.json");
        startActivityForResult(intent, CREATE_FILE);
    }

    private void importData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, PICK_JSON_FILE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == RESULT_OK) {
            try {
                File source = new File(getFilesDir(), "barcodes.json");
                Uri uri = data.getData();
                InputStream is = new FileInputStream(source);
                OutputStream os;
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
        } else if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
            try {
                FileOutputStream fos = openFileOutput("barcodes.json", MODE_PRIVATE);
                InputStream is = getIsFromUri(data.getData());
                byte[] buf = new byte[8192];
                int length;
                while ((length = is.read(buf)) > 0) {
                    fos.write(buf, 0, length);
                }
                is.close();
                fos.close();
                Toast.makeText(this, "Data imported", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private InputStream getIsFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        return inputStream;
    }


}


