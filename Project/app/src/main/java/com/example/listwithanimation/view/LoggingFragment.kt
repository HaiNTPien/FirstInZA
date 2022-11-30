package com.example.listwithanimation.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listwithanimation.R
import com.example.listwithanimation.adapters.LoggingAdapter
import com.example.listwithanimation.databinding.FragmentLoggingBinding
import com.example.listwithanimation.databinding.LoggingItemLayoutBinding
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.helpers.SharePreferences
import com.example.listwithanimation.helpers.SharePreferences.get
import com.example.listwithanimation.helpers.SharePreferences.set
import com.example.listwithanimation.models.LogModel
import com.google.gson.Gson

class LoggingFragment : Fragment() {
    lateinit var binding: FragmentLoggingBinding
    private lateinit var adapter: LoggingAdapter
    private lateinit var sharePref: SharedPreferences
    private var isRecyclerViewInit = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharePref = SharePreferences.defaultPrefs(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoggingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()

    }

    private fun initRecyclerView() {
        isRecyclerViewInit = true
        val linearLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapter = LoggingAdapter()
        binding.rvLogging.apply {
            layoutManager = linearLayoutManager
            this@LoggingFragment.adapter.configRecyclerView(this)
            this@LoggingFragment.adapter.onItemClickCallback = {
                sharePref["dataLog"] = Gson().toJson(listOf<LogModel>())
                updateLoggingList()
            }
        }
        updateLoggingList()
    }

    override fun onResume() {
        super.onResume()
        if (isRecyclerViewInit) {
            updateLoggingList()
        }
    }

    private fun updateLoggingList() {
        val loggingList = sharePref["dataLog", ""]
        binding.rvLogging.post {
            if (Gson().fromJson(loggingList, Array<LogModel>::class.java) == null) {
                adapter.submitList(listOf())
            } else {
                adapter.submitList(
                    Gson().fromJson(loggingList, Array<LogModel>::class.java).toList()
                )
            }
        }
    }
}