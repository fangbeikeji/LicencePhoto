package com.fbkj.licencephoto.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event.ON_USER_SIGN_STATE_CHANGED
import com.fbkj.licencephoto.databinding.FragmentMineBinding
import com.fbkj.licencephoto.dialogs.ContactDialog
import com.fbkj.licencephoto.local.SignInHandler
import com.fbkj.licencephoto.ui.*
import com.fbkj.licencephoto.ui.mine.AboutUsActivity
import com.fbkj.licencephoto.ui.mine.MyOrderActivity
import com.fbkj.licencephoto.ui.mine.RecommendActivity
import com.fbkj.licencephoto.utils.RequirePermission
import com.fbkj.licencephoto.utils.toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactDialog: ContactDialog

    companion object {
        fun newInstance() = MineFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlLogin.setOnClickListener {
            if (!SignInHandler.getInstance().isSnapSigned()) {
                RequirePermission.getInstance().onPermissionFetch(requireActivity(), lifecycleScope)
            } else {
                toast("欢迎用户${SignInHandler.getInstance().snapUser!!.id}")
            }
        }

        Glide.with(this).load(R.mipmap.ic_launcher_round2)
            .transform(CircleCrop()).into(binding.ivAvatar)

        contactDialog = ContactDialog(requireContext(), object : ContactDialog.Callback {
            override fun onClick(v: View?) {
                requireContext().toast("已复制qq号到粘贴板")
            }
        })
        contactDialog.window!!.setBackgroundDrawableResource(R.color.transparent)

        binding.tvName.text = "欢迎使用证件照大师"
        if (SignInHandler.getInstance().snapUser != null) {
            binding.tvId.text = "用户ID: ${SignInHandler.getInstance().snapUser!!.id}"
        }

        binding.swContact.setOnClickListener { contactDialog.show() }
        binding.swAboutUs.setOnClickListener { AboutUsActivity.start(requireContext()) }
        binding.swRecommend.setOnClickListener {
            RecommendActivity.start(requireContext())
        }

        binding.tvMyLicences.setOnClickListener {
            if (!SignInHandler.getInstance().isSnapSigned()) {
                RequirePermission.getInstance().onPermissionFetch(requireActivity(), lifecycleScope)
            } else {
                MyLicenceActivity.start(requireContext())
            }
        }

        binding.swOrder.setOnClickListener {
            if (!SignInHandler.getInstance().isSnapSigned()) {
                RequirePermission.getInstance().onPermissionFetch(requireActivity(), lifecycleScope)
            } else {
                MyOrderActivity.start(requireContext(), 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: String) {
        if (message == ON_USER_SIGN_STATE_CHANGED) {
            if (SignInHandler.getInstance().isSnapSigned()) {
                binding.tvName.text = "欢迎使用证件照大师"
                binding.tvId.text = "用户ID: ${SignInHandler.getInstance().snapUser!!.id}"
            }
        }
    }
}