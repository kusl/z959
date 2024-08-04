package com.github.kusl.z959

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var cameraCapture: CameraCapture

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            // Handle the case where the user denies the permission
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraPreview()
        }
        cameraCapture = CameraCapture(this, TextureView(this))
    }

    @Composable
    fun CameraPreview() {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    TextureView(context).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                startCamera()
                            }

                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                cameraCapture.stopCamera()
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraCapture.startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
