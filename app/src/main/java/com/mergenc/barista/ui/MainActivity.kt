package com.mergenc.barista.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mergenc.barista.R
import com.mergenc.barista.network.realtimedatabase.PaymentMethod
import com.mergenc.barista.network.realtimedatabase.SelectedPriceAmount

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var databasePayment: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create a random with the given seed
        // Random(Instant.now().nano).nextInt(0,100)

        // create a random number with the current time in milliseconds
        // Random(System.currentTimeMillis()).nextInt(0,100)

        resetCurrentPrice()
        resetPaymentMethod()
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.layout_actionbar)
    }

    private fun resetCurrentPrice() {
        database = FirebaseDatabase.getInstance().getReference("price")
        val currentPriceAmount = 0
        val selectedPriceAmount = SelectedPriceAmount(currentPriceAmount.toString())

        database.setValue(selectedPriceAmount)
    }

    private fun resetPaymentMethod() {
        databasePayment = FirebaseDatabase.getInstance().getReference("paymentMethod")
        val currentPaymentMethod = null
        val selectedPaymentMethod = PaymentMethod(currentPaymentMethod)

        databasePayment.setValue(selectedPaymentMethod)
    }
}