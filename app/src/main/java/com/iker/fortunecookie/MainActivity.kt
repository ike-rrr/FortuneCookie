package com.iker.fortunecookie

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build

class MainActivity : AppCompatActivity() {

    private lateinit var frases: Array<String>
    private lateinit var mp: MediaPlayer
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cargar array de frases desde strings.xml
        frases = resources.getStringArray(R.array.frases_cookie)

        // Inicializar sonido y vibración
        mp = MediaPlayer.create(this, R.raw.cookie)
        vibrator = getSystemService(Vibrator::class.java)

        // Referencias de vistas
        val imgCookie = findViewById<ImageView>(R.id.imgCookie)
        val txtFrase = findViewById<TextView>(R.id.txtFrase)
        val btnReload = findViewById<ImageButton>(R.id.btnReload)
        val btnCopy = findViewById<ImageButton>(R.id.btnCopy)
        val btnShare = findViewById<ImageButton>(R.id.btnShare)

        // Asignar iconos públicos de Android a los botones
        btnReload.setImageResource(android.R.drawable.ic_menu_rotate)
        btnCopy.setImageResource(android.R.drawable.ic_menu_agenda)
        btnShare.setImageResource(android.R.drawable.ic_menu_send)

        // Función para mostrar una frase aleatoria
        fun mostrarFraseAleatoria() {
            txtFrase.text = frases[Random.nextInt(frases.size)]
        }

        // Tocar galleta → sonido + vibración + frase
        imgCookie.setOnClickListener {

            // Sonido
            mp.start()

            // Vibración corta (100 ms) compatible con API 24+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }

            mostrarFraseAleatoria()
        }

        // Botón recargar → nueva frase
        btnReload.setOnClickListener {
            mostrarFraseAleatoria()
        }

        // Botón copiar → portapapeles
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("frase", txtFrase.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        // Botón compartir → Intent
        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, txtFrase.text.toString())
            startActivity(Intent.createChooser(intent, "Compartir frase"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mp.release()
    }
}
