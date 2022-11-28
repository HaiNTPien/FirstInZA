package com.example.listwithanimation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.listwithanimation.databinding.FragmentLoadingBinding

class LoadingFragment : Fragment() {

    private lateinit var binding: FragmentLoadingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isResumed){
            (requireActivity() as ContactActivity).turnOffLoading(1)
        }
    }
}