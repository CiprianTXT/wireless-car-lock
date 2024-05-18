package com.cipriantxt.carlockapp.ui.home

import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.cipriantxt.carlockapp.R
import com.cipriantxt.carlockapp.databinding.FragmentHomeBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val permissions = arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
    private var btPermissionsGranted = false
    private val requestPermissionsLauncher = registerForActivityResult(RequestMultiplePermissions()) { isGranted ->
        if (isGranted.values.all { granted -> granted }) {
            btPermissionsGranted = true
        } else if (!permissions.any { permission -> shouldShowRequestPermissionRationale(requireActivity(), permission) }) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.bt_permission_denied_title)
                .setMessage(R.string.bt_permission_denied_desc)
                .setNeutralButton(R.string.bt_permission_denied_settings) { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + requireContext().packageName)
                    )
                    startActivity(intent)
                }
                .setPositiveButton(R.string.bt_permission_denied_ack) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val selectBtDeviceBtn: MaterialCardView = binding.selectBtDeviceBtn
        selectBtDeviceBtn.setOnClickListener { view ->
            requestBtPermissions()
            if (btPermissionsGranted) {
                view.findNavController().navigate(R.id.action_navigation_home_to_navigation_bt_device_list)
            }
        }

        return root
    }

    private fun requestBtPermissions() {
        val hasBtPermissions = permissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PERMISSION_GRANTED
        }
        when {
            hasBtPermissions -> {
                btPermissionsGranted = true
            }

            permissions.any { permission -> shouldShowRequestPermissionRationale(requireActivity(), permission) } -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.bt_rationale_title)
                    .setMessage(R.string.bt_rationale_desc)
                    .setNegativeButton(R.string.bt_rationale_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.bt_rationale_ack) { _, _ ->
                        requestPermissionsLauncher.launch(permissions)
                    }
                    .show()
            }

            else -> {
                requestPermissionsLauncher.launch(permissions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}