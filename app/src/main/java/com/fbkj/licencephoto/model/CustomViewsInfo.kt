package com.fbkj.licencephoto.model
import com.stx.xhb.androidx.entity.BaseBannerInfo

/**
- @author:  LZC
- @time:  2021/7/19
- @desc:
 */
class CustomViewsInfo(i:Int) :BaseBannerInfo{

    var title :String = ""

    var info: Int = i

    override fun getXBannerUrl(): Any {
        return info
    }
    override fun getXBannerTitle(): String {
        return title
    }





}