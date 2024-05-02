package com.example.hdmgr.classes

import java.util.Date

class Folder (
    var id: Int = -1,
    var ten: String = "",
    var ngay: Date? = Date(),
    var hoaDon: ArrayList<Receipt> = ArrayList()
) {

}