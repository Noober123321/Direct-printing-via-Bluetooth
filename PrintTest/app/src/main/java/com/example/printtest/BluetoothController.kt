package com.example.printtest

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*


class BluetoothController(private val activity: Activity)
{
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun hasConnectPermission(context: Context): Boolean
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else { true }
    }

    fun requestConnectPermission(activity: Activity, requestCode: Int) //Ask for permission bl_con
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !hasConnectPermission(activity))
        {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                requestCode
            )
        }
   }

    private fun getAdapter(context: Context): BluetoothAdapter?
    {
        val btService = context.getSystemService(Context.BLUETOOTH_SERVICE)
                        as? android.bluetooth.BluetoothManager
        return btService?.adapter

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getDeviceUUID(device: BluetoothDevice): UUID
    {
        val uuids: Array<ParcelUuid>? = device.uuids
        if(uuids != null && uuids.isNotEmpty())
        {
            if(uuids.any() { it.uuid == SPP_UUID})
            {
                return SPP_UUID
            }
            return uuids[0].uuid
        }
        return SPP_UUID
    }

    fun findFirstPrinterMac(context: Context): String?
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e(TAG, "Missing Bluetooth_Connect")
            return null
        }

        val adapter = getAdapter(context)  ?: run {
            Log.e("BluetoothController", "BluetoothAdapter unavailable")
            return null
        }
        if(!adapter.isEnabled) return null

        val devices = adapter.bondedDevices

        val printer = devices.firstOrNull {
            val cls = it.bluetoothClass
            cls.majorDeviceClass == BluetoothClass.Device.Major.IMAGING
        } ?: devices.firstOrNull()

        return printer?.address
    }

    fun sendRawData(context: Context, mac: String?, data: ByteArray)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e(TAG, "Missing Bluetooth_Connect")
            return
        }

        val adapter = getAdapter(context) ?: return

        Thread {
            try {
                val device = adapter.getRemoteDevice(mac)
                val uuid = getDeviceUUID(device)
                val socket = device.createInsecureRfcommSocketToServiceRecord(uuid)

                socket.connect()

                socket.outputStream.use { outputStream ->
                    val chunkSize = 1024
                    val dataInputStream = data.inputStream()
                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int

                    while (dataInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        outputStream.flush()

                        Thread.sleep(50)
                    }
                }

                socket.close()
                Log.d("PRINT", "All chunks sent successfully. Total size: ${data.size}")

            } catch (e: IOException) {
                Log.e("PRINT", "Error during BT comm: ${e.message}", e)
            }
        }.start()
    }
}