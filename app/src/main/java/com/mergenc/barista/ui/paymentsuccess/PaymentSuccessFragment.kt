package com.mergenc.barista.ui.paymentsuccess

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.mergenc.barista.databinding.FragmentPaymentSuccessBinding
import com.mergenc.barista.ui.MainActivity


class PaymentSuccessFragment : Fragment() {
    private var _binding: FragmentPaymentSuccessBinding? = null
    private val binding get() = _binding!!

    private val args: PaymentSuccessFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedPriceAmount = args.balance
        val earnedWalletCoin: Int = selectedPriceAmount.toInt() / 3

        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.progressBarPaymentSuccess.visibility = View.VISIBLE
                binding.clPaymentSuccess.visibility = View.GONE
            }

            override fun onFinish() {
                binding.progressBarPaymentSuccess.visibility = View.GONE
                binding.clPaymentSuccess.visibility = View.VISIBLE
            }
        }
        timer.start()

        binding.tvEarnedCoinWallet.text =
            "QR kod ile ödeyerek $earnedWalletCoin coin kazandın :)"
        binding.ivHome.setOnClickListener {
            //restart MainActivity
            activity?.finish()
            activity?.startActivity(Intent(context, MainActivity::class.java))
            activity?.finishAffinity()

        }

    }
}