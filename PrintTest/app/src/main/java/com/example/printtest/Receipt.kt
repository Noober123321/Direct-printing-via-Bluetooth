package com.example.printtest

data class Receipt(val merchantName: String, val items: List<ReceiptItem>, val total: Double)
