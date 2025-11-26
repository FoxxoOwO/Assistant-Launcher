package com.teres4.assistantlauncher // Vaše package name

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                waitAndExecuteCommand()
            } else {
                finish() // Pokud nedá práva, ukončit
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DŮLEŽITÉ: Smazal jsem setContentView(R.layout.activity_main)
        // Nic se nebude vykreslovat.

        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        checkAndRun()
    }

    private fun checkAndRun() {
        if (Shizuku.pingBinder() && checkShizukuPermission()) {
            waitAndExecuteCommand()
        } else if (Shizuku.pingBinder()) {
            // Pokud nemáme práva, musíme je vyžádat (to zobrazí systémové okno, což je OK)
            Shizuku.requestPermission(0)
        } else {
            Toast.makeText(this, "Shizuku neběží", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkShizukuPermission(): Boolean {
        return if (Shizuku.isPreV11()) false else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    private fun waitAndExecuteCommand() {
        // Tady nic nezobrazujeme, jen čekáme na pozadí
        Handler(Looper.getMainLooper()).postDelayed({
            executeCommand()
        }, 1000)
    }

    private fun executeCommand() {
        try {
            val command = arrayOf("sh", "-c", "input keyevent 219")

            // Reflexe pro jistotu (jak jsme řešili minule)
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, command, null, null) as Process

            process.waitFor()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // DŮLEŽITÉ: Ať se stane cokoliv, aplikaci teď ukončíme
            // Protože je transparentní, uživatel jen uvidí, že se "nic nestalo",
            // ale příkaz se provedl.
            finishAndRemoveTask()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}