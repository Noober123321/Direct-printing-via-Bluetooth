package com.example.printtest

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var controller: PrintController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controller = PrintController(this)

        // Ваш HTML-контент
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { 
                        width: 384px; 
                        margin: 0; 
                        padding: 0; 
                        font-family: sans-serif; 
                        text-align: center; 
                        background: #fff; 
                    }
                    h1 { font-size: 22px; margin: 10px 0 5px 0; }
                    h2 { font-size: 16px; margin: 0; }
                    p { font-size: 14px; margin: 5px 0; }
                </style>
            </head>
            <body>
              <h1>Store Receipt</h1>
              <h2>Acme #1234</h2>
              <p>Item A: 2 - $20.00</p>
              <p>Item B: 1 - $13.50</p>
              <p><strong>Total: $36.18</strong></p>
            </body>
            </html>
            """.trimIndent()

        findViewById<Button>(R.id.printButton).setOnClickListener {
            controller.printHtmlContent(htmlContent)      }
    }
}