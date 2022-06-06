package com.fbkj.licencephoto.local

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import com.fbkj.licencephoto.model.AddressModel
import com.fbkj.licencephoto.model.NewOrder
import com.fbkj.licencephoto.model.NormalData
import com.fbkj.licencephoto.model.Order
import com.fbkj.licencephoto.network.RetrofitHelper
import com.tencent.mmkv.MMKV

interface LastClick {
    fun recordSizeData(recordSizeData: NormalData)
    fun recordAddressData(recordAddressData: AddressModel)
    fun recordOrderData(recordOrderData: NewOrder)
    fun hasAddress():Boolean
}

class LastClickRecord : LastClick {
    private val kv = MMKV.defaultMMKV()

    /**
     * 存一下最近一次用户点击反馈
     * */
    var recordSizeData: NormalData? = null
        set(value) {
            field = value
            val v = if (value == null) null else {
                RetrofitHelper.gson.toJson(value)
            }
            kv.encode(PREF_SIZE_RECORD, v)

        }

    /**
     * 存一下最近一次用户点击订单信息
     * */
    var recordOrderData: NewOrder? = null
        set(value) {
            field = value
            val v = if (value == null) null else {
                RetrofitHelper.gson.toJson(value)
            }
            kv.encode(PREF_ORDER_RECORD, v)
        }

    /**
     * 存一下最近一次用户最近一次使用地址
     * */
    var recordAddressData: AddressModel? = null
        set(value) {
            field = value
            val v = if (value == null) null else {
                RetrofitHelper.gson.toJson(value)
            }
            kv.encode(PREF_ADDRESS_RECORD, v)
        }

    var background: String = "蓝底"

    var tradNo: String = ""

    lateinit var order: Order

    var isBeauty: Int = 0

    var flushNum: Int = 0

    lateinit var finalBitmapWithBg: Bitmap

    lateinit var finalBitmapWithoutBg: Bitmap

    lateinit var isOrderData: NewOrder

    var isOrder: Boolean = false

    var onDiscountOrder: Boolean = false

    var orderType: Int = 0

    var saveLicenceTimes: Int = 0

    init {
        recordSizeData =  RetrofitHelper.gson.fromJson(kv.decodeString(PREF_SIZE_RECORD),
            NormalData::class.java)
        recordAddressData = RetrofitHelper.gson.fromJson(kv.decodeString(PREF_ADDRESS_RECORD),
            AddressModel::class.java)
        recordOrderData = RetrofitHelper.gson.fromJson(kv.decodeString(PREF_ORDER_RECORD),
            NewOrder::class.java)
    }

    override fun recordSizeData(recordSizeData: NormalData) {
        this.recordSizeData = recordSizeData
    }

    override fun recordAddressData(recordAddressData: AddressModel) {
        this.recordAddressData = recordAddressData
    }

    override fun recordOrderData(recordOrderData: NewOrder) {
        this.recordOrderData = recordOrderData
    }

    override fun hasAddress():Boolean{
        return recordAddressData !=null
    }

    companion object {
        private const val PREF_SIZE_RECORD = "pref_record"
        private const val PREF_ADDRESS_RECORD = "pref_address_record"
        private const val PREF_ORDER_RECORD = "pref_order_record"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LastClickRecord? = null

        fun getInstance() =
            INSTANCE ?: synchronized(LastClickRecord::class.java) {
                INSTANCE ?: LastClickRecord()
                    .also { INSTANCE = it }
            }
    }
}