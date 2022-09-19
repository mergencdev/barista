package com.mergenc.barista.ui.customer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.mergenc.barista.databinding.FragmentCustomerBinding
import com.mergenc.barista.network.analytics.AnalyticsEventConstants
import com.mergenc.barista.network.realtimedatabase.QRCodeID
import com.mergenc.barista.network.realtimedatabase.SelectedPriceAmount
import java.text.SimpleDateFormat
import java.util.*


class CustomerFragment : Fragment() {
    private var _binding: FragmentCustomerBinding? = null
    private val binding get() = _binding!!

    private lateinit var analytics: FirebaseAnalytics
    private lateinit var database: DatabaseReference
    private lateinit var qrId: String
    private val args: CustomerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics = Firebase.analytics
        listenPriceChange(view)
        qrId = args.qrId
        insertIdToDatabase()
        generateQrCode()
    }

    private fun insertIdToDatabase() {
        database = FirebaseDatabase.getInstance().getReference("qrCodeIds")
        val qRCodeID = QRCodeID(qrId)

        database.setValue(qRCodeID).addOnSuccessListener {
            Log.d("Firebase", "QR Code ID inserted to database. ID: $qrId")
        }.addOnFailureListener {
            Log.e("Firebase", "QR Code ID could not be inserted to database")
        }
    }

    // Listen "price" path's data change
    private fun listenPriceChange(view: View) {
        database = FirebaseDatabase.getInstance().getReference("price")
        val resetPriceAmount = SelectedPriceAmount("0")
        database.setValue(resetPriceAmount)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val price = snapshot.child("selectedPriceAmount").value.toString()
                if (price != "0") {
                    //Toast.makeText(context, "Price changed: $price", Toast.LENGTH_SHORT).show()
                    val action =
                        CustomerFragmentDirections.actionCustomerFragmentToTotalAmountFragment(price)
                    view.findNavController().navigate(action)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Price could not be read from database")
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun generateQrCode() {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(qrId, BarcodeFormat.QR_CODE, 250, 250)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            binding.ivQrCode.setImageBitmap(bmp)
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm")
            val currentDate = sdf.format(Date())
            binding.tvQrCodeGenerateTime.text = currentDate

            val timer = object : CountDownTimer(1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.ivQrCode.visibility = View.INVISIBLE
                    binding.tvQrCodeGenerateTime.visibility = View.INVISIBLE
                }

                override fun onFinish() {
                    binding.progressBar.visibility = View.GONE
                    binding.ivQrCode.visibility = View.VISIBLE
                    binding.tvQrCodeGenerateTime.visibility = View.VISIBLE
                }
            }
            timer.start()

            try {
                val params = Bundle()
                params.putString(AnalyticsEventConstants.CUSTOMER.QR_CODE_ID, qrId)
                analytics.logEvent(AnalyticsEventConstants.CUSTOMER.QR_CODE_GENERATED, params)
                Log.e("Firebase", "QR Code Generated")
            } catch (e: Exception) {
                Log.e("Firebase", "QR Code could not be logged to Firebase")
            }

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}