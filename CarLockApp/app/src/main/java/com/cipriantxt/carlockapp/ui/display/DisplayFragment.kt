package com.cipriantxt.carlockapp.ui.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cipriantxt.carlockapp.databinding.FragmentDisplayBinding

class DisplayFragment : Fragment() {

    private var _binding: FragmentDisplayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val displayViewModel =
            ViewModelProvider(this).get(DisplayViewModel::class.java)

        _binding = FragmentDisplayBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        For this component:
//        <TextView
//        android:id="@+id/text_display"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:layout_marginStart="8dp"
//        android:layout_marginTop="8dp"
//        android:layout_marginEnd="8dp"
//        android:textAlignment="center"
//        android:textSize="20sp"
//        app:layout_constraintBottom_toBottomOf="parent"
//        app:layout_constraintEnd_toEndOf="parent"
//        app:layout_constraintStart_toStartOf="parent"
//        app:layout_constraintTop_toTopOf="parent" />

//        Modifying content:
//        val textView: TextView = binding.textDisplay
//        displayViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}