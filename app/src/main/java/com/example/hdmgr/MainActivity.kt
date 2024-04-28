package com.example.hdmgr

import android.icu.number.NumberFormatter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hdmgr.adapters.ReceiptAdapter
import com.example.hdmgr.classes.Receipt
import com.example.hdmgr.databinding.ActivityMainBinding
import com.example.hdmgr.etc.EndlessRecyclerViewScrollListener
import com.example.hdmgr.sqlite.DbOperationManager
import java.text.DecimalFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbOperationManager: DbOperationManager
    private lateinit var rcvRec: RecyclerView
    private lateinit var sumNumDate: TextView
    private lateinit var sumNumDateLabel: TextView
    private lateinit var sumMoneyDateLabel: TextView
    private lateinit var layoutManager: LinearLayoutManager
    private var recArr: ArrayList<Receipt> = ArrayList()
    private lateinit var receiptAdapter: ReceiptAdapter
    private lateinit var sumMoneyCount: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        //init
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        dbOperationManager = DbOperationManager(this)
        //layout stuff (for the layout)
        val drawerLayout = binding.root
        val navigationView = binding.sidebar
        sumNumDate = binding.sumNumDate
        sumMoneyDateLabel = binding.sumMoneyDateLabel
        sumNumDateLabel = binding.sumNumDateLabel
        sumMoneyCount = binding.sumMoneyDate
        binding.btnMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }
        rcvRec = binding.rcvItems
        changeToMonthlyData()
        rcvRec.layoutManager = layoutManager
        //layout stuff (for the dropdown menus)
        binding.timeSelector.setOnClickListener {
            var curOffset = 0;
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.menu_title_options, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                    it -> run {
                when (it.itemId) {
                    R.id.option_all -> {
                        binding.tvTitle.text = it.title
                        changeToAllData()
                    }
                    R.id.option_this_month -> {
                        binding.tvTitle.text = it.title
                        changeToMonthlyData()
                    }
                    R.id.option_this_year -> {
                        binding.tvTitle.text = it.title
                        changeToYearlyData()
                    }
                    else -> {}
                }
            }
                true
            }
            popupMenu.show()
        }
    }
    //TODO: Refactor this
    //refresh the recyclerview
    private fun refreshRecyclerView(adapter: ReceiptAdapter, isNotAll: Boolean = false, count: Int = 0, moneyCount: Int = 0){
        Log.d("D", "Swapping adapter")
        if(rcvRec.adapter == null){
            rcvRec.adapter = adapter
            layoutManager = object : LinearLayoutManager(this){
                override fun canScrollVertically(): Boolean {
                    return true
                }
            }
            rcvRec.layoutManager = layoutManager
        } else {
            rcvRec.swapAdapter(adapter, false)
        }
        val formatter = DecimalFormat("#,###.##")
        sumNumDate.text = formatter.format(count)
        sumMoneyCount.text = resources.getString(R.string.currency, formatter.format(moneyCount))
        if(!isNotAll){
            sumNumDateLabel.text = resources.getString(R.string.sum_num_date, "")
            sumMoneyDateLabel.text = resources.getString(R.string.sum_money_date, "")
        }
        Log.d("D", "Done")
    }
    //monthly data (with optional calendar parameter)
    private fun prepareMonthlyData(cal: Calendar = Calendar.getInstance(), lazy: Boolean = false, offset: Int = 0, countPerOffset: Int = 5): ArrayList<Receipt> {
        val recArr = dbOperationManager.getReceiptsByMonth(
            (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0'),
            cal.get(Calendar.YEAR).toString(),
            offset,
            countPerOffset
        )
        if(!lazy){
            sumNumDateLabel.text = resources.getString(R.string.sum_num_date, "tháng ${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}")
            sumNumDate.text = recArr.size.toString()
            sumMoneyDateLabel.text = resources.getString(R.string.sum_money_date, "tháng ${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}")
        }
        return recArr
    }
    //prepare yearly data
    private fun prepareYearlyData(cal: Calendar = Calendar.getInstance(), offset: Int = 0, countPerOffset: Int = 5, lazy: Boolean = false): ArrayList<Receipt>{
        val recArr = dbOperationManager.getReceiptsByYear(cal.get(Calendar.YEAR).toString(), offset, countPerOffset)
        if(!lazy){
            sumNumDateLabel.text = resources.getString(R.string.sum_num_date, "năm ${cal.get(Calendar.YEAR)}")
            sumNumDate.text = recArr.size.toString()
            sumMoneyDateLabel.text = resources.getString(R.string.sum_money_date, "năm ${cal.get(Calendar.YEAR)}")
        }
        return recArr
    }
    //change to monthly data
    private fun changeToMonthlyData(cal: Calendar = Calendar.getInstance()){
        var curOffset = 0
        val recArr = prepareMonthlyData(cal)
        receiptAdapter = ReceiptAdapter(this, recArr)
        val (count, moneyCount) = dbOperationManager.getAllItemCountAndMoneySum(month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0'), year = cal.get(Calendar.YEAR).toString())
        refreshRecyclerView(receiptAdapter, true, count = count, moneyCount = moneyCount)
        addOnScrollToBottomListener({
            prepareMonthlyData(offset = it)
        })
    }
    //change to all data
    private fun changeToAllData(){
        val (total, sum) = dbOperationManager.getAllItemCountAndMoneySum()
        var curOffset = 0
        recArr = dbOperationManager.getReceipts()
        receiptAdapter = ReceiptAdapter(this, recArr)
        refreshRecyclerView(receiptAdapter, count = total, moneyCount = sum)
        addOnScrollToBottomListener({
            dbOperationManager.getReceipts(it)
        })
    }
    //change to yearlyData
    private fun changeToYearlyData(cal: Calendar = Calendar.getInstance()){
        prepareYearlyData(cal)
        receiptAdapter = ReceiptAdapter(this, recArr)
        val (count, moneyCount) = dbOperationManager.getAllItemCountAndMoneySum(year = cal.get(Calendar.YEAR).toString())
        refreshRecyclerView(receiptAdapter, count = count, moneyCount = moneyCount)
        addOnScrollToBottomListener({
            prepareYearlyData(cal, it, lazy = true)
        })
    }
    //lazy loading
    private fun addOnScrollToBottomListener(fetchData: (curOffset: Int) -> ArrayList<Receipt>, offset: Int = 0){
        rcvRec.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            var curOffset = offset
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisiblePos = layoutManager.findLastVisibleItemPosition()
                if(lastVisiblePos == recArr.size - 1){
                    curOffset += 5
                    recArr.addAll(fetchData(curOffset))
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        receiptAdapter.notifyItemRangeInserted(lastVisiblePos + 1, recArr.size)
                    }, 500)
                }
            }
        })
    }
}