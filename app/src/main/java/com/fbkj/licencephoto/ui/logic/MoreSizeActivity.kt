package com.fbkj.licencephoto.ui.logic

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fbkj.licencephoto.databinding.MoreSizeActivityBinding
import com.fbkj.licencephoto.moresizefragment.ExamLicenceFragment
import com.fbkj.licencephoto.moresizefragment.OtherLicenceFragment
import com.fbkj.licencephoto.moresizefragment.PassPortFragment
import com.fbkj.licencephoto.moresizefragment.UsualSizeFragment
import com.fbkj.licencephoto.ui.SearchActivity
import com.google.android.material.tabs.TabLayoutMediator

class MoreSizeActivity : AppCompatActivity() {

    private var _binding: MoreSizeActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding= MoreSizeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text="更多规格"
        binding.viewPager2.adapter = MoreSizeFragmentAdapter(this)
        binding.viewPager2.isUserInputEnabled = true
        binding.viewPager2.offscreenPageLimit = 4
        lifecycleScope
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "常用尺寸"
                1 -> tab.text = "护照签证"
                2 -> tab.text = "考试证照"
                3 -> tab.text = "生活其它"
            }
        }.attach()
        binding.ivMoreSearch.setOnClickListener { SearchActivity.start(this) }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MoreSizeActivity::class.java))
        }
    }

}

private class MoreSizeFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> UsualSizeFragment.newInstance()
            1 -> PassPortFragment.newInstance()
            2 -> ExamLicenceFragment.newInstance()
            3 -> OtherLicenceFragment.newInstance()
            else -> throw IllegalStateException("No fragment can be instantiated for position $position")
        }
    }
}