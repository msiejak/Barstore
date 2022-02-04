package com.msiejak.internal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.color.DynamicColors
import com.google.android.material.composethemeadapter.MdcTheme

class InternalChangelogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyIfAvailable(this)
        setTheme(com.google.android.material.R.style.Theme_Material3_DayNight)
        DynamicColors.applyIfAvailable(this)
        setContentView(R.layout.compose_root)
        DynamicColors.applyIfAvailable(this)
        findViewById<ComposeView>(R.id.root).setContent { App() }
    }

    fun openLink(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    @Composable
    fun App() {
        MdcTheme(this) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    "Internal Changelog",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                UpdateCard(
                    releaseName = "1.0.0 Dogfood 1 (4)",
                    body = "• Added settings\n• Moved around toolbar menu options\n• Added manual barcode entry (UPC-E and UPC-A only)\n• Made barcode text selectable\n• Added barcode name in sheet\n• Increased resolution of barcode images by 4X\n• Changed barcode image aspect ratio\n• Added max name length (21 characters)\n• Added the ability to change names\n" +
                            "• The screen will no longer fall asleep when viewing a barcode\n" +
                            "• The sheet is now closed after a barcode is deleted\n• Fixed a bug in which the screen would stay at max brightness in some circumstances"

                )
                UpdateCard(
                    releaseName = "0.0 (3)",
                    body = "• UI Bugfixes\n• Added internal changelog\n• Added message when there are no barcodes stored\n• Added backup rules\n• Added option to clear all barcodes\n" +
                            "• Added debug menu (temporary settings)\n• Added option to pick image from gallery\n" +
                            "• Added Android Jetpack SplashScreen\n• Added Crashlytics"

                )
                Row {
                    Button(
                        onClick = { openLink("https://bugs.msiejak.dev") }, modifier = Modifier
                            .weight(.5f)
                            .padding(end = 2.dp)
                    ) {
                        Text("Report a bug")
                    }
                    Button(
                        onClick = { openLink("https://t.me/msiejak") }, modifier = Modifier
                            .weight(.5f)
                            .padding(start = 2.dp)
                    ) {
                        Text("Developer Contact")
                    }
                }
            }
        }

    }

    @Composable
    fun UpdateCard(releaseName: String, body: String) {
        Card(
            backgroundColor = MaterialTheme.colorScheme.surface, modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = releaseName, style = MaterialTheme.typography.headlineSmall)
                Text(text = body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}