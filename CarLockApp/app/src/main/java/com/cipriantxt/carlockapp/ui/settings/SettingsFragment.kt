package com.cipriantxt.carlockapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.cipriantxt.carlockapp.ActivityPipe
import com.cipriantxt.carlockapp.R
import com.cipriantxt.carlockapp.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var activityPipe: ActivityPipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activityPipe = requireActivity() as ActivityPipe
        val deviceSaveBtn = binding.deviceSaveBtn
        deviceSaveBtn.setOnClickListener { view ->
            val deviceName =  if (binding.deviceNameField.text!!.isNotEmpty()) {
                binding.deviceNameField.text
            } else {
                "*"
            }
            val devicePin = if (binding.devicePinField.text!!.isNotEmpty()) {
                binding.devicePinField.text
            } else {
                "*"
            }
            val deviceCredCommand = "setBtCred:$deviceName,$devicePin"

            if (deviceCredCommand != "setBtCred:*,*") {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.device_save_dialog_title)
                    .setMessage(R.string.device_save_dialog_desc)
                    .setNegativeButton(R.string.device_save_dialog_negative) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.device_save_dialog_positive) { _, _ ->
                        try {
                            val btConnection = activityPipe!!.getBtConnection()
                            btConnection!!.enqueueJob(this@SettingsFragment, deviceCredCommand)
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
                    .show()
            } else {
                Toast.makeText(requireContext(), R.string.device_empty_fields, Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}