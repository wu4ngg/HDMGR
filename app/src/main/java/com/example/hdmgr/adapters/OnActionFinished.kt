package com.example.hdmgr.adapters

import com.example.hdmgr.classes.Receipt

interface OnActionFinished{
    fun onFinished(receipt: Receipt)
}