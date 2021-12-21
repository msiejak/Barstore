package com.msiejak.barstore

import android.content.Context
import androidx.annotation.Nullable
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class Barcode(barcodeString: String, barcodeType: String, @Nullable name: String?) {
    var barcodeString: String = "0"
    var name: String = "Unnamed"
    var type: String = "Unknown"

    init {
        this.barcodeString = barcodeString
        if (name != null) {
            this.name = name
        }
        this.type = barcodeType
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

        fun getJson(c: Context): JSONArray {
            try {
                return FileReader(File(c.filesDir, "barcodes.json")).use {
                    return@use JSONArray(it.readText())
                }
            }
      catch (e: Exception) {
          createJsonFile(c)
          return JSONArray("[]")
            }
        }
        }

    fun storeBarcode(c: Context) {
        val json = "{\"data\":\"$barcodeString\",\"name\":\"$name\",\"type\":\"$type\"}"
        val jsonObj = JSONObject(json)
        if(File(c.filesDir, "barcodes.json").exists()) {
            val barcodes = JSONArray(File(c.filesDir, "barcodes.json").readText())
            barcodes.put(jsonObj)
            FileWriter(File(c.filesDir, "barcodes.json")).use {
                it.write(barcodes.toString())
            }
        }else {

        }
    }

}