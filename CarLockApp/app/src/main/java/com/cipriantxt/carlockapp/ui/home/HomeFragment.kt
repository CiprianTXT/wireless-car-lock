package com.cipriantxt.carlockapp.ui.home

import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.ActivityCompat
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

    private var permissions = hashMapOf(
        BLUETOOTH_SCAN to false,
        BLUETOOTH_CONNECT to false
    )
    private var currentPermission: String? = null
    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        if (isGranted) {
            permissions[currentPermission!!] = true
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.bt_permission_denied_title)
                .setMessage(R.string.bt_permission_denied_desc)
                .setNegativeButton(R.string.bt_permission_denied_settings) { dialog, which ->
                }
                .setPositiveButton(R.string.bt_permission_denied_ack) { action, _ ->
                    action.dismiss()
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
            requestPermissions()
            if (permissions.values.all { granted -> granted }) {
                view.findNavController().navigate(R.id.action_navigation_home_to_navigation_bt_device_list)
            }
        }

        return root
    }

    private fun requestPermissions() {
//        val notGrantedPermissions = permissions.filter { permission ->
//            ContextCompat.checkSelfPermission(requireContext(), permission) != PERMISSION_DENIED
//        }
//        val showRationale = notGrantedPermissions.any { permission ->
//            shouldShowRequestPermissionRationale(permission)
//        }
//        if (notGrantedPermissions.isNotEmpty()) {
//            if (showRationale) {
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle(R.string.bt_rationale_title)
//                    .setMessage(R.string.bt_rationale_desc)
//                    .setNegativeButton(R.string.bt_rationale_negative_btn) { dialog, _ ->
//
//                    }
//            }
//        }
        permissions.forEach { (permission, _) ->
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PERMISSION_GRANTED -> {
                    permissions[permission] = true
                }

                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission) -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.bt_rationale_title)
                        .setMessage(R.string.bt_rationale_desc)
                        .setNegativeButton(R.string.bt_rationale_cancel) { dialog, _ ->
                        }
                        .setPositiveButton(R.string.bt_rationale_ack) { dialog, _ ->
                        }
                        .show()
                }

                else -> {
                    currentPermission = permission
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}