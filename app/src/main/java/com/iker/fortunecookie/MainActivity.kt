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
import com.google.firebase.ktx.Firebase
import com.iker.fortunecookie.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var analytics: FirebaseAnalytics
    private var currentPhrase: String = ""
    private var userId: String? = null

    // VARIABLES ADMOB
    private var rewardedAd: RewardedAd? = null
    private val TAG = "AdMob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        analytics = Firebase.analytics

        signInAnonymously()

        // Inicializar AdMob
        MobileAds.initialize(this) {}
        loadRewardedAd()

        binding.imgCookie.setOnClickListener {
            showNewPhrase()
            logEvent("cookie_tap", "Tocado galleta para nueva frase")
        }

        // MODIFICADO → Ahora muestra anuncio antes de recargar
        binding.btnReload.setOnClickListener {

            rewardedAd?.let { ad ->

                ad.show(this) {
                    Log.d(TAG, "User earned reward.")
                    showNewPhrase()
                }

            } ?: run {

                Log.d(TAG, "Ad not ready.")
                showNewPhrase()
                loadRewardedAd()
            }
        }

        binding.btnCopy.setOnClickListener {
            copyPhrase()
            logEvent("copy_phrase", "Copiar frase al portapapeles")
        }

        binding.btnShare.setOnClickListener {
            sharePhrase()
            logEvent("share_phrase", "Compartir frase")
        }

        showNewPhrase()
    }

    // FUNCIÓN CARGA ANUNCIO
    private fun loadRewardedAd() {

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad

                    rewardedAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad dismissed.")
                                rewardedAd = null
                                loadRewardedAd() // 🔁 Pre-carga automática
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                Log.d(TAG, "Ad failed to show.")
                                rewardedAd = null
                                loadRewardedAd()
                            }
                        }
                }
            }
        )
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        userId = user?.uid
                        Log.d("FIREBASE_AUTH", "Anonymous sign-in successful. UID: $userId")

                        val bundle = Bundle()
                        bundle.putString("user_id", userId)
                        analytics.logEvent("user_anonymous_login", bundle)
                    } else {
                        Log.e("FIREBASE_AUTH", "Anonymous sign-in failed", task.exception)
                    }
                }
        } else {
            userId = auth.currentUser?.uid
        }
    }

    private fun showNewPhrase() {
        currentPhrase = getRandomPhrase()
        binding.txtFrase.text = currentPhrase

        val bundle = Bundle().apply {
            putString("phrase", currentPhrase)
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent("open_cookie", bundle)
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

        Toast.makeText(this, "Frase compartida ✓", Toast.LENGTH_SHORT).show()
    }

    private fun copyPhrase() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Frase de la fortuna", currentPhrase)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Frase copiada al portapapeles 📋", Toast.LENGTH_SHORT).show()
    }

    private fun logEvent(eventName: String, description: String) {
        val bundle = Bundle().apply {
            putString("event_description", description)
            putString("phrase", currentPhrase)
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent(eventName, bundle)
    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle().apply {
            putString("screen", "MainActivity")
            userId?.let { putString("user_id", it) }
        }
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
    }
}