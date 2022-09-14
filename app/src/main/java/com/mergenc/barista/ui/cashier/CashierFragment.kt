package com.mergenc.barista.ui.cashier

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mergenc.barista.R
import com.mergenc.barista.databinding.FragmentCashierBinding
import com.mergenc.barista.databinding.FragmentCustomerBinding

class CashierFragment : Fragment() {
    private var _binding: FragmentCashierBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashierBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}