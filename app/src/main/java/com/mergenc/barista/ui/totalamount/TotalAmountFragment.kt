package com.mergenc.barista.ui.totalamount

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mergenc.barista.R
import com.mergenc.barista.databinding.FragmentTotalAmountBinding
import com.mergenc.barista.network.realtimedatabase.PaymentMethod
import com.mergenc.barista.network.realtimedatabase.SelectedPriceAmount
import com.mergenc.barista.ui.customer.CustomerFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class TotalAmountFragment : Fragment() {
    private var _binding: FragmentTotalAmountBinding? = null
    private val binding get() = _binding!!
    private val args: TotalAmountFragmentArgs by navArgs()
    private lateinit var selectedPriceAmount: String
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotalAmountBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedPriceAmount = args.price
        binding.tvBalance.text = "   $selectedPriceAmount,00 ₺"
        database = FirebaseDatabase.getInstance().getReference("paymentMethod")

        val sdf = SimpleDateFormat("dd.M.yyyy / hh:mm")
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate

        binding.cwPaymentWallet.setOnClickListener {
            val balance = binding.tvBalance.text.toString().substring(3, 5).toInt()
            if (balance < 48) {
                //Make error dialog
                makeErrorDialog()

            } else {
                val selectedPaymentMethod = PaymentMethod("wallet")
                database.setValue(selectedPaymentMethod)

                val action =
                    TotalAmountFragmentDirections.actionTotalAmountFragmentToPaymentSuccessFragment(
                        selectedPriceAmount
                    )
                view.findNavController().navigate(action)
            }
        }

        binding.cwPaymentCredit.setOnClickListener {
            val selectedPaymentMethod = PaymentMethod("credit")
            database.setValue(selectedPaymentMethod)

            val action =
                TotalAmountFragmentDirections.actionTotalAmountFragmentToPaymentSuccessFragment(
                    selectedPriceAmount
                )
            view.findNavController().navigate(action)
        }
    }

    private fun makeErrorDialog() {
        val mDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_error_dialog, null)

        val mBuilder =
            AlertDialog.Builder(requireContext()).setView(mDialogView).show()
        mBuilder.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val buttonOkay = mDialogView.findViewById(R.id.button_okay_error) as Button

        buttonOkay.setOnClickListener {
            mBuilder.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}