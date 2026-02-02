package com.iker.fortunecookie

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iker.fortunecookie.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var analytics: FirebaseAnalytics
    private var currentPhrase: String = ""
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances (EXACTLY as per requirements)
        auth = FirebaseAuth.getInstance()
        analytics = Firebase.analytics

        // Anonymous sign-in (EXACTLY as per requirements)
        signInAnonymously()

        // Set click listeners
        binding.imgCookie.setOnClickListener {
            showNewPhrase()
            logEvent("cookie_tap", "Tocado galleta para nueva frase")
        }

        binding.btnReload.setOnClickListener {
            showNewPhrase()
            logEvent("reload_phrase", "Recargar para ver otra frase") // Event d)
        }

        binding.btnCopy.setOnClickListener {
            copyPhrase()
            logEvent("copy_phrase", "Copiar frase al portapapeles") // Event c)
        }

        binding.btnShare.setOnClickListener {
            sharePhrase()
            logEvent("share_phrase", "Compartir frase") // Event b)
        }

        // Show initial phrase
        showNewPhrase()
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        userId = user?.uid
                        Log.d("FIREBASE_AUTH", "Anonymous sign-in successful. UID: $userId")

                        // Optional: Store UID in analytics user properties
                        val bundle = Bundle()
                        bundle.putString("user_id", userId)
                        analytics.logEvent("user_anonymous_login", bundle)
                    } else {
                        Log.e("FIREBASE_AUTH", "Anonymous sign-in failed", task.exception)
                    }
                }
        } else {
            userId = auth.currentUser?.uid
            Log.d("FIREBASE_AUTH", "User already signed in. UID: $userId")
        }
    }

    private fun showNewPhrase() {
        currentPhrase = getRandomPhrase()
        binding.txtFrase.text = currentPhrase

        // Firebase Analytics event a) - User opens cookie and sees phrase
        val bundle = Bundle().apply {
            putString("phrase", currentPhrase)
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent("open_cookie", bundle)

        Log.d("FIREBASE_ANALYTICS", "Event: open_cookie, Phrase: $currentPhrase")
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
            "Haz de cada día tu obra maestra.",
            "Los pequeños cambios pueden generar grandes resultados.",
            "El tiempo es el recurso más valioso, úsalo sabiamente.",
            "La creatividad es la inteligencia divirtiéndose.",
            "A veces perder es ganar la lección más importante.",
            "Tu actitud determina tu dirección."
        )
        return phrases.random()
    }

    private fun sharePhrase() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "🍪 Mi frase de la fortuna: $currentPhrase\n\nDescarga Fortune Cookie!")
        }
        startActivity(Intent.createChooser(intent, "Compartir frase"))

        // Firebase Analytics event b) - User shares phrase
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "share")
            putString("phrase", currentPhrase)
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent("share_phrase", bundle)

        Log.d("FIREBASE_ANALYTICS", "Event: share_phrase, Phrase: $currentPhrase")
        Toast.makeText(this, "Frase compartida ✓", Toast.LENGTH_SHORT).show()
    }

    private fun copyPhrase() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Frase de la fortuna", currentPhrase)
        clipboard.setPrimaryClip(clip)

        // Firebase Analytics event c) - User copies phrase to clipboard
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "copy")
            putString("phrase", currentPhrase)
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent("copy_phrase", bundle)

        Log.d("FIREBASE_ANALYTICS", "Event: copy_phrase, Phrase: $currentPhrase")
        Toast.makeText(this, "Frase copiada al portapapeles 📋", Toast.LENGTH_SHORT).show()
    }

    private fun logEvent(eventName: String, description: String) {
        val bundle = Bundle().apply {
            putString("event_description", description)
            putString("phrase", currentPhrase)
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent(eventName, bundle)
        Log.d("FIREBASE_ANALYTICS", "Event: $eventName, Description: $description")
    }

    override fun onResume() {
        super.onResume()
        // Log app open event
        val bundle = Bundle().apply {
            putString("screen", "MainActivity")
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
    }
}