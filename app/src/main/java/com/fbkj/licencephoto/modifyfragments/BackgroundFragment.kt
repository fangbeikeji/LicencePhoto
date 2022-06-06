package com.fbkj.licencephoto.modifyfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.BackgroundFragmentBinding
import com.fbkj.licencephoto.model.EventModel
import org.greenrobot.eventbus.EventBus

class BackgroundFragment : Fragment() {

    private var _binding: BackgroundFragmentBinding? = null
    private val binding get() = _binding!!
    private var tag = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = BackgroundFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rbBlue.isChecked = true
        binding.flowRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_blue -> tag = 0
                R.id.rb_red -> tag = 1
                R.id.rb_white -> tag = 2
                R.id.rb_blue_transparent -> tag = 3
                R.id.rb_red_transparent -> tag = 4
                R.id.rb_grey_transparent -> tag = 5
            }
            EventBus.getDefault().post(EventModel(tag, Event.ON_BACKGROUND_CHANGED))
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = BackgroundFragment()
    }
}