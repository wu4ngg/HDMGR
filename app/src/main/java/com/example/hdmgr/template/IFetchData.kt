package com.example.hdmgr.template

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hdmgr.adapters.ReceiptAdapter
import com.example.hdmgr.classes.Folder
import com.example.hdmgr.classes.Receipt
import java.util.Calendar

abstract class IFetchData(var context: Context){
    val list: ArrayList<Receipt> = ArrayList()
    abstract fun fetchDataReceipt(cal: Calendar, offset: Int, countPerOffset: Int): ArrayList<Receipt>
    protected fun refreshRecyclerView(rc: RecyclerView, adapter: ReceiptAdapter){
        if(rc.adapter == null){
            rc.adapter = adapter
            val layoutManager = object : LinearLayoutManager(context){
                override fun canScrollVertically(): Boolean {
                    return true
                }
            }
            rc.layoutManager = layoutManager
        }
        rc.swapAdapter(adapter, false)
    }
    protected fun setupLazyLoading(){
        //TODO: Refactor
    }
    abstract fun updateView()
    fun fetchData(rc: RecyclerView, cal: Calendar = Calendar.getInstance(), offset: Int = 0, countPerOffset: Int = 5){
        val arr = fetchDataReceipt(cal, offset, countPerOffset)
        refreshRecyclerView(rc, ReceiptAdapter(context, arr))
        setupLazyLoading()
    }
    fun fetchDataAndSetupThings(rc: RecyclerView, cal: Calendar = Calendar.getInstance(), offset: Int = 0, countPerOffset: Int = 5){

    }
}