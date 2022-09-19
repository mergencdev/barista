package com.mergenc.barista.ui.cashier

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.mergenc.barista.R
import com.mergenc.barista.databinding.ActivityCashierBinding
import com.mergenc.barista.network.analytics.AnalyticsEventConstants
import com.mergenc.barista.network.realtimedatabase.SelectedPriceAmount
import com.mergenc.barista.ui.MainActivity
import java.io.IOException


class CashierActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCashierBinding

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private var selectedPrice: Int = 0
    private lateinit var qrCodeIdFirebase: String

    private lateinit var database: DatabaseReference
    private lateinit var databasePayment: DatabaseReference
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashierBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        analytics = Firebase.analytics

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

        check()
    }

    @SuppressLint("SetTextI18n")
    private fun showPaymentMethodSuccessDialog(paymentMethod: String) {

        val mDialogView = LayoutInflater.from(this@CashierActivity)
            .inflate(R.layout.custom_payment_success_dialog, null)

        val mBuilder =
            AlertDialog.Builder(this@CashierActivity).setView(mDialogView).show()
        mBuilder.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val buttonOkayPaymentSuccess =
            mDialogView.findViewById(R.id.button_okay_payment_success) as Button
        val textViewPaymentSuccess = mDialogView.findViewById(R.id.tv_payment_success) as TextView

        when (paymentMethod) {
            "wallet" -> {
                textViewPaymentSuccess.text = "Müşteri Coin Wallet ile ödemeyi gerçekleştirdi."
            }
            "credit" -> {
                textViewPaymentSuccess.text = "Müşteri kredi kartı ile ödemeyi gerçekleştirdi."
            }
        }

        buttonOkayPaymentSuccess.setOnClickListener {
            mBuilder.dismiss()

            finish()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
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

    @SuppressLint("SetTextI18n")
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
        val progressBar = mDialogView.findViewById<ProgressBar>(R.id.progressBarPayment)
        val cardViewPayment = mDialogView.findViewById<CardView>(R.id.cwPayment)

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

        buttonOkay.setOnClickListener {
            cardViewPayment.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE

            if (editTextNumber.visibility == View.VISIBLE) {
                selectedPrice = editTextNumber.text.toString().toInt()
            }

            database = FirebaseDatabase.getInstance().getReference("price")
            val selectedPriceAmount = SelectedPriceAmount(selectedPrice.toString())

            database.setValue(selectedPriceAmount).addOnSuccessListener {
                Log.d("Firebase", "Selected price inserted to database: $selectedPrice")

            }.addOnFailureListener {
                Log.e(
                    "Firebase",
                    "Selected price could not be inserted to database: $selectedPrice"
                )
            }

            // Log firebase analytics event
            val params = Bundle()
            params.putInt(
                AnalyticsEventConstants.CASHIER.PRICE_DIALOG.PRICE_DIALOG_PRICE,
                selectedPrice
            )
            analytics.logEvent(
                AnalyticsEventConstants.CASHIER.PRICE_DIALOG.PRICE_DIALOG_CONFIRM,
                params
            )

            /*val params = Bundle()
            params.putInt(AnalyticsEventConstants.CASHIER.PRICE_DIALOG.PRICE_DIALOG_PRICE, selectedPrice)
            analytics.logEvent("selected_price", params)*/
        }
    }

    private fun check() {
        databasePayment = FirebaseDatabase.getInstance().getReference("paymentMethod")
        databasePayment.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val selectedPaymentMethod = snapshot.child("selectedPaymentMethod").value.toString()
                if (selectedPaymentMethod != "null") {
                    showPaymentMethodSuccessDialog(selectedPaymentMethod)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Payment method could not be retrieved from database")
            }
        })

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

                        val params = Bundle()
                        params.putInt(
                            AnalyticsEventConstants.CASHIER.QR_CODE_SCANNING.QR_CODE_VALUE,
                            scannedValue.toInt()
                        )
                        analytics.logEvent(
                            AnalyticsEventConstants.CASHIER.QR_CODE_SCANNING.QR_CODE_SCANNED,
                            params
                        )

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