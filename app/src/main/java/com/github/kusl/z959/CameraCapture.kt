package com.github.kusl.z959

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraCapture(private val context: Context, private val textureView: TextureView) {
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val backgroundHandler: Handler
    private val backgroundThread: HandlerThread

    init {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    fun startCamera() {
        val cameraId = cameraManager.cameraIdList[0]
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return
        }
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCameraPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        }, backgroundHandler)
    }

    private fun createCameraPreviewSession() {
        val texture = textureView.surfaceTexture
        texture?.setDefaultBufferSize(textureView.width, textureView.height)
        val surface = Surface(texture)
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                // Handle failure
            }
        }, backgroundHandler)
    }

    fun stopCamera() {
        captureSession.close()
        cameraDevice.close()
        backgroundThread.quitSafely()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}
