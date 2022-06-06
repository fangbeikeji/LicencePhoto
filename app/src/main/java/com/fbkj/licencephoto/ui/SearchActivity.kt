package com.fbkj.licencephoto.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.databinding.SearchActivityBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.NormalData
import com.fbkj.licencephoto.model.SizeModel
import com.fbkj.licencephoto.utils.JsonFetch
import com.fbkj.licencephoto.utils.RequirePermission
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchActivity : AppCompatActivity() {

    private var _binding: SearchActivityBinding? = null
    private val binding get() = _binding!!
    private var sizeName = arrayListOf<String>()//按照名字
    private var sizePixel= arrayListOf<String>()//按照像素
    private var sizeUnit= arrayListOf<String>()//按照mm尺寸
    private var searchAdapter = SearchAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = SearchActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false)

        val jsonStrings = JsonFetch().getJson(this, "all_licence.json")
        val firstInitResult =
            Gson().fromJson<SizeModel>(jsonStrings, object : TypeToken<SizeModel>() {}.type)

        for(i in firstInitResult.fetcher.indices){
            sizeName.add(firstInitResult.fetcher[i].sizeName)
            sizePixel.add(firstInitResult.fetcher[i].sizePixel)
            sizeUnit.add(firstInitResult.fetcher[i].sizeUnit)
        }

        binding.ivClearAll.setOnClickListener { binding.etSearch.text.clear() }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().isEmpty()){
                    binding.recyclerView.visibility = View.GONE
                }else{
                    binding.recyclerView.visibility = View.VISIBLE
                    val searchData = arrayListOf<NormalData>()
                    for (j in firstInitResult.fetcher.indices){
                        if (sizeName[j].contains(s.toString())){
                            searchData.add(firstInitResult.fetcher[j])
                        }
                        if (sizePixel[j].contains(s.toString())){
                            searchData.add(firstInitResult.fetcher[j])
                        }
                        if (sizeUnit[j].contains(s.toString())){
                            searchData.add(firstInitResult.fetcher[j])
                        }
                    }
                    searchAdapter.setNewData(searchData)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        searchAdapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
                LastClickRecord.getInstance().recordSizeData(searchAdapter.data[position])
                RequirePermission.getInstance().openCameraOptions(this@SearchActivity,
                    lifecycleScope)
                adapter.notifyDataSetChanged()
            }
        binding.recyclerView.adapter = searchAdapter
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SearchActivity::class.java))
        }
    }
}

class SearchAdapter(searchData: List<NormalData>?) : BaseQuickAdapter<NormalData, BaseViewHolder>
    (R.layout.search_item, searchData) {
    override fun convert(helper: BaseViewHolder?, item: NormalData?) {
        helper?.apply {
            setText(R.id.size_name,item!!.sizeName)
            setText(R.id.size_pixel,"${item.sizePixel}  |  ${item.sizeUnit}")
        }
    }
}