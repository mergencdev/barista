package com.mergenc.barista.ui.cashier

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mergenc.barista.R
import com.mergenc.barista.databinding.ActivityCashierBinding
import java.io.IOException

class CashierActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCashierBinding

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private var selectedPrice: Int = 0
    private lateinit var qrCodeIdFirebase: String
    private lateinit var qrCodeIdFromGeneration: String

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashierBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        qrCodeIdFromGeneration = intent.getStringExtra("qrCodeId").toString()

        if (ContextCompat.checkSelfPermission(
                this@CashierActivity, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }

        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this@CashierActivity, R.anim.scanner_animation)
        binding.barcodeLine.startAnimation(aniSlide)
    }

    private fun retrieveIdFromDatabase() {
        database = FirebaseDatabase.getInstance().getReference("qrCodeIds")
        database.get().addOnSuccessListener {
            if (it.exists()) {
                qrCodeIdFirebase = it.child("id").value as String
                if (scannedValue == qrCodeIdFirebase) {
                    //Toast.makeText(this, "QR ID: $qrCodeIdFirebase", Toast.LENGTH_SHORT).show()
                    showPriceDialog()
                } else {
                    Toast.makeText(this, "QR kod tanımlanamadı.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "QR ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPriceDialog() {
        val mDialogView = LayoutInflater.from(this@CashierActivity)
            .inflate(R.layout.custom_price_dialog, null)

        val mBuilder =
            AlertDialog.Builder(this@CashierActivity).setView(mDialogView).show()
        mBuilder.window?.setBackgroundDrawableResource(android.R.color.transparent)


        val editTextNumber = mDialogView.findViewById<EditText>(R.id.editTextNumber)
        val radioGroupPrice = mDialogView.findViewById<RadioGroup>(R.id.rg_price)
        val buttonOkay = mDialogView.findViewById<Button>(R.id.button_okay)
        val buttonCancel = mDialogView.findViewById<Button>(R.id.button_cancel)

        radioGroupPrice.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = mDialogView.findViewById(checkedId)

            when (checkedId) {
                R.id.rb_15 -> {
                    editTextNumber.visibility = View.GONE
                    selectedPrice = 15
                }
                R.id.rb_30 -> {
                    editTextNumber.visibility = View.GONE
                    selectedPrice = 30
                }
                R.id.rb_50 -> {
                    editTextNumber.visibility = View.GONE
                    selectedPrice = 50
                }
                R.id.rb_other -> {
                    editTextNumber.visibility = View.VISIBLE
                    //selectedPrice = editTextNumber.text.toString().toInt()
                }
            }
        }
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue

                    runOnUiThread {
                        // okuma isleminden sonra yapilacak islem (qr kodun detaylari)
                        cameraSource.stop()
                        binding.barcodeLine.clearAnimation()
                        Log.d("QRCode", "QR Code: $scannedValue")

                        retrieveIdFromDatabase()
                    }
                } else {
                    Toast.makeText(this@CashierActivity, "value- else", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@CashierActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }
}