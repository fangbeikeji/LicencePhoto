package com.fbkj.licencephoto.modifyfragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event.ON_ITEM_CHECK_CHANGED
import com.fbkj.licencephoto.databinding.FragmentSizeBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.model.NormalData
import com.fbkj.licencephoto.model.SizeModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.JsonFetch
import com.google.android.material.tabs.TabLayout
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus

class SizeFragment : Fragment() {

    private var _binding: FragmentSizeBinding? = null
    private val binding get() = _binding!!
    private var jsonStrings: String? = null
    private lateinit var sizeAdapter: SizeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tab = arrayOf("常用尺寸", "护照签证", "考试证照", "生活其它")

        for (i in tab.indices) {
            binding.tabLayout.apply {
                addTab(newTab().setText(tab[i]))
            }
        }

        binding.recycleView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )

        jsonStrings = JsonFetch().getJson(requireContext(), "normal_licence.json")
        val firstInitResult =
            RetrofitHelper.gson.fromJson<SizeModel>(jsonStrings, object : TypeToken<SizeModel>() {}.type)
        sizeAdapter = SizeAdapter(firstInitResult.fetcher)

        sizeAdapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
                EventBus.getDefault().post(
                    EventModel(
                        sizeAdapter.data[position].sizeIndex,
                        ON_ITEM_CHECK_CHANGED, sizeAdapter.data[position].sizeName
                    )
                )
                for (i in adapter.data.indices) {
                    sizeAdapter.data[i].choose = false
                }

                LastClickRecord.getInstance().recordSizeData(sizeAdapter.data[position])
                sizeAdapter.data[position].choose = true
                sizeAdapter.notifyDataSetChanged()
            }

        binding.recycleView.adapter = sizeAdapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                adapterDataChanges(tab!!.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        LastClickRecord.getInstance().recordSizeData!!.sizeIndex.toString().let {
            binding.tabLayout.getTabAt(
                it.substring(0,1).toInt()-1)!!.select()
            binding.recycleView.smoothScrollToPosition(
                it.substring(1,3).toInt()-1)
        }
    }

    private fun adapterDataChanges(position: Int) {
        sizeAdapter.data.clear()
        when (position) {
            0 -> jsonStrings = JsonFetch().getJson(requireContext(), "normal_licence.json")
            1 -> jsonStrings = JsonFetch().getJson(requireContext(), "passport_licence.json")
            2 -> jsonStrings = JsonFetch().getJson(requireContext(), "exam_licence.json")
            3 -> jsonStrings = JsonFetch().getJson(requireContext(), "other_licence.json")
        }
        val data = RetrofitHelper.gson.fromJson<SizeModel>(jsonStrings,
            object : TypeToken<SizeModel>() {}.type)
        sizeAdapter.setNewData(data.fetcher)
        sizeAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() = SizeFragment()
    }

}

class SizeAdapter(sizeData: List<NormalData>) : BaseQuickAdapter<NormalData, BaseViewHolder>
    (R.layout.recycle_view_size_item, sizeData) {
    @SuppressLint("ResourceAsColor")
    override fun convert(helper: BaseViewHolder?, item: NormalData?) {
        helper?.setText(R.id.tv_size, item!!.sizeName)
        helper?.setText(R.id.tv_pixel, item!!.sizePixel)
        helper?.setBackgroundRes(R.id.cl_item, R.drawable.recycle_view_background)
        if (item!!.choose || item.sizeIndex == LastClickRecord.getInstance().recordSizeData!!.sizeIndex) {
            helper?.setBackgroundRes(R.id.cl_item, R.drawable.recycle_view_choosen_background)
        }
    }
}