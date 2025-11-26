package com.teres4.assistantlauncher

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    // Listener pro výsledek žádosti o oprávnění
    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                // Pokud uživatel povolí, spustíme akci
                waitAndExecuteCommand()
            } else {
                Toast.makeText(this, "Shizuku permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Shizuku listener musíme zaregistrovat
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

        checkAndRun()
    }

    private fun checkAndRun() {
        // 1. Zkontrolujeme, zda Shizuku server běží
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "Shituku is not running!", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Zkontrolujeme oprávnění
        if (checkShizukuPermission()) {
            waitAndExecuteCommand()
        } else {
            // 3. Pokud nemáme oprávnění, požádáme o něj
            requestShizukuPermission()
        }
    }

    private fun checkShizukuPermission(): Boolean {
        return if (Shizuku.isPreV11()) {
            false // Staré verze neřešíme, předpokládáme v11+
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestShizukuPermission() {
        if (Shizuku.isPreV11()) {
            // Staré verze
        } else {
            Shizuku.requestPermission(0)
        }
    }

    private fun waitAndExecuteCommand() {

        Handler(Looper.getMainLooper()).postDelayed({
            executeCommand()
        }, 1000)
    }

    private fun executeCommand() {
        try {
            // Vytvoření procesu přes Shizuku (běží pod ADB shell userem)
            // "sh", "-c" je standardní způsob spuštění shell příkazu
            val process = Shizuku.newProcess(arrayOf("sh", "-c", "input keyevent 219"), null, null)

            process.waitFor()

            // Volitelné: Ukončit aplikaci po úspěchu
            // finish()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Odregistrování listeneru, aby nedocházelo k memory leakům
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}