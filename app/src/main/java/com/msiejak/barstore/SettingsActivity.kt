package com.msiejak.barstore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.color.DynamicColors
import com.msiejak.barstore.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        DynamicColors.applyIfAvailable(this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        DynamicColors.applyIfAvailable(this)
        setContentView(binding.root)
        DynamicColors.applyIfAvailable(this)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.licenses -> {
                    startActivity(
                        Intent(
                            this@SettingsActivity,
                            OssLicensesMenuActivity::class.java
                        )
                    )
                    true
                }
                R.id.debugMenu -> {
                    startActivity(
                        Intent(
                            this@SettingsActivity,
                            Class.forName("com.msiejak.internal.ManualFlagOverride")
                        )
                    )
                    true
                }
                R.id.changelog -> {
                    openUrl("https://msiejak.dev/barstore/changelog")
                    true
                }
                R.id.appInfo -> {
                    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    i.addCategory(Intent.CATEGORY_DEFAULT)
                    i.data = Uri.parse("package:$packageName")
                    startActivity(i)
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.devContact.setOnClickListener {
            openUrl("mailto:contact@msiejak.dev")
        }

    }

    private fun openUrl(url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }
}