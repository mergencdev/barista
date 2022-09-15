package com.mergenc.barista.ui.choosetype

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.mergenc.barista.databinding.FragmentChooseTypeBinding
import com.mergenc.barista.ui.cashier.CashierActivity

class ChooseTypeFragment : Fragment() {
    private var _binding: FragmentChooseTypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseTypeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCashier.setOnClickListener {
            val intent = Intent(getActivity(), CashierActivity::class.java)
            getActivity()?.startActivity(intent)


        }

        binding.buttonCustomer.setOnClickListener {
            val action = ChooseTypeFragmentDirections.actionChooseTypeFragmentToCustomerFragment()
            it.findNavController().navigate(action)
        }
    }
}