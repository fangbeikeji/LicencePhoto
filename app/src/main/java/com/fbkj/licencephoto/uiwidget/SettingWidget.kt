package com.fbkj.licencephoto.uiwidget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fbkj.licencephoto.R

/**
- @author:  LZC
- @time:  2021/6/11
- @desc:
 */

@SuppressLint("CustomViewStyleable")
class SettingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.setting_item_layout, this, true)
        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingLayout)
        val iconRes = a.getResourceId(R.styleable.SettingLayout_icon, 0)
        val title = a.getString(R.styleable.SettingLayout_title)
        val iconRight = a.getBoolean(R.styleable.SettingLayout_iconRight, true)
        val textColor = a.getColor(R.styleable.SettingLayout_textColor, Color.BLACK)
        a.recycle()
        val ivIcon = findViewById<ImageView>(R.id.iv_icon)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val ivRight = findViewById<ImageView>(R.id.iv_icon_right)

        if (iconRes != 0) {
            ivIcon.setImageResource(iconRes)
        } else {
            ivIcon.visibility = View.GONE
        }

        if (iconRight) {
            ivRight.visibility = View.VISIBLE
        } else {
            ivRight.visibility = View.GONE
        }

        tvTitle.text=title
        tvTitle.setTextColor(textColor)
    }

}