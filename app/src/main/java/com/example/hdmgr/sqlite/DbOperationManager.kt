package com.example.hdmgr.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.example.hdmgr.classes.Receipt
import kotlin.math.log

class DbOperationManager(context: Context) {
    private val database: Database = Database(context)
    private val readableDatabase: SQLiteDatabase = database.readableDatabase
    //Receipts
    fun getReceipts(limitOffset: Int = 0, limitCount: Int = 5): ArrayList<Receipt> {
        val rawResult = readableDatabase.rawQuery(
            "SELECT * FROM ${Database.hoaDonTable} h, ${Database.thumucHoaDon} t WHERE h.${Database.thumucHoaDon} = t.${Database.identifier} LIMIT $limitOffset,$limitCount",
            null
        )
        return CursorManager.convertToReceiptArray(rawResult)
    }
    fun getAllItemCountAndMoneySum(day: String = "", month: String = "", year: String = ""): Pair<Int, Int>{
        val query = "SELECT COUNT(*), SUM(SOTIEN) FROM HoaDon h ${if(day != "") "WHERE STRFTIME('%d', H.NGAY) = $day" else ""} ${if(month != "") "${if(day != "") "AND" else "WHERE"} STRFTIME('%m', H.NGAY) = $month" else ""} ${if(year != "") "${if(day != "" || month != "") "AND" else "WHERE"} STRFTIME('%Y', H.NGAY) = $year" else ""}"
        val rawResult = readableDatabase.rawQuery("SELECT COUNT(*), SUM(SOTIEN) FROM HoaDon h ${if(day != "") "WHERE STRFTIME('%d', H.NGAY) = '$day'" else ""} ${if(month != "") "${if(day != "") "AND" else "WHERE"} STRFTIME('%m', H.NGAY) = '$month'" else ""} ${if(year != "") "${if(day != "" || month != "") "AND" else "WHERE"} STRFTIME('%Y', H.NGAY) = '$year'" else ""}", null)
        if (rawResult.moveToNext()){
            return rawResult.getInt(0) to rawResult.getInt(1)
        }
        rawResult.close()
        return 0 to 0
    }
    //STRFTIME('%m', DATE('now')) = STRFTIME('%m', NGAY) AND STRFTIME('%Y', DATE('now')) = STRFTIME('%Y', NGAY)
    fun getReceiptsWithOtherConditions(condition: String) : ArrayList<Receipt>{
        val rawResult = readableDatabase.rawQuery(
            "SELECT * FROM ${Database.hoaDonTable} h, ${Database.thumucHoaDon} t WHERE h.${Database.thumucHoaDon} = t.${Database.identifier} AND $condition",
            null
        )
        return CursorManager.convertToReceiptArray(rawResult)
    }
    fun getReceiptsByMonth(month: String, year: String, limitOffset: Int = 0, limitCount: Int = 5) : ArrayList<Receipt>{
        Log.d("SuperLongDebugTag", "getReceiptsByMonth: month = $month, year = $year")
        val rawResult = readableDatabase.rawQuery(
            "SELECT * FROM ${Database.hoaDonTable} h, ${Database.thumucHoaDon} t WHERE h.${Database.thumucHoaDon} = t.${Database.identifier} AND (STRFTIME('%m', H.NGAY) = '$month' AND STRFTIME('%Y', H.NGAY) = '$year') LIMIT $limitOffset,$limitCount",
            null
        )
        return CursorManager.convertToReceiptArray(rawResult)
    }
    fun getReceiptsByYear(year: String, limitOffset: Int = 0, limitCount: Int = 5) : ArrayList<Receipt>{
        Log.d("SuperLongDebugTag", "SELECT * FROM ${Database.hoaDonTable} h, ${Database.thumucHoaDon} t WHERE h.${Database.thumucHoaDon} = t.${Database.identifier} AND STRFTIME('%Y', H.NGAY) = '$year' LIMIT $limitOffset,$limitCount")
        val rawResult = readableDatabase.rawQuery(
            "SELECT * FROM ${Database.hoaDonTable} h, ${Database.thumucHoaDon} t WHERE h.${Database.thumucHoaDon} = t.${Database.identifier} AND STRFTIME('%Y', H.NGAY) = '$year' LIMIT $limitOffset,$limitCount",
            null
        )
        return CursorManager.convertToReceiptArray(rawResult)
    }

    fun getReceiptsByDate(date: String) : ArrayList<Receipt>{
        val rawResult = readableDatabase.rawQuery(
            "SELECT * FROM ${Database.hoaDonTable} h, ${Database.thumucHoaDon} t WHERE h.${Database.thumucHoaDon} = t.${Database.identifier} AND DATE(H.NGAY) = DATE('$date')",
            null
        )
        return CursorManager.convertToReceiptArray(rawResult)
    }
}