package com.example.hdmgr.dialogs

import com.example.hdmgr.classes.Receipt

interface OnUploadFinished {
    fun onFinished(receipt: Receipt)
}