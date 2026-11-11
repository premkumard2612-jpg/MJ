package com.boss.mj

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var keyBox: EditText
    private lateinit var nameBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("mj", Context.MODE_PRIVATE)
        val pad = (16 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(pad, pad * 2, pad, pad)

        val title = TextView(this)
        title.text = "MJ - Personal Assistant"
        title.textSize = 24f
        root.addView(title)

        val info = TextView(this)
        info.text = "Steps: 1) Key aur naam bharo  2) Overlay permission do  3) Battery restriction hatao  4) START dabao. Phir bolo: MJ, YouTube kholo"
        info.setPadding(0, pad / 2, 0, pad / 2)
        root.addView(info)

        keyBox = EditText(this)
        keyBox.hint = "Gemini API key paste karo"
        keyBox.setSingleLine(true)
        keyBox.setText(prefs.getString("key", ""))
        root.addView(keyBox)

        nameBox = EditText(this)
        nameBox.hint = "MJ aapko kya bulaye (sir / boss)"
        nameBox.setSingleLine(true)
        nameBox.setText(prefs.getString("call", "boss"))
        root.addView(nameBox)

        addButton(root, "Overlay permission do") {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
        addButton(root, "Battery restriction hatao") {
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
        addButton(root, "MJ START") { startMJ() }
        addButton(root, "MJ STOP") {
            stopService(Intent(this, MJService::class.java))
            Toast.makeText(this, "MJ band ho gaya", Toast.LENGTH_SHORT).show()
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun addButton(root: LinearLayout, label: String, action: () -> Unit) {
        val b = Button(this)
        b.text = label
        b.setOnClickListener { action() }
        root.addView(b)
    }

    private fun startMJ() {
        getSharedPreferences("mj", Context.MODE_PRIVATE).edit()
            .putString("key", keyBox.text.toString().trim())
            .putString("call", nameBox.text.toString().trim().ifEmpty { "boss" })
            .apply()

        val need = ArrayList<String>()
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            need.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (need.isNotEmpty()) {
            requestPermissions(need.toTypedArray(), 11)
            return
        }
        startForegroundService(Intent(this, MJService::class.java))
        Toast.makeText(this, "MJ chalu ho gaya. Ab MJ bolo", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(this, "Permission ke baad dobara START dabao", Toast.LENGTH_LONG).show()
    }
}
