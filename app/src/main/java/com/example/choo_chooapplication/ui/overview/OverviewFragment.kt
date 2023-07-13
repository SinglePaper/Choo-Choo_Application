package com.example.choo_chooapplication.ui.overview

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.choo_chooapplication.databinding.FragmentHomeBinding

class OverviewFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var sharedPref: SharedPreferences;
    private lateinit var editor: SharedPreferences.Editor;

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val overviewViewModel =
            ViewModelProvider(this).get(OverviewViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedPref = this.activity?.getSharedPreferences("personInfo", Context.MODE_PRIVATE) as SharedPreferences
        editor = sharedPref.edit()

        val textView: TextView = binding.tvHomeTeamName
//        overviewViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = "Does this work?"
//        }
        var teamName = sharedPref.getString("teamName", "Team loading...")
        textView.text = "$teamName"
        Log.d("Testing", "Team overview opened.")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}