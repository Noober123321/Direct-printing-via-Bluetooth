Bluetooth print for Android

A test Android application for direct printing on ESC/POS compatible thermal printers via Bluetooth. This project demonstrates how to handle Bluetooth connections, render data to a printable format, and send it to a Bluetooth printer. 

How It Works:
  🔹 Direct Bluetooth Printing
    The app finds the first paired thermal printer, establishes a connection, and sends data directly. It's designed to work with modern Android versions, handling all necessary Bluetooth permissions. 
  🔹 Dual Printing Modes
    Plain Text: Generates a simple, text-based sales receipt using standard ESC/POS commands for styling (bold, alignment, etc.).
    HTML to Image: Reliably converts an HTML string into a Bitmap image. It uses a robust "brute-force" rendering method where a WebView is programmatically created with a fixed width (384px) and a large, fixed height to ensure all content is captured correctly before printing.
  🔹 Advanced Bluetooth Handling
    Dynamic UUID Discovery: Automatically queries the printer for its supported connection UUIDs, ensuring greater compatibility. 
    Chunked Data Transfer: Prevents printer buffer overflows by sending large image data in small, sequential packets. 

Key Components:
  • PrintController: The "brain" of the app, which manages the printing logic for both text and HTML.
  • BluetoothController: A low-level class that handles device discovery, connection, and data transmission.
  • EscPosConverter: A specialized utility that converts a Bitmap into a raw ByteArray using the ESC/POS GS v 0 raster graphics command.

Acknowledgements 🙏
A significant portion of the logic for the bitmap-to-ESC/POS conversion EscPosConverter.kt and for the dynamic UUID discovery in BluetoothController.kt was carefully analyzed and adapted from the excellent open-source library DantSu/ESCPOS-ThermalPrinter-Android. The original Java code was translated to Kotlin and integrated into this project's architecture. 

GitHub Repository: https://github.com/DantSu/ESCPOS-ThermalPrinter-Android
