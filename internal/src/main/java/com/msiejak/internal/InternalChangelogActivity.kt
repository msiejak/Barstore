package com.msiejak.internal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
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
                    "Internal Changelog (2.2)",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                UpdateCard(
                    releaseName = "Dogfood 1",
                    body = "• Material You redesign\n• New internal changelog built with Jetpack Compose\n• Bugfixes\n• Reduced spacing between cards"
                )
                UpdateCard(
                    releaseName = "Dogfood 2",
                    body = "• Material You redesign\n• Bugfixes\n• Replacing custom \"Toolbars\" and other Widgets with Material3 versions\n" +
                            "• Removed \"Hide Title when scrolling\" from settings\n• Updated casing in settings\n• Added swipe-away functionality with snackbars\n\nKnown bug: Only modules in course 1 currently work"
                )
                UpdateCard(
                    releaseName = "Dogfood 3",
                    body = "• Material You redesign\n• Bugfixes\n• Replacing custom \"Toolbars\" and other Widgets with Material3 versions\n" + "• UI improvements"
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
            backgroundColor = MaterialTheme.colors.surface, modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = releaseName, style = MaterialTheme.typography.h6)
                Text(text = body, style = MaterialTheme.typography.body2)
            }
        }
    }
}