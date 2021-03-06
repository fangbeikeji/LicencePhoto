package com.fbkj.licencephoto.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.allenliu.versionchecklib.v2.AllenVersionChecker
import com.allenliu.versionchecklib.v2.builder.UIData
import com.fbkj.licencephoto.BuildConfig
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.base.BaseActivity
import com.fbkj.licencephoto.config.Event.ON_LAUNCH_SUCCESS
import com.fbkj.licencephoto.config.Event.ON_USER_SIGN_STATE_CHANGED
import com.fbkj.licencephoto.databinding.ActivityMainBinding
import com.fbkj.licencephoto.fragment.HomeFragment
import com.fbkj.licencephoto.fragment.MineFragment
import com.fbkj.licencephoto.local.SignInHandler
import com.fbkj.licencephoto.model.SnapLoginModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.local.SavingUserMsg
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.reflect.TypeToken
import com.hjq.permissions.XXPermissions
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.tab_custom_view_home.view.*
import org.greenrobot.eventbus.EventBus
import java.io.*
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var permissions = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        EventBus.getDefault().post(ON_LAUNCH_SUCCESS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //android11??????,SD???????????????
            permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        XXPermissions.with(this@MainActivity).permission(permissions)
            .request { _, _ ->
                //???????????????
                if (!SignInHandler.getInstance().isSnapSigned()) {
                    if (SavingUserMsg.getInstance().readUserMsgFromSD().isEmpty()) {
                        //??????????????????
                        viewModel.snapLogin()
                    } else {
                        //???SD?????????????????????????????????
                        val result = RetrofitHelper.gson.fromJson<SnapLoginModel>(
                            SavingUserMsg.getInstance().readUserMsgFromSD(),
                            object : TypeToken<SnapLoginModel>() {}.type
                        )
                        SignInHandler.getInstance().signIn(result)
                        EventBus.getDefault().post(ON_USER_SIGN_STATE_CHANGED)
                    }
                } else {
                    //?????????????????????sd????????????????????????????????????????????????????????????licencepic.txt?????????
                    SavingUserMsg.getInstance().saveUserMsg2SD(
                        RetrofitHelper.gson.toJson(SignInHandler.getInstance().snapUser)
                    )
                }
            }

        binding.viewPager2.adapter = MainPagerAdapter(this@MainActivity)
        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.offscreenPageLimit = 2
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.setCustomView(R.layout.tab_custom_view_home)
            when (position) {
                0 -> {
                    tab.view.tab_icon.setImageResource(R.mipmap.ic_home_on)
                    tab.view.tab_text.text = "??????"
                }
                1 -> {
                    tab.view.tab_icon.setImageResource(R.mipmap.ic_mine_off)
                    tab.view.tab_text.text = "??????"
                }
            }
        }.attach()

        checkUpdate()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> tab.view.tab_icon.setImageResource(R.mipmap.ic_home_on)
                    1 -> tab.view.tab_icon.setImageResource(R.mipmap.ic_mine_on)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> tab.view.tab_icon.setImageResource(R.mipmap.ic_home_off)
                    1 -> tab.view.tab_icon.setImageResource(R.mipmap.ic_mine_off)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun checkUpdate() {
        viewModel.updateModel.observe(this) {
            if (it.version_code == BuildConfig.VERSION_CODE) {
                return@observe
            }
            val updateLib = AllenVersionChecker
                .getInstance()
                .downloadOnly(
                    UIData.create().setTitle("????????????")
                        .setContent(it.description)
                        .setDownloadUrl(it.apk_url)
                )
                .setForceRedownload(true)//????????????????????????????????????????????????apk
                .setShowDownloadingDialog(false)//???????????????????????????
                .setDownloadAPKPath("${getExternalFilesDir("apk")?.path}")//??????????????????
                .setApkName(
                    "${getString(R.string.app_name)}-${packageName}.apk"
                )//????????????????????????
                .setOnCancelListener {
                    MMKV.defaultMMKV().encode("updateVersion",it.version_code)
                }
            //??????????????????????????????
            if (it.must_update) {
                updateLib.setForceUpdateListener {//????????????????????????????????????????????? ?????????????????????????????????
                    finish()
                    exitProcess(0)
                }
            }
            updateLib.executeMission(this@MainActivity)
        }

        viewModel.checkUpdate(packageName, BuildConfig.VERSION_CODE)

    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}

private class MainPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> MineFragment.newInstance()
            else -> throw IllegalStateException("No fragment can be instantiated for position $position")
        }
    }
}