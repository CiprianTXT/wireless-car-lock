package com.cipriantxt.carlockapp.ui.home

import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.cipriantxt.carlockapp.R
import com.cipriantxt.carlockapp.databinding.FragmentHomeBinding
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var btScanGranted = false
    private var btConnectGranted = false
    private val btScanPermission = BLUETOOTH_SCAN
    private val btConnectPermission = BLUETOOTH_CONNECT
    private val permissions = arrayOf(btScanPermission, btConnectPermission)


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
            if (btScanGranted && btConnectGranted) {
                view.findNavController().navigate(R.id.action_navigation_home_to_navigation_bt_device_list)
            }
        }

        return root
    }

    private fun requestPermissions() {
        val notGrantedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) != PERMISSION_DENIED
        }
        if (notGrantedPermissions.isNotEmpty()) {
            val showRationale = notGrantedPermissions.any { permission ->
                shouldShowRequestPermissionRationale(permission)
            }
            if (showRationale) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.bt_rationale_title)
                    .setMessage(R.string.bt_rationale_desc)
                    .setNegativeButton(R.string.bt_rationale_negative_btn) { dialog, _ ->
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}