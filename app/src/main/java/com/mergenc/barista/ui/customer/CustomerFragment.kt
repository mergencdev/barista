package com.mergenc.barista.ui.customer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.mergenc.barista.databinding.FragmentCustomerBinding
import java.text.SimpleDateFormat
import java.util.*

class CustomerFragment : Fragment() {
    private var _binding: FragmentCustomerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateQrCode()
    }

    @SuppressLint("SimpleDateFormat")
    private fun generateQrCode() {
        // random number int
        val random = (1..999).random()

        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(random.toString(), BarcodeFormat.QR_CODE, 250, 250)
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

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}