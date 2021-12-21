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


class MainActivity : AppCompatActivity() {
     private lateinit var binding: ActivityMainBinding

    companion object {
        const val BRIGHTNESS_NORMAL = -1F
        const val BRIGHTNESS_MAX = 1F
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setWinBrightness(BRIGHTNESS_MAX)
        var imageView = ImageView(this)
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                barcode.barcodeString,
                zxingBarcodeFormat.UPC_A,
                imageView.getWidth(),
                imageView.getHeight()
            )
            val bitmap = Bitmap.createBitmap(
                imageView.getWidth(),
                imageView.getHeight(),
                Bitmap.Config.RGB_565
            )
            for (i in 0 until imageView.getWidth()) {
                for (j in 0 until imageView.getHeight()) {
                    bitmap.setPixel(i, j, if (bitMatrix.get(i, j)) Color.BLACK else Color.WHITE)
                }
            }
            imageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
    fun addBarcode() {
        val size = binding.recyclerView.adapter!!.itemCount
        scanBarcode()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }
    fun scanBarcode() {
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
    fun createBarcodeObj(barcodeData: String, barcodeType: String) {
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
                        createBarcodeObj(barcodes[0].displayValue!!, barcodes[0].format.toString())
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
}

class BarcodeAdapter(private val dataSet: JSONArray) :
    RecyclerView.Adapter<BarcodeAdapter.ViewHolder>() {

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
            
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.length()

}

