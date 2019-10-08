package com.example.srcwh

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_qr.*
import android.app.Activity
import android.content.Intent
import android.view.WindowManager

class QRActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 2

    private lateinit var surfaceView: SurfaceView
    private lateinit var textView: TextView

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    private var isComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        surfaceView = qr_view
        textView = qr_text

        // Make status and navigation bars transparent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build()
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodeList = detections.detectedItems
                if (!isComplete && barcodeList.size() > 0) {
                    Log.d("QR", "Found ${barcodeList.size()} barcode(s)")

                    val qr = barcodeList.valueAt(0).rawValue

                    // Make sure the QR code is legit
                    if (QR_REGEX.toRegex().matches(qr)) {
                        isComplete = true

                        val resultIntent = Intent()
                        resultIntent.putExtra("qr", qr)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        })

        cameraSource =
            CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1024, 768)
                .setRequestedFps(24f)
                .setAutoFocusEnabled(true)
                .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (ContextCompat.checkSelfPermission(this@QRActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraSource.start(holder)
                } else {
                    ActivityCompat.requestPermissions(this@QRActivity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraSource.start(surfaceView.holder)
            } else {
                Toast.makeText(this, "Scanner will not work without camera permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        barcodeDetector.release()
        cameraSource.stop()
        cameraSource.release()
    }
}
