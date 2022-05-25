package com.msiejak.barstore

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
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
    private var prefs: SharedPreferences? = null

    companion object {
        const val BRIGHTNESS_NORMAL = -1F
        const val BRIGHTNESS_MAX = 1F
        const val KEEP_SCREEN_ON = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGE = 4
        const val CODE_UPC_A = 0
        const val CODE_UPC_E = 1
        const val CODE_INVALID = -1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        DynamicColors.applyIfAvailable(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fab.setOnClickListener {
            addBarcode()
        }
        refreshRecyclerView()
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
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
                R.id.settings -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            SettingsActivity::class.java
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

    private fun setWinBrightness(brightness: Float) {
        window.attributes.screenBrightness = brightness
        if (brightness == BRIGHTNESS_MAX) window.addFlags(KEEP_SCREEN_ON)
        else window.clearFlags(KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
    }

    private fun viewBarcode(barcode: Barcode, index: Int) {
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
                1000,
                280
            )
            val bitmap = Bitmap.createBitmap(
                1000,
                280,
                Bitmap.Config.RGB_565
            )
            for (i in 0 until 1000) {
                for (j in 0 until 280) {
                    bitmap.setPixel(i, j, if (bitMatrix.get(i, j)) Color.BLACK else Color.WHITE)
                }
            }
            imageView?.setImageBitmap(bitmap)
            sheetDialog?.findViewById<MaterialButton>(R.id.delete)?.setOnClickListener {
                Barcode.deleteBarcode(this@MainActivity, index)
                sheetDialog?.dismiss()
                Toast.makeText(this@MainActivity, "Barcode deleted", Toast.LENGTH_SHORT).show()
                refreshRecyclerView()
            }
            sheetDialog?.findViewById<MaterialButton>(R.id.edit)?.setOnClickListener {
                val editTextlLayout =
                    layoutInflater.inflate(R.layout.edittext, null) as TextInputLayout
                val editText = editTextlLayout.findViewById<TextInputEditText>(R.id.nameInput)
                editText.setText(Barcode.getName(this@MainActivity, index))
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.rename_barcode)
                    .setView(editTextlLayout)
                    .setPositiveButton(R.string.save) { a, _ ->
                        if (editText.text.toString().isNotEmpty()) {
                            Barcode.nameBarcode(this@MainActivity, index, editText.text.toString())
                            Toast.makeText(this@MainActivity, "Barcode renamed", Toast.LENGTH_SHORT)
                                .show()
                            refreshRecyclerView()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.empty_name,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        a.dismiss()
                        sheetDialog?.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            sheetDialog?.findViewById<MaterialButton>(R.id.search)?.setOnClickListener {
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, barcode.barcodeString)
                startActivity(intent)
            }
            sheetDialog?.findViewById<TextView>(R.id.timeData)?.text = barcode.time
            sheetDialog?.findViewById<TextView>(R.id.name)?.text = barcode.name
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
            sheetDialog?.setOnDismissListener { setWinBrightness(BRIGHTNESS_NORMAL) }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun addBarcode() {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle(R.string.new_barcode)
            .setMessage(R.string.chose_input)
            .setPositiveButton(R.string.scan) { _, _ ->
                scanBarcode()
            }
            .setNegativeButton(R.string.chose_image) { _, _ ->
                choseBarcode()
            }
            .setNeutralButton(R.string.manual) { _, _ ->
                enterBarcodeEntry()
            }
            .show()

    }

    private fun enterBarcodeEntry() {
        if (prefs == null) {
            prefs = getSharedPreferences("general", MODE_PRIVATE)
            enterBarcodeEntry()
        } else {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.manual)
                .setMessage(R.string.manual_warning)
                .setOnCancelListener {
                    enterBarcode()
                }.setPositiveButton(android.R.string.ok) { _, _ ->
                    enterBarcode()
                }.show()
        }
    }

    private fun validateCode(code: String): Int {
        var retV: Int
        try {
            MultiFormatWriter().encode(
                code,
                Barcode.getBarcodeFormat(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A),
                250,
                80
            )
            retV = CODE_UPC_A
        } catch (exc: Exception) {
            retV = try {
                MultiFormatWriter().encode(
                    code,
                    Barcode.getBarcodeFormat(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E),
                    250,
                    80
                )
                CODE_UPC_E
            } catch (exc: Exception) {
                CODE_INVALID
            }
        }
        return retV
    }

    private fun enterBarcode() {
        sheetDialog = BottomSheetDialog(this)
        sheetDialog!!.setContentView(R.layout.param_sheet)
        sheetDialog!!.findViewById<View>(R.id.textFieldCode)?.visibility = View.VISIBLE
        sheetDialog!!.findViewById<TextView>(R.id.message)?.text = getString(R.string.enter_barcode)
        sheetDialog!!.show()
        var barcode: Barcode
        sheetDialog!!.findViewById<MaterialButton>(R.id.submit)?.setOnClickListener {
            val data =
                sheetDialog!!.findViewById<TextInputEditText>(R.id.codeInput)?.text.toString()
            val name =
                sheetDialog!!.findViewById<TextInputEditText>(R.id.nameInput)?.text.toString()
            when (validateCode(data)) {
                CODE_UPC_A -> {
                    barcode = Barcode(
                        data,
                        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A,
                        name,
                        getCurrTime()
                    )
                    barcode.storeBarcode(this@MainActivity)
                    sheetDialog!!.dismiss()
                    refreshRecyclerView()
                }
                CODE_UPC_E -> {
                    barcode = Barcode(
                        data,
                        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E,
                        name,
                        getCurrTime()
                    )
                    barcode.storeBarcode(this@MainActivity)
                    sheetDialog!!.dismiss()
                    refreshRecyclerView()
                }
                CODE_INVALID -> {
                    Toast.makeText(this@MainActivity, "Invalid barcode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun choseBarcode() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        intent.putExtra("return-data", true)
        startActivityForResult(intent, PICK_IMAGE)
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
        val headerAdapter = HeaderAdapter()
        dataSet = Barcode.getJson(this@MainActivity)
        val barcodeAdapter = BarcodeAdapter(dataSet)
        val concatAdapter = ConcatAdapter(headerAdapter, barcodeAdapter)
        val rv = binding.recyclerView
        rv.adapter = concatAdapter

        if (binding.recyclerView.adapter?.itemCount == 0) {
            binding.emptyContainer.visibility = View.VISIBLE
        } else {
            binding.emptyContainer.visibility = View.GONE
        }
    }

    private fun createBarcodeObj(
        barcodeData: String,
        barcodeName: String,
        barcodeType: Int,
        time: String
    ) {
        val barcode = Barcode(barcodeData, barcodeType, barcodeName, time)
        barcode.storeBarcode(this@MainActivity)
        refreshRecyclerView()
    }

    private fun getCurrTime(): String {
        val dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
        val now = LocalDateTime.now()
        return dtf.format(now)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var image: Bitmap? = null
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            lifecycleScope.launch(Dispatchers.IO) {
                image = BitmapFactory.decodeFile(File(cacheDir, "image.png").path)
            }.invokeOnCompletion { processImage(image!!) }
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            lifecycleScope.launch(Dispatchers.IO) {
                val d = data?.data
                image = MediaStore.Images.Media.getBitmap(contentResolver, d)
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
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
        ).build()

        val scanner = BarcodeScanning.getClient(options)
        scanner.process(bitmap)
            .addOnSuccessListener { barcodes ->
                try {
                    val time = getCurrTime()
                    getName(barcodes[0], time)

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "No barcode found", Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
                if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "dogfood") {
                    saveImageToCache(image)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun getName(barcode: com.google.mlkit.vision.barcode.common.Barcode, time: String) {
        sheetDialog = BottomSheetDialog(this)
        sheetDialog!!.setContentView(R.layout.param_sheet)
        sheetDialog!!.show()
        sheetDialog!!.findViewById<Button>(R.id.submit)?.setOnClickListener {
            val name = sheetDialog!!.findViewById<EditText>(R.id.nameInput)!!.text.toString()
            if (name.isNotEmpty()) {
                createBarcodeObj(barcode.rawValue!!, name, barcode.format, time)
                sheetDialog!!.dismiss()
            } else {
                Toast.makeText(this@MainActivity, R.string.empty_name, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToCache(image: Bitmap) {
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.name)
        val root: MaterialCardView = view.findViewById(R.id.root)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.barcode_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val json: JSONArray = dataSet
        val jsonObj = json.getJSONObject(position)
        var time = "Unknown"
        try {
            time = jsonObj.get("time").toString()
        } catch (e: Exception) {
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

    override fun getItemCount() = dataSet.length()

}

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view){
    private val v = view
    private val c  = v.context
    private var adLoaded = false
    fun bind() {
        if(!adLoaded) {
            Log.e("bind() called", "bind: ",)
            val parent = v.findViewById<MaterialCardView>(R.id.ad)
            val loadingContainer = v.findViewById<FrameLayout>(R.id.ad_load)
            val adLoader = AdLoader.Builder(c, BuildConfig.ADMOB_AD_ID)
                .forNativeAd { nativeAd: NativeAd ->
                    val styles = NativeTemplateStyle.Builder()
                        .build()
                    val template = v.findViewById<TemplateView>(R.id.my_template)
                    template.setStyles(styles)
                    template.visibility = View.VISIBLE
                    loadingContainer.removeAllViews()
                    template.setNativeAd(nativeAd)
                    adLoaded = true
                }
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            loadingContainer.removeAllViews()
                            parent.visibility = View.GONE
                        }
                    })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setRequestMultipleImages(false)
                        .build()
                )
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }
}

class HeaderAdapter: RecyclerView.Adapter<HeaderViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.native_ad, parent, false)
        return HeaderViewHolder(view)
    }


    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind()
    }

}

