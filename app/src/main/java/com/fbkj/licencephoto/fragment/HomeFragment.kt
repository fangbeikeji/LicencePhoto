package com.fbkj.licencephoto.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.DialogBannerBottomsheetBinding
import com.fbkj.licencephoto.databinding.HomeFragmentBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.local.SavingUserMsg
import com.fbkj.licencephoto.local.SignInHandler
import com.fbkj.licencephoto.model.*
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.ui.*
import com.fbkj.licencephoto.ui.logic.EditImageActivity
import com.fbkj.licencephoto.ui.logic.MoreSizeActivity
import com.fbkj.licencephoto.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.reflect.TypeToken
import com.hjq.permissions.XXPermissions
import com.umeng.analytics.MobclickAgent
import org.greenrobot.eventbus.EventBus

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeRecyclerViewAdapter: HomeRecyclerViewAdapter
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null
    private lateinit var viewModel: MainViewModel
    private var pList = arrayListOf<String>()
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            EditImageActivity.start(requireContext(), it.data?.data!!,false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initDialog()

        //android11适配,SD卡内存获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            pList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            pList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        binding.ivBanner.setImageResource(R.mipmap.ic_banner1)
        binding.inputFromAlbum.setOnClickListener {
            //默认一寸
            LastClickRecord.getInstance().recordSizeData(homeRecyclerViewAdapter.data[0])
            inputFromAlbum()
        }
        binding.ivBanner.setOnClickListener { bottomSheetDialog!!.show() }
        binding.llSearch.setOnClickListener { SearchActivity.start(requireContext()) }

        val jsonString = JsonFetch().getJson(requireContext(), "hotsize.json")
        val result = RetrofitHelper.gson.fromJson<SizeModel>(
            jsonString,
            object : TypeToken<SizeModel>() {}.type
        )

        homeRecyclerViewAdapter = HomeRecyclerViewAdapter(result.fetcher)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerView.adapter = homeRecyclerViewAdapter

        homeRecyclerViewAdapter.setOnItemClickListener { _, _, position ->
            //热门规格点进去
            LastClickRecord.getInstance().recordSizeData(homeRecyclerViewAdapter.data[position])
//            openCamera()
            RequirePermission.getInstance().openCameraOptions(requireActivity(),lifecycleScope)
        }

        binding.takePhotoNow.setOnClickListener {
            //默认拍照,一寸
            LastClickRecord.getInstance().recordSizeData(homeRecyclerViewAdapter.data[0])
//            openCamera()
            RequirePermission.getInstance().openCameraOptions(requireActivity(),lifecycleScope)
        }

        binding.tvMoreSize.setOnClickListener { MoreSizeActivity.start(requireContext()) }

    }


    private fun inputFromAlbum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XXPermissions.with(activity).permission(pList).request { _, _ ->
                if (!SignInHandler.getInstance().isSnapSigned()) {
                    if (SavingUserMsg.getInstance().readUserMsgFromSD().isEmpty()) {
                        viewModel.snapLogin()
                    } else {
                        val result = RetrofitHelper.gson.fromJson<SnapLoginModel>(
                            SavingUserMsg.getInstance().readUserMsgFromSD(),
                            object : TypeToken<SnapLoginModel>() {}.type
                        )
                        SignInHandler.getInstance().signIn(result)
                        EventBus.getDefault().post(Event.ON_USER_SIGN_STATE_CHANGED)
                    }
                } else {
                    //登录的话，判断sd卡是否被清除了数据，如果被清楚就再新建一个licencepic.txt存信息
                    SavingUserMsg.getInstance().saveUserMsg2SD(
                        RetrofitHelper.gson.toJson(SignInHandler.getInstance().snapUser)
                    )
                    selectImage()
                }
            }
        } else {
            //小于6.0，不用申请权限，直接执行
            selectImage()
        }
    }

    private fun initDialog() {
        val bottomSheet = DialogBannerBottomsheetBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.dialog)
        bottomSheetDialog!!.setContentView(bottomSheet.root)
        bottomSheetDialog!!.setCanceledOnTouchOutside(true)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet.root.parent as FrameLayout)

        bottomSheet.tvAlbumOutput.setOnClickListener {
            //默认一寸
            LastClickRecord.getInstance().recordSizeData(homeRecyclerViewAdapter.data[0])
            bottomSheetDialog!!.dismiss()
//            RequirePermission.getInstance().require(activity!!,lifecycleScope,false)
            inputFromAlbum()
        }
        bottomSheet.tvTakePic.setOnClickListener {
            //默认拍照,一寸
            LastClickRecord.getInstance().recordSizeData(homeRecyclerViewAdapter.data[0])
//            openCamera()
            RequirePermission.getInstance().openCameraOptions(requireActivity(),lifecycleScope)
        }
    }

    /**
     * 调起系统相册 默认一寸
     */
    private fun selectImage() {
        LastClickRecord.getInstance().recordSizeData(homeRecyclerViewAdapter.data[0])
        launcher.launch(Intent().apply {
            type = "image/*"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            action = Intent.ACTION_PICK
            data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        })
        MobclickAgent.onEvent(ContextHolder.context, "album_input")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}

class HomeRecyclerViewAdapter(sizeData: List<NormalData>) :
    BaseQuickAdapter<NormalData, BaseViewHolder>
        (R.layout.home_size_item, sizeData) {
    private val lastIndex = sizeData.size - 1
    override fun convert(helper: BaseViewHolder?, item: NormalData?) {
        helper?.setText(R.id.size_name, item!!.sizeName)
        helper?.setText(R.id.size_des, "${item!!.sizePixel}  |  ${item.sizeUnit}")
        when (helper?.adapterPosition) {
            0 -> helper.setBackgroundRes(R.id.cl_home_item, R.drawable.drawable_top_radius)
            lastIndex -> {
                helper.setBackgroundRes(R.id.cl_home_item, R.drawable.drawable_bottom_radius)
                helper.setVisible(R.id.v_division, false)
            }
        }
    }
}