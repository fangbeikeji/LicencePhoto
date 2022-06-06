package com.fbkj.licencephoto.moresizefragment

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.model.NormalData

/**
- @author:  LZC
- @time:  2021/6/10
- @desc:
 */
open class MoreSizeAdapter constructor(sizeData: List<NormalData>)
    : BaseQuickAdapter<NormalData, BaseViewHolder>
    (R.layout.home_size_item, sizeData) {
    private val lastIndex=sizeData.size-1
    override fun convert(helper: BaseViewHolder?, item: NormalData?) {
        helper?.setText(R.id.size_name, item!!.sizeName)
        helper?.setText(R.id.size_des, "${item!!.sizePixel}  |  ${item.sizeUnit}")
        when(helper?.adapterPosition){
            0->helper.setBackgroundRes(R.id.cl_home_item,R.drawable.drawable_top_radius)
            lastIndex->{
                helper.setBackgroundRes(R.id.cl_home_item,R.drawable.drawable_bottom_radius)
                helper.setVisible(R.id.v_division,false)
            }
        }
    }
}