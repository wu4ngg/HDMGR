package com.example.hdmgr.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Bundle
import android.service.autofill.OnClickAction
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.hdmgr.R
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener

class CustomAlertDialog(context: Context, var title: String, var subtitle: String, var icon: Int = R.drawable.pencil_line) : Dialog(context) {
    var imageResouce: Int = 0
    var onYesClickAction: () -> Unit = {}
    var onNoClickAction: () -> Unit = {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val yesBtn: Button = findViewById(R.id.dialog_yes)
        val noBtn: Button = findViewById(R.id.dialog_no)
        val titleView: TextView = findViewById(R.id.dialog_title)
        val subtitleView: TextView = findViewById(R.id.dialog_subtitle)
        val iconView: ImageView = findViewById(R.id.dialog_icon)
        titleView.text = title
        subtitleView.text = subtitle
        iconView.setImageResource(icon)
        yesBtn.setOnClickListener {
            onYesClickAction()
            dismiss()
        }
        noBtn.setOnClickListener {
            onNoClickAction()
            dismiss()
        }
    }
}