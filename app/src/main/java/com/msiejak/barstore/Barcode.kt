package com.msiejak.barstore

import android.content.Context
import androidx.annotation.Nullable
import com.google.zxing.BarcodeFormat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class Barcode(
    barcodeString: String,
    barcodeType: Int,
    @Nullable name: String?,
    @Nullable time: String?
) {
    var barcodeString: String = "0"
    var name: String = "Unnamed"
    var type: Int = 0
    var time: String = "Unknown"

    init {
        this.barcodeString = barcodeString
        if (name != null) {
            this.name = name
        }
        this.type = barcodeType
        if (time != null) {
            this.time = time
        }
    }

    companion object {

        private fun createJsonFile(c: Context) {
            val file = File(c.filesDir, "barcodes.json")
            file.createNewFile()
            val json = "[]"
            val writer = FileWriter(file)
            writer.write(json)
            writer.close()
        }

        fun getBarcodeFormat(mlkitFormat: Int): BarcodeFormat {
            when (mlkitFormat) {
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> return BarcodeFormat.AZTEC
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> return BarcodeFormat.CODABAR
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> return BarcodeFormat.CODE_128
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> return BarcodeFormat.CODE_39
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> return BarcodeFormat.CODE_93
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> return BarcodeFormat.DATA_MATRIX
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> return BarcodeFormat.EAN_13
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> return BarcodeFormat.EAN_8
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> return BarcodeFormat.ITF
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> return BarcodeFormat.QR_CODE
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> return BarcodeFormat.UPC_A
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> return BarcodeFormat.UPC_E
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> return BarcodeFormat.PDF_417
                else -> return BarcodeFormat.UPC_A
            }
        }


        fun getJson(c: Context): JSONArray {
            try {
                return FileReader(File(c.filesDir, "barcodes.json")).use {
                    return@use JSONArray(it.readText())
                }
            } catch (e: Exception) {
                createJsonFile(c)
                return JSONArray("[]")
            }
        }

        fun getName(c: Context, i: Int): String {
            val json = getJson(c)
            return json.getJSONObject(i).get("name").toString()
        }

        fun deleteBarcode(c: Context, index: Int) {
            val json = getJson(c)
            json.remove(index)
            val writer = FileWriter(File(c.filesDir, "barcodes.json"))
            writer.write(json.toString())
            writer.close()
        }

        fun nameBarcode(c: Context, index: Int, newName: String) {
            val json = getJson(c)
            json.getJSONObject(index).put("name", newName)
            val writer = FileWriter(File(c.filesDir, "barcodes.json"))
            writer.write(json.toString())
            writer.close()
        }
    }

    fun storeBarcode(c: Context) {
        val json =
            "{\"data\":\"$barcodeString\",\"name\":\"$name\",\"type\":$type, \"time\":\"$time\"}"
        val jsonObj = JSONObject(json)
        if (File(c.filesDir, "barcodes.json").exists()) {
            val barcodes = JSONArray(File(c.filesDir, "barcodes.json").readText())
            barcodes.put(jsonObj)
            FileWriter(File(c.filesDir, "barcodes.json")).use {
                it.write(barcodes.toString())
            }
        }
    }

}