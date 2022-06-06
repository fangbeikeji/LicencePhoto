package com.fbkj.licencephoto.ui.mine

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fbkj.licencephoto.databinding.MyOrderActivityBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.tab_custom_view_home.view.*
import com.fbkj.licencephoto.orderfragment.ElectronOrderFragment
import com.fbkj.licencephoto.orderfragment.FlushOrderFragment

class MyOrderActivity : AppCompatActivity() {

    private var _binding: MyOrderActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MyOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "我的订单"

        binding.viewPager2.adapter = OrderPagerAdapter(this@MyOrderActivity)
        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "电子版"
                1 -> tab.text = "冲印版"
            }
        }.attach()
        binding.tabLayout.getTabAt(intent.getIntExtra("orderType",0))!!.select()
    }

    companion object {
        fun start(context: Context,orderType: Int) {
            context.startActivity(Intent(context, MyOrderActivity::class.java).apply {
                putExtra("orderType",orderType)
            })
        }
    }

}

private class OrderPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> ElectronOrderFragment.newInstance()
            1 -> FlushOrderFragment.newInstance()
            else -> throw IllegalStateException("No fragment can be instantiated for position $position")
        }
    }
}