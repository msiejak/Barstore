package com.msiejak.barstore

import android.Manifest
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.jayway.jsonpath.JsonPath
import com.msiejak.barstore.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), BarcodeAdapter.ViewBarcode {
    private lateinit var binding: ActivityMainBinding
    private var sheetDialog: BottomSheetDialog? = null
    private lateinit var dataSet: JSONArray

    companion object {
        const val BRIGHTNESS_NORMAL = -1F
        const val BRIGHTNESS_MAX = 1F
        const val REQUEST_IMAGE_CAPTURE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyIfAvailable(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fab.setOnClickListener {
            addBarcode()
        }
        dataSet = Barcode.getJson(this@MainActivity)
        binding.recyclerView.adapter = BarcodeAdapter(dataSet)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.licenses -> {
                    startActivity(Intent(this@MainActivity, OssLicensesMenuActivity::class.java))
                    true
                }
                R.id.clearAll -> {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(R.string.clear_all)
                        .setMessage(R.string.clear_all_confirmation)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            File(filesDir, "barcodes.json").delete()
                            refreshRecyclerView()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                R.id.internalChangelog -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            Class.forName("com.msiejak.internal.InternalChangelogActivity")
                        )
                    )
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    fun setWinBrightness(brightness: Float) {
        window.attributes.screenBrightness = brightness
        window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
    }

    fun viewBarcode(barcode: Barcode, index: Int) {
        sheetDialog = BottomSheetDialog(this)
        sheetDialog!!.setContentView(R.layout.code_sheet)
        sheetDialog!!.show()
        setWinBrightness(BRIGHTNESS_MAX)
        val imageView: ImageView? = sheetDialog!!.findViewById(R.id.image)
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                barcode.barcodeString,
                Barcode.getBarcodeFormat(barcode.type),
                250,
                80
            )
            val bitmap = Bitmap.createBitmap(
                250,
                80,
                Bitmap.Config.RGB_565
            )
            for (i in 0 until 250) {
                for (j in 0 until 80) {
                    bitmap.setPixel(i, j, if (bitMatrix.get(i, j)) Color.BLACK else Color.WHITE)
                }
            }
            imageView?.setImageBitmap(bitmap)
            sheetDialog?.findViewById<MaterialButton>(R.id.delete)?.setOnClickListener {
                Barcode.deleteBarcode(this@MainActivity, index)
                Toast.makeText(this@MainActivity, "Barcode deleted", Toast.LENGTH_SHORT).show()
                refreshRecyclerView()
            }
            sheetDialog?.findViewById<MaterialButton>(R.id.search)?.setOnClickListener {
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, barcode.barcodeString)
                startActivity(intent)
            }
            sheetDialog?.findViewById<TextView>(R.id.timeData)?.text = barcode.time
            sheetDialog?.findViewById<MaterialButton>(R.id.share)?.setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "image/*"
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageUri =
                    android.net.Uri.parse(
                        MediaStore.Images.Media.insertImage(
                            contentResolver,
                            bitmap,
                            "",
                            ""
                        )
                    )
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                shareIntent.putExtra(Intent.EXTRA_TEXT, barcode.barcodeString)
                try {
                    startActivity(Intent.createChooser(shareIntent, "Share barcode"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@MainActivity, "No app to share", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            sheetDialog?.findViewById<TextView>(R.id.codeData)?.text = barcode.barcodeString
            sheetDialog?.setOnCancelListener { setWinBrightness(BRIGHTNESS_NORMAL) }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun addBarcode() {
        scanBarcode()
    }

    private fun scanBarcode() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val outputFileUri = File(cacheDir?.path, "image.png")
        outputFileUri.createNewFile()

        intent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            FileProvider.getUriForFile(
                applicationContext,
                applicationContext.packageName + ".provider",
                outputFileUri
            )
        )
        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            //no camara app found
        }
    }

    private fun refreshRecyclerView() {
        val rv = binding.recyclerView
        dataSet = Barcode.getJson(this@MainActivity)
        rv.adapter = BarcodeAdapter(dataSet)
        if(binding.recyclerView.adapter?.itemCount == 0) {
            binding.emptyContainer.visibility = View.VISIBLE
        }else {
            binding.emptyContainer.visibility = View.GONE
        }
    }

    fun createBarcodeObj(barcodeData: String, barcodeName: String, barcodeType: Int, time: String) {
        val barcode = Barcode(barcodeData, barcodeType, barcodeName, time)
        barcode.storeBarcode(this@MainActivity)
        refreshRecyclerView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var image: Bitmap? = null
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            lifecycleScope.launch(Dispatchers.IO) {
                image = BitmapFactory.decodeFile(File(cacheDir, "image.png").path)
            }.invokeOnCompletion { processImage(image!!) }

        }
    }

    private fun processImage(image: Bitmap) {
        val bitmap = InputImage.fromBitmap(image, 0)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E,
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417,
        ).build()

        val scanner = BarcodeScanning.getClient(options)
        scanner.process(bitmap)
            .addOnSuccessListener { barcodes ->
                try {
                    var time = "Unknown"
//                    if (Build.VERSION.SDK_INT >= 29) {
//
//                    }else {
                        fun getCurrTime(): String {
                            val dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
                            val now = LocalDateTime.now()
                            return dtf.format(now)
                        }
                        time = getCurrTime()
                        getName(barcodes[0], time)
//                    }

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "No barcode found", Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
                if (BuildConfig.DEBUG) {
                    saveImageToCache(image)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun getName(barcode: com.google.mlkit.vision.barcode.common.Barcode, time: String) {
        sheetDialog = BottomSheetDialog(this)
        sheetDialog!!.setContentView(R.layout.param_sheet)
        sheetDialog!!.show()
        sheetDialog!!.findViewById<Button>(R.id.submit)?.setOnClickListener {
            val name = sheetDialog!!.findViewById<EditText>(R.id.nameInput)!!.text.toString()
            createBarcodeObj(barcode.rawValue!!, name, barcode.format, time)
            sheetDialog!!.dismiss()
        }
    }

    fun saveImageToCache(image: Bitmap) {
        val file = File(externalCacheDir, "${System.currentTimeMillis()}.png")
        file.createNewFile()
        val fileOutputStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        Toast.makeText(
            this@MainActivity,
            "(debug) saved image to external cache",
            Toast.LENGTH_LONG
        ).show()
    }


    override fun view(barcode: Barcode, position: Int) {
        viewBarcode(barcode, position)
    }
}

class BarcodeAdapter(private val dataSet: JSONArray) :
    RecyclerView.Adapter<BarcodeAdapter.ViewHolder>() {


    interface ViewBarcode {

        fun view(barcode: Barcode, position: Int)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val root: MaterialCardView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.name)
            root = view.findViewById(R.id.root)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.barcode_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val json: JSONArray = dataSet
        val jsonObj = json.getJSONObject(position)
        var time = "Unknown"
        try {
            time = jsonObj.get("time").toString()
        }catch(e: Exception) {
            e.printStackTrace()
        }
//        Toast.makeText(viewHolder.root.context, JsonPath.read<JSONArray>(dataSet, "$[*].name").toString(), Toast.LENGTH_LONG).show()

        viewHolder.textView.text = jsonObj.get("name").toString()
        viewHolder.root.setOnClickListener {
            val i = it.context as ViewBarcode
            i.view(
                Barcode(
                    jsonObj.get("data").toString(),
                    jsonObj.get("type") as Int,
                    jsonObj.get("name").toString(),
                    time
                ), position
            )
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.length()

}

