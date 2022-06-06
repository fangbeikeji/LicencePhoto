package com.fbkj.licencephoto.modifyfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.fbkj.licencephoto.config.Event.ON_BUFFING_CHANGED
import com.fbkj.licencephoto.config.Event.ON_WHITE_CHANGED
import com.fbkj.licencephoto.databinding.BuffingFragmentBinding
import com.fbkj.licencephoto.model.EventModel
import org.greenrobot.eventbus.EventBus

class BuffingFragment : Fragment() {

    private var _binding: BuffingFragmentBinding? = null
    private val binding get() = _binding!!
    private var buffering = 0
    private var white = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BuffingFragmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sbBuffing.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    buffering = seekBar!!.progress
                    EventBus.getDefault().post(EventModel(buffering, ON_BUFFING_CHANGED))
                }
            })
//
        binding.sbWhite.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    white = seekBar!!.progress
                    EventBus.getDefault().post(EventModel(white, ON_WHITE_CHANGED))
                }
            })
    }

    companion object {
        @JvmStatic
        fun newInstance() = BuffingFragment()
    }
}