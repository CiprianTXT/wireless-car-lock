package com.cipriantxt.carlockapp.ui.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.cipriantxt.carlockapp.ActivityPipe
import com.cipriantxt.carlockapp.R
import com.cipriantxt.carlockapp.databinding.FragmentDisplayBinding

class DisplayFragment : Fragment() {
    private var _binding: FragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private var activityPipe: ActivityPipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activityPipe = requireActivity() as ActivityPipe
        val contactSaveBtn = binding.contactSaveBtn
        contactSaveBtn.setOnClickListener { view ->
            val contactName = if (binding.contactNameField.text!!.isNotEmpty()) {
                binding.contactNameField.text
            } else {
                "*"
            }
            val contactPhoneNumber = if (binding.contactPhoneNumberField.text!!.isNotEmpty()) {
                binding.contactPhoneNumberField.text
            } else {
                "*"
            }
            val contactCommand = "setInfo:$contactName,$contactPhoneNumber"

            val showContact = binding.contactShowSwitch.isChecked
            val displayCommand = if (showContact) {
                "hideInfo:0"
            } else {
                "hideInfo:1"
            }

            try {
                val btConnection = activityPipe!!.getBtConnection()
                if (contactCommand != "setInfo:*,*") {
                    btConnection!!.enqueueJob(this@DisplayFragment, contactCommand)
                }
                btConnection!!.enqueueJob(this@DisplayFragment, displayCommand)
            } catch (e: Exception) {
                when (e) {
                    is NullPointerException, is ClassCastException -> {
                        Toast.makeText(requireContext(), R.string.bt_no_remote_connection, Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                view.findNavController().navigate(R.id.navigation_home)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}