package com.msiejak.barstore

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import org.json.JSONObject

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
        val jsonObj: JSONObject =
            if (viewHolder.root.context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .getBoolean("order", false)
            ) {
                json.getJSONObject(json.length() - (position + 1))
            } else {
                json.getJSONObject(position)
            }
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

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val v = view
    private val c = v.context
    private var adLoaded = false
    fun bind() {
        if (!adLoaded) {
            Log.e("bind() called", "bind: ")
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
                    parent.removeView(loadingContainer)
                    template.setNativeAd(nativeAd)
                    adLoaded = true
                }
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            loadingContainer.removeAllViews()
                            parent.visibility = View.GONE
                            Log.e(adError.toString(), "onAdFailedToLoad: ")
                        }
                    })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setRequestMultipleImages(false)
                        .build()
                )
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
            Log.e("laoded ad", "bind: ")
        }
    }
}

class HeaderAdapter : RecyclerView.Adapter<HeaderViewHolder>() {
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