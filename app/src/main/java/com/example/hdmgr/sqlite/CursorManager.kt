package com.example.hdmgr.sqlite

import android.database.Cursor
import com.example.hdmgr.classes.Folder
import com.example.hdmgr.classes.Receipt
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CursorManager {
    companion object{
        fun convertToReceiptArray(cursor: Cursor): ArrayList<Receipt>{
            val arr = ArrayList<Receipt>()
            var receipt: Receipt = Receipt();
            while (cursor.moveToNext()){
                try {
                    val dateStringHd = cursor.getString(2)
                    receipt.fullDateString = dateStringHd
                    val date = convertToDate(dateStringHd)
                    receipt = Receipt(cursor.getInt(0), cursor.getString(1), date, cursor.getString(4), cursor.getInt(3))
                    val dateStringFd = cursor.getString(8)
                    val dateFd = convertToDate(dateStringFd)
                    val folder: Folder = Folder(cursor.getInt(6), cursor.getString(7), dateFd)
                    receipt.folder = folder
                    arr.add(receipt)
                } catch (ex: Exception){
                    arr.add(receipt)
                    continue
                }
            }
            cursor.close()
            return arr
        }
        private fun convertToDate(dateString: String): Date?{
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return formatter.parse(dateString)
        }
        fun convertToFolderArray(cursor: Cursor): ArrayList<Folder>{
            val arr: ArrayList<Folder> = ArrayList()
            while (cursor.moveToNext()){
                val date = convertToDate(cursor.getString(2))
                arr.add(Folder(cursor.getInt(0), cursor.getString(1), date))
            }
            return arr
        }
    }
}