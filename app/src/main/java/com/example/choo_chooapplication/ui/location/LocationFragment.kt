package com.example.choo_chooapplication.ui.location

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.choo_chooapplication.MainActivity
import com.example.choo_chooapplication.databinding.FragmentLocationBinding

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(LocationViewModel::class.java)

        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = "Redirecting to Maps..."
        }

        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/@51.5639249,5.0408643,17z/data=!3m1!4b1!4m2!7m1!2e1?entry=ttu")
        )
        startActivity(urlIntent)

        return root
    }

//    override fun onResume() {
//        super.onResume()
//        val intent = Intent(this.context,MainActivity::class.java)
//        startActivity(intent)
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}