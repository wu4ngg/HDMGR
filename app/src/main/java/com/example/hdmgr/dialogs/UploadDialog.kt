package com.example.hdmgr.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.example.hdmgr.R
import com.example.hdmgr.classes.Folder
import com.example.hdmgr.classes.Receipt
import com.example.hdmgr.databinding.AddDialogBinding
import com.example.hdmgr.sqlite.DbOperationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadDialog(activityContext: Context) : Dialog(activityContext) {
    private lateinit var binding: AddDialogBinding
    private val dbOperationManager = DbOperationManager(context)
    private lateinit var tvFolder: TextView;
    private var receipt: Receipt = Receipt()
    private lateinit var onUploadFinished: OnUploadFinished
    private var folderList = ArrayList<Folder>()
    private lateinit var edtName: EditText
    private lateinit var edtCont: EditText
    private lateinit var edtMoney: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnCancel: Button
    private val calTime = Date()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val formatter = SimpleDateFormat("dd/MM/yy", Locale.US)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.add_dialog)
        val btnChooseFolder = findViewById<LinearLayout>(R.id.btn_choose_folder)
        val tvDate = findViewById<TextView>(R.id.tv_date)
        val tvTime = findViewById<TextView>(R.id.tv_time)
        edtName = findViewById<EditText>(R.id.edt_name_rec)
        edtCont = findViewById<EditText>(R.id.edt_content_rec)
        edtMoney = findViewById<EditText>(R.id.edt_money_rec)
        btnUpload = findViewById(R.id.dialog_yes)
        btnCancel = findViewById(R.id.dialog_no)
        tvDate.text = formatter.format(calTime)
        tvTime.text = timeFormatter.format(calTime)
        tvFolder = findViewById(R.id.tv_folder_name)
        btnChooseFolder.setOnClickListener{
            chooseFolder(it)
        }
        btnUpload.setOnClickListener {
            upload()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
    private fun upload(){
        if(edtName.text.toString().equals("")){
            Toast.makeText(context, "Vui lòng nhập tên hoá đơn", Toast.LENGTH_SHORT).show()
            return
        }
        if(edtCont.text.toString().equals("")){
            Toast.makeText(context, "Vui lòng nhập nội dung hoá đơn", Toast.LENGTH_SHORT).show()
            return
        }
        if(edtName.text.toString().equals("")){
            Toast.makeText(context, "Vui lòng nhập số tiền của hoá đơn", Toast.LENGTH_SHORT).show()
            return
        }
        if(receipt.folder.id == -1){
            Toast.makeText(context, "Vui lòng chọn thư mục của hoá đơn", Toast.LENGTH_SHORT).show()
            return
        }
        receipt.title = edtName.text.toString()
        receipt.noidung = edtCont.text.toString()
        receipt.soTien = edtMoney.text.toString().toInt()
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        receipt.fullDateString = formatter.format(calTime)
        dbOperationManager.uploadReceipt(receipt)
        onUploadFinished.onFinished(receipt)
        dismiss()
    }
    fun setOnUploadFinishedListener(onUploadFinished: OnUploadFinished){
        this.onUploadFinished = onUploadFinished
    }
    private fun chooseFolder(v: View){
        val popupMenu = PopupMenu(context, v)
        if(folderList.size == 0)
            folderList = dbOperationManager.getAllFolder()
        for (i in folderList.indices){
            popupMenu.menu.add(0, i, i, folderList[i].ten)
        }
        
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            val folder = folderList[it.itemId]
            tvFolder.text = folder.ten
            receipt.folder = folder
            true
        }
    }
}