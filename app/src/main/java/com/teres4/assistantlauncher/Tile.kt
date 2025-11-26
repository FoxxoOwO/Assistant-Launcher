package com.teres4.assistantlauncher

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class Tile : TileService() {

    override fun onClick() {
        super.onClick()

        // 1. Vytvoříme klasický Intent (jako předtím)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // 2. Zabalíme ho do PendingIntent (TOTO JE TA OPRAVA)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            // FLAG_IMMUTABLE je povinný pro Android 12+ (API 31+)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. Spustíme aktivitu a zavřeme lištu pomocí PendingIntent
        try {
            // Verze pro Android 14 a novější vyžaduje PendingIntent
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(pendingIntent)
            } else {
                // Starší verze (API 24-33) také podporují PendingIntent, takže to můžeme použít univerzálně
                startActivityAndCollapse(pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Chyba: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Vizuální odezva dlaždice (volitelné)
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.updateTile()

        // Hned ji zase vypneme (protože akce je jednorázová)
        // Použijeme postDelayed, aby uživatel viděl probliknutí
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (qsTile != null) {
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.updateTile()
            }
        }, 300)
    }
}