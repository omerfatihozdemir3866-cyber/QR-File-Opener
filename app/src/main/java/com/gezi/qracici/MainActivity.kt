package com.gezi.qracici

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var scanResultText: TextView

    // QR tarama sonucu icin launcher
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // Taranan metni ekranda goster (dogrulama / bilgi amacli)
            scanResultText.text = result.contents
        } else {
            scanResultText.text = "Tarama iptal edildi."
        }
    }

    // PDF secme sonucu icin launcher (Android'in kendi dosya secici arayuzu)
    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            openPdfWithChooser(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanResultText = findViewById(R.id.scanResultText)
        val btnScan: Button = findViewById(R.id.btnScan)
        val btnPickPdf: Button = findViewById(R.id.btnPickPdf)

        btnScan.setOnClickListener {
            startQrScan()
        }

        btnPickPdf.setOnClickListener {
            // Sadece PDF dosyalarini listelemek icin filtre
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        }
    }

    private fun startQrScan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Gezi QR kodunu kameraya gosterin")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        qrLauncher.launch(options)
    }

    private fun openPdfWithChooser(uri: Uri) {
        // Secilen PDF icin kalici okuma izni al (bir sonraki acilista da calissin diye)
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // izin alinamazsa yine de acmayi dene
        }

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Kullanicinin hangi uygulamayla acacagini kendisinin secmesi icin "chooser"
        val chooser = Intent.createChooser(viewIntent, "PDF'i su uygulamayla ac:")
        startActivity(chooser)
    }
}
