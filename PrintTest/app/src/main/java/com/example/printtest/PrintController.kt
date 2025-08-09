package com.example.printtest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.graphics.createBitmap

class PrintController(private val activity: Activity)
{
    val blManager = BluetoothController(activity)

    @SuppressLint("DefaultLocale")
    fun printSampleReceipt()
    {
        //Bill info
        val items = listOf(
            ReceiptItem("Item 1", 2, 150.0),
            ReceiptItem("Item 2", 4, 75.99)
        )
        val total = items.sumOf { it.quantity * it.price }
        val receipt = Receipt("My Shop", items, total)

        //ESC/POS commands
        val init = byteArrayOf(0x1B, 0x40)             //printer initialization
        val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)       //center alignment
        val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)       //left alignment
        val boldOn = byteArrayOf(0x1B, 0x45, 0x01)       //enable bold
        val boldOff = byteArrayOf(0x1B, 0x45, 0x00)       //disable bold
        val underlineOn = byteArrayOf(0x1B, 0x2D, 0x01)       //enable underline
        val underlineOff = byteArrayOf(0x1B, 0x2D, 0x00)       //disable underline
        val lineFeed = byteArrayOf(0x0A)                   //next line
        val feedLines = byteArrayOf(0x0A, 0x0A, 0x0A)       //next line x3
        val cut = byteArrayOf(0x1D, 0x56, 0x42, 0x00) //printer cut

        //creating full bill using String
        val sb = StringBuilder().apply {
            //first text then commands
            items.forEach {
                append(
                    String.format(
                        "%-16s%3d%8.2f\n",
                        it.name.take(16), it.quantity, it.price
                    )
                )
            }
            append("\nTotal: ${"%.2f".format(receipt.total)}\n")
            append("Thanks for purchase!\n")
        }
        //compiling text to ASCII bytes
        val textBytes = sb.toString().toByteArray(Charsets.US_ASCII)

        //form final packet
        val headerBytes = receipt.merchantName.toByteArray(Charsets.US_ASCII)
        val packet = init +
                alignCenter +
                boldOn + underlineOn +
                headerBytes + lineFeed +
                underlineOff + boldOff +
                alignLeft +
                textBytes +
                feedLines +
                cut

        //send to the printer
        sendDataToPrint(packet)
    }

    private fun sendDataToPrint(packet: ByteArray)
    {
        val mac = blManager.findFirstPrinterMac(activity)
        if (mac != null) {
            blManager.sendRawData(activity, mac, packet)
        } else {
            Toast.makeText(activity, "Printer not found", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun printHtmlContent(htmlContent: String) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val renderWidth = 384
        val renderHeight = 4000

        val webView = WebView(activity).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            layoutParams = ViewGroup.LayoutParams(renderWidth, renderHeight)
            visibility = View.GONE
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val contentHeight = view.contentHeight
                    if (contentHeight > 0) {
                        val bitmap = createBitmap(renderWidth, contentHeight)
                        val canvas = Canvas(bitmap)
                        view.draw(canvas)

                        printBitmap(bitmap)
                    } else {
                        Log.e("PrintController", "WebView contentHeight is zero.")
                    }

                    rootView.removeView(view)
                    view.destroy()
                }, 1000)
            }
        }

        rootView.addView(webView)
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }


    fun printBitmap(bitmap: Bitmap)
    {
        val escBytes = EscPosConverter.bitmapToBytes(bitmap)
        val init = byteArrayOf(0x1B, 0x40)
        val cut = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
        val feed = "\n\n\n".toByteArray()

        val packet = init + escBytes + feed + cut

        sendDataToPrint(packet)
    }
}