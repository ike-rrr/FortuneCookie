package com.iker.fortunecookie

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var analytics: FirebaseAnalytics

    private lateinit var imgCookie: ImageView
    private lateinit var txtFrase: TextView
    private lateinit var btnReload: ImageButton
    private lateinit var btnCopy: ImageButton
    private lateinit var btnShare: ImageButton

    private var currentPhrase: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase initialization
        auth = Firebase.auth
        analytics = FirebaseAnalytics.getInstance(this)

        // Anonymous sign-in
        signInAnonymously()

        // Initialize UI elements - MUST MATCH XML IDs
        imgCookie = findViewById(R.id.imgCookie)
        txtFrase = findViewById(R.id.txtFrase)  // Changed from tvPhrase
        btnReload = findViewById(R.id.btnReload)
        btnCopy = findViewById(R.id.btnCopy)
        btnShare = findViewById(R.id.btnShare)  // Changed from btnShare

        // Set click listeners
        imgCookie.setOnClickListener { showNewPhrase() }
        btnReload.setOnClickListener { showNewPhrase() }
        btnCopy.setOnClickListener { copyPhrase() }
        btnShare.setOnClickListener { sharePhrase() }

        // Show initial phrase
        showNewPhrase()
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    Log.d("AUTH", "Anonymous UID: $uid")
                } else {
                    Log.e("AUTH", "Anonymous sign-in failed", task.exception)
                }
            }
        }
    }

    private fun showNewPhrase() {
        currentPhrase = getRandomPhrase()
        txtFrase.text = currentPhrase  // Changed from tvPhrase to txtFrase

        // Firebase Analytics event
        val bundle = Bundle()
        bundle.putString("phrase", currentPhrase)
        analytics.logEvent("open_cookie", bundle)

        Toast.makeText(this, "Nueva frase generada!", Toast.LENGTH_SHORT).show()
    }

    private fun getRandomPhrase(): String {
        val phrases = listOf(
            "Hoy es un buen día para aprender algo nuevo.",
            "La paciencia es la clave del éxito.",
            "Confía en el proceso.",
            "Cada paso cuenta.",
            "El esfuerzo trae recompensa.",
            "La suerte favorece a los valientes.",
            "Un viaje de mil millas comienza con un solo paso.",
            "El conocimiento es poder.",
            "La sonrisa es el idioma universal de los inteligentes.",
            "Haz de cada día tu obra maestra."
        )
        return phrases.random()
    }

    private fun sharePhrase() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "🍪 Mi frase de la fortuna: $currentPhrase")
        }
        startActivity(Intent.createChooser(intent, "Compartir frase"))

        // Firebase Analytics event
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.METHOD, "share")
        bundle.putString("phrase", currentPhrase)
        analytics.logEvent("share_phrase", bundle)

        Toast.makeText(this, "Frase compartida ✓", Toast.LENGTH_SHORT).show()
    }

    private fun copyPhrase() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Frase de la fortuna", currentPhrase)
        clipboard.setPrimaryClip(clip)

        // Firebase Analytics event
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.METHOD, "copy")
        bundle.putString("phrase", currentPhrase)
        analytics.logEvent("copy_phrase", bundle)

        Toast.makeText(this, "Frase copiada al portapapeles 📋", Toast.LENGTH_SHORT).show()
    }
}