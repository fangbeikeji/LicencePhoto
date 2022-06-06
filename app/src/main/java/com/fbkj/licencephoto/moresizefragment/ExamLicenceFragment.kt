package com.fbkj.licencephoto.moresizefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fbkj.licencephoto.databinding.MoreSizeFragmentBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.SizeModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.JsonFetch
import com.fbkj.licencephoto.utils.RequirePermission
import com.google.gson.reflect.TypeToken

class ExamLicenceFragment : Fragment() {

    private lateinit var adapter: MoreSizeAdapter
    private var _binding: MoreSizeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MoreSizeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val jsonString = JsonFetch().getJson(requireContext(), "exam_licence.json")
        val result = RetrofitHelper.gson.fromJson<SizeModel>(jsonString,
            object : TypeToken<SizeModel>() {}.type)

        adapter = MoreSizeAdapter(result.fetcher)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { _, _, position ->
            LastClickRecord.getInstance().recordSizeData(adapter.data[position])
            RequirePermission.getInstance().openCameraOptions(activity!!,lifecycleScope)
        }
    }

    companion object {
        fun newInstance() = ExamLicenceFragment()
    }
}