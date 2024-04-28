package com.example.hdmgr.classes

import java.util.Date

class Folder (
    var id: Int,
    var ten: String,
    var ngay: Date?,
    var hoaDon: ArrayList<Receipt> = ArrayList()
) {

}