package com.example.hdmgr.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(val context: Context) : SQLiteOpenHelper(context, "main.db", null, 1) {
    companion object{
        const val hoaDonTable = "HoaDon"
        const val thuMucTable = "ThuMuc"
        const val identifier = "id"
        const val ten = "ten"
        const val ngay = "ngay"
        const val sotienHoaDon = "sotien"
        const val thumucHoaDon = "thumuc"
    }
    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("CREATE TABLE HOADON (" +
                "ID INTEGER UNIQUE PRIMARY KEY," +
                "TEN NVARCHAR(100) NOT NULL," +
                "NGAY DATE NOT NULL," +
                "SOTIEN INTEGER NOT NULL," +
                "NOIDUNG NVARCHAR(1024) NOT NULL," +
                "THUMUC INTEGER REFERENCES THUMUC(ID)" +
                ");")
        p0?.execSQL("CREATE TABLE THUMUC (" +
                "ID INTEGER UNIQUE PRIMARY KEY," +
                "TEN NVARCHAR(100) NOT NULL," +
                "NGAY NVARCHAR(100) NOT NULL" +
                ");")
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
}