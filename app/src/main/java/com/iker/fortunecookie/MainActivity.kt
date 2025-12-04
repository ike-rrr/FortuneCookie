package com.iker.fortunecookie

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class CookieActivity : AppCompatActivity() {


    private lateinit var imageCookie: ImageView
    private lateinit var textPhrase: TextView
    private lateinit var btnShare: ImageButton
    private lateinit var btnCopy: ImageButton
    private lateinit var btnReload: ImageButton
    private lateinit var phrases: Array<String>
    private var currentPhrase: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cookie)


// Inicialització de vistes
        imageCookie = findViewById(R.id.imageCookie)
        textPhrase = findViewById(R.id.textPhrase)
        btnShare = findViewById(R.id.btnShare)
        btnCopy = findViewById(R.id.btnCopy)
        btnReload = findViewById(R.id.btnReload)


// Llegim l'array de frases des de resources
        phrases = resources.getStringArray(R.array.cookie_phrases)


// Quan toquem la imatge s'obté i mostra una frase aleatòria
        imageCookie.setOnClickListener {
            showRandomPhrase()
        }


// Botó per obtenir una altra frase
        btnReload.setOnClickListener {
            showRandomPhrase()
        }


// Copiar al porta-retalls
        btnCopy.setOnClickListener {
        }