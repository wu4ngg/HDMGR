package com.example.hdmgr.classes

import java.util.Date

class Receipt(
    var id: Int = -1,
    var title: String? = "",
    var ngay: Date? = Date(),
    var noidung: String? = "",
    var fullDateString: String = ""
) {
    var folder: Folder = Folder()
    var soTien: Int = 0
        set(value) {
            field = if(value < 0){
                0
            } else value
        }
    constructor(id: Int, title: String, ngay: Date?, noidung: String, soTien: Int) : this(id, title, ngay, noidung){
        this.soTien = soTien;
    }
}