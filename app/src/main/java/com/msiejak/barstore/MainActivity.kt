package com.msiejak.barstore

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.barhopper.RecognitionOptions.CODE_128
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.color.DynamicColors
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode.BarcodeFormat
import com.google.mlkit.vision.common.InputImage
import com.msiejak.barstore.databinding.ActivityMainBinding
import org.json.JSONArray
import com.google.zxing.BarcodeFormat as zxingBarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix


class MainActivity : AppCompatActivity(), BarcodeAdapter.ViewBarcode {
     private lateinit var binding: ActivityMainBinding
     private var sheetDialog: BottomSheetDialog? = null

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
        binding.recyclerView.adapter = BarcodeAdapter(Barcode.getJson(this@MainActivity))
    }

    fun setWinBrightness(brightness: Float) {
        window.attributes.screenBrightness = brightness
        window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
    }
    fun viewBarcode(barcode: Barcode) {
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
            sheetDialog?.findViewById<TextView>(R.id.codeData)?.text = barcode.barcodeString
            sheetDialog?.setOnCancelListener { setWinBrightness(BRIGHTNESS_NORMAL) }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
    private fun addBarcode() {
        val size = binding.recyclerView.adapter!!.itemCount
        scanBarcode()
    }
    private fun scanBarcode() {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                //no camara app found
            }
    }
    fun manualBarcodeEntry() {

    }
    fun getBarcodeList() {

    }
    fun displayBarcodeList() {

    }
    fun createBarcodeObj(barcodeData: String, barcodeType: Int) {
        val barcode = Barcode(barcodeData, barcodeType, null)
        barcode.storeBarcode(this@MainActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            processImage(imageBitmap)
        }
    }

    private fun processImage(image: Bitmap) {
//        val image = InputImage.fromBitmap(BitmapFactory.decodeStream(assets.open("barcode.jpg")), 90)
        val bitmap = InputImage.fromBitmap(image, 0)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
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
            )
            .build()

        val scanner = BarcodeScanning.getClient(options)
        val result = scanner.process(bitmap)
            .addOnSuccessListener { barcodes ->
                    try {
                        Toast.makeText(this@MainActivity, barcodes[0].displayValue, Toast.LENGTH_LONG).show()
                        createBarcodeObj(barcodes[0].displayValue!!, barcodes[0].format)
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                        binding.recyclerView.invalidate()
                    }catch(e: Exception) {
                        Toast.makeText(this@MainActivity, "No barcode found (succ)", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            .addOnFailureListener { exception ->
                    Toast.makeText(this@MainActivity, "No barcode found (exc)", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
            }
    }


    override fun view(barcode: Barcode) {
        viewBarcode(barcode)
    }
}

class BarcodeAdapter(private val dataSet: JSONArray) :
    RecyclerView.Adapter<BarcodeAdapter.ViewHolder>() {


    interface ViewBarcode {
        fun view(barcode: Barcode)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.name)

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

        viewHolder.textView.text = jsonObj.get("data").toString()
        viewHolder.textView.setOnClickListener {
            val i = it.context as ViewBarcode
            i.view(Barcode(jsonObj.get("data").toString(), jsonObj.get("type") as Int, jsonObj.get("name").toString()))
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.length()

}

