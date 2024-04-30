package com.example.hdmgr.adapters

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import com.example.hdmgr.R
import com.example.hdmgr.classes.Receipt
import com.example.hdmgr.dialogs.CustomAlertDialog
import com.example.hdmgr.exceptions.InvalidReceiptException
import com.example.hdmgr.sqlite.DbOperationManager
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ReceiptAdapter(
    var context: Context,
    var itemList: ArrayList<Receipt>
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {
    val dbOperationManager = DbOperationManager(context)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvId = view.findViewById<TextView>(R.id.tv_id)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        var tvDate = view.findViewById<TextView>(R.id.tv_date)
        var tvTime = view.findViewById<TextView>(R.id.tv_time)
        var tvPurpose = view.findViewById<TextView>(R.id.tv_purpose)
        var tvFolder = view.findViewById<TextView>(R.id.tv_folder)
        var tvMoney = view.findViewById<TextView>(R.id.tv_money)
        val btnMenu = view.findViewById<ImageButton>(R.id.btn_menu)
        val root = view.findViewById<LinearLayout>(R.id.root)
        var tvIndex = view.findViewById<TextView>(R.id.tv_index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (itemList.size > 0) ViewHolder(LayoutInflater.from(context).inflate(R.layout.receipt_layout, parent, false)) else ViewHolder(LayoutInflater.from(context).inflate(R.layout.empty_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return if (itemList.size > 0) itemList.size else 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(itemList.size > 0){
            //init
            val dbOperationManager = DbOperationManager(context)
            val rec = itemList[position]
            //revalidate
            if(rec.id == -1){
                val reval = dbOperationManager.getByFullDateString(rec.fullDateString)
                if(reval.size != 0){
                    rec.id = reval[0].id
                } else {
                    throw InvalidReceiptException("The receipt whose date is ${rec.fullDateString} is invalid.")
                }
            }
            //calendar
            val calTime = rec.ngay
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
            //number
            val numberFormatter = DecimalFormat("#,###.##")
            //layout
            if(position == 0 && itemList.size != 0 && holder.root != null){
                val layoutParams: LayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                val r: Resources = context.resources
                val px25 = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    25F,
                    r.displayMetrics
                ).toInt()
                val px16 = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16F,
                    r.displayMetrics
                ).toInt()
                layoutParams.setMargins(0, px25, 0, px16)
                holder.root.layoutParams = layoutParams
            } else if(holder.root != null) {
                val layoutParams: LayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                val r: Resources = context.resources
                val px25 = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    0F,
                    r.displayMetrics
                ).toInt()
                val px16 = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16F,
                    r.displayMetrics
                ).toInt()
                layoutParams.setMargins(0, px25, 0, px16)
                holder.root.layoutParams = layoutParams
            }
            holder.tvIndex.text = context.resources.getString(R.string.placeholder_id, position + 1)
            holder.tvId.text = context.resources.getString(R.string.placeholder_maso, rec.id)
            holder.tvTitle.text = rec.title
            holder.tvDate.text = calTime?.let { formatter.format(it) }
            holder.tvTime.text = calTime?.let { timeFormatter.format(it) }
            holder.tvPurpose.text = rec.noidung
            holder.tvFolder.text = rec.folder.ten
            holder.tvMoney.text = context.resources.getString(R.string.currency, numberFormatter.format(rec.soTien));
            holder.btnMenu.setOnClickListener{
                val popup = PopupMenu(context, it)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.menu_receipt_option, popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.action_delete -> {
                            val customAlertDialog = CustomAlertDialog(context, "Xoá hoá đơn #${rec.id}?", "Bạn vẫn có thể hoàn tác trong 10s")
                            customAlertDialog.show()
                            customAlertDialog.onYesClickAction = {
                                notifyItemRemoved(position)
                                itemList.remove(rec)
                                notifyItemRangeChanged(position, itemList.size - 1)
                                dbOperationManager.deleteReceipt(rec.id)
                                Snackbar.make(holder.root, "Đã xoá hoá đơn số #${rec.id}", Snackbar.LENGTH_SHORT)
                                    .setAction("Hoàn tác"){
                                        itemList.add(position, rec)
                                        dbOperationManager.uploadReceipt(rec)
                                        notifyItemInsertedAndRevalidate(position)
                                    }
                                    .show()

                            }
                        }
                        R.id.action_edit -> {

                        }
                    }
                    true
                }
            }
        }
    }
    fun notifyItemInsertedAndRevalidate(position: Int){
        notifyItemInserted(position)
        notifyItemRangeChanged(position, itemCount)
    }
    fun notifyItemRemovedAndRevalidate(position: Int){
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}