package com.example.hdmgr

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hdmgr.adapters.ReceiptAdapter
import com.example.hdmgr.classes.Receipt
import com.example.hdmgr.databinding.ActivityMainBinding
import com.example.hdmgr.dialogs.OnUploadFinished
import com.example.hdmgr.dialogs.UploadDialog
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
    private var numCount = 0
    private var moneyCount = 0
    private var mode = 1
    private var currentSearchQuery = ""
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
        //Layout stuff: For the upload dialog
        var uploadDialog: UploadDialog
        binding.btnAdd.setOnClickListener{
            uploadDialog = UploadDialog(this)
            uploadDialog.setOnUploadFinishedListener(object : OnUploadFinished{
                override fun onFinished(receipt: Receipt) {
                    //upload finished
                    recArr.add(0, receipt)
                    receiptAdapter.notifyItemInsertedAndRevalidate(0)
                    rcvRec.scrollToPosition(0)
                    refreshRecyclerView(count = numCount + 1, moneyCount = moneyCount + receipt.soTien, alsoUpdateAdapter = false)
                    Toast.makeText(applicationContext, "Đăng tải thành công!", Toast.LENGTH_SHORT).show()
                }
            })
            uploadDialog.show()
        }
        //layout stuff: search
        binding.edtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.edtSearch.text.toString().isEmpty()){
                    when(mode){
                        0 -> changeToAllData()
                        1 -> changeToMonthlyData()
                        2 -> changeToYearlyData()
                    }
                    return
                }
                val arrLength = recArr.size
                recArr.clear()
                val searchQuery = binding.edtSearch.text.toString()
                currentSearchQuery = searchQuery
                recArr.addAll(dbOperationManager.getBySearchQuery(searchQuery))
                val (total, sum) = dbOperationManager.getStatsBySearchQuery(searchQuery)
                refreshRecyclerView(ReceiptAdapter(this@MainActivity, recArr), recreateAdapter = true, count = total, moneyCount = sum)
                addOnScrollToBottomListener({
                    dbOperationManager.getBySearchQuery(binding.edtSearch.text.toString(), limitOffset = it)
                })
                binding.tvTitle.text = "Tìm kiếm"
            }

        })
    }
    //TODO: Refactor this
    //refresh the recyclerview
    private fun refreshRecyclerView(adapter: ReceiptAdapter = receiptAdapter, isNotAll: Boolean = false, count: Int = 0, moneyCount: Int = 0, alsoUpdateAdapter: Boolean = true, recreateAdapter: Boolean = false){
        Log.d("D", "Swapping adapter")
        numCount = count
        this.moneyCount = moneyCount
        if(alsoUpdateAdapter){
            if(rcvRec.adapter == null || recreateAdapter){
                rcvRec.adapter = adapter
                layoutManager = object : LinearLayoutManager(this){
                    override fun canScrollVertically(): Boolean {
                        return true
                    }
                }
                rcvRec.layoutManager = layoutManager
            } else {
                rcvRec.adapter = adapter
                receiptAdapter = adapter
                rcvRec.adapter!!.notifyItemRangeChanged(0, receiptAdapter.itemCount)
            }
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
        binding.tvTitle.text = "Tháng này"
        var curOffset = 0
        recArr = prepareMonthlyData(cal)
        receiptAdapter = ReceiptAdapter(this, recArr)
        val (count, moneyCount) = dbOperationManager.getAllItemCountAndMoneySum(month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0'), year = cal.get(Calendar.YEAR).toString())
        refreshRecyclerView(receiptAdapter, true, count = count, moneyCount = moneyCount)
        addOnScrollToBottomListener({
            prepareMonthlyData(offset = it)
        })
    }
    //change to all data
    private fun changeToAllData(){
        binding.tvTitle.text = "Tất cả"
        val (total, sum) = dbOperationManager.getAllItemCountAndMoneySum()
        mode = 0
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
        binding.tvTitle.text = "Năm nay"
        recArr = prepareYearlyData(cal)
        mode = 2
        receiptAdapter = ReceiptAdapter(this, recArr)
        val (count, moneyCount) = dbOperationManager.getAllItemCountAndMoneySum(year = cal.get(Calendar.YEAR).toString())
        refreshRecyclerView(receiptAdapter, count = count, moneyCount = moneyCount)
        addOnScrollToBottomListener({
            prepareYearlyData(cal, it, lazy = true)
        })
    }
    //lazy loading
    private fun addOnScrollToBottomListener(fetchData: (curOffset: Int) -> ArrayList<Receipt>, offset: Int = 0){
        rcvRec.clearOnScrollListeners()
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
                        receiptAdapter.notifyItemRangeChanged(lastVisiblePos + 1, recArr.size)
                    }, 500)
                }
            }
        })
    }
}