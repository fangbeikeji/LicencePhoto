package com.fbkj.licencephoto.modifyfragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event.ON_RELOAD_CHANGED
import com.fbkj.licencephoto.databinding.FragmentReloadBinding
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.model.ReloadData
import com.fbkj.licencephoto.model.ReloadModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.JsonFetch
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus

class ReloadFragment : Fragment() {

    private var _binding: FragmentReloadBinding? = null
    private val binding get() = _binding!!
    private var jsonStrings: String? = null
    private lateinit var reloadAdapter: ReloadAdapter
    private val gson = GsonBuilder().setLenient().create()
    private var sexType = 0

    companion object {
        fun newInstance() = ReloadFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReloadBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tab = arrayOf("男装", "女装")

        for (i in tab.indices) {
            binding.tabLayout.apply {
                addTab(newTab().setText(tab[i]))
            }
        }

        binding.recycleView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.ivClearClothes.setOnClickListener {
            EventBus.getDefault().post(EventModel(-1, ON_RELOAD_CHANGED))
        }
        jsonStrings = JsonFetch().getJson(requireContext(), "mandress.json")
        val firstInitResult =
            gson.fromJson<ReloadModel>(jsonStrings, object : TypeToken<ReloadModel>() {}.type)
        setImageReload(firstInitResult)

        reloadAdapter = ReloadAdapter(firstInitResult.data)

        reloadAdapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
                EventBus.getDefault().post(
                    EventModel(position, ON_RELOAD_CHANGED, sexType.toString())
                )

                for (i in adapter.data.indices) {
                    reloadAdapter.data[i].choose = false
                }

                reloadAdapter.data[position].choose = true
                reloadAdapter.notifyDataSetChanged()

            }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                adapterDataChanges(tab!!.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.recycleView.adapter = reloadAdapter
    }

    private fun adapterDataChanges(position: Int) {
        reloadAdapter.data.clear()
        when (position) {
            0 -> {
                jsonStrings = JsonFetch().getJson(requireContext(), "mandress.json")
                sexType = 0
            }
            1 -> {
                jsonStrings = JsonFetch().getJson(requireContext(), "ladydress.json")
                sexType = 1
            }
        }
        val data = RetrofitHelper.gson.fromJson<ReloadModel>(
            jsonStrings,
            object : TypeToken<ReloadModel>() {}.type
        )
        setImageReload(data)
        reloadAdapter.setNewData(data.data)
        reloadAdapter.notifyDataSetChanged()
    }

    private fun setImageReload(data: ReloadModel) {
        if (sexType == 0) {
            data.data[0].image = R.mipmap.ic_male1
            data.data[1].image = R.mipmap.ic_male2
            data.data[2].image = R.mipmap.ic_male3
        } else {
            data.data[0].image = R.mipmap.ic_female1
            data.data[1].image = R.mipmap.ic_female2
            data.data[2].image = R.mipmap.ic_female3
        }
    }

}

class ReloadAdapter(sizeData: List<ReloadData>) : BaseQuickAdapter<ReloadData, BaseViewHolder>
    (R.layout.reload_dress_item, sizeData) {

    @SuppressLint("ResourceAsColor")
    override fun convert(helper: BaseViewHolder?, item: ReloadData?) {
        Glide.with(mContext).load(item!!.image).into(helper?.itemView!!.findViewById(R.id.iv_dress))
        helper.setBackgroundRes(R.id.cl_reload_item, R.drawable.recycle_view_background)
        if (item.choose) {
            helper.setBackgroundRes(
                R.id.cl_reload_item,
                R.drawable.drawable_clothes_choosen
            )
        }
    }
}