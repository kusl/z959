# For the ZTE Z959
which runs Android 5.1.1 on API level 22 
I want to write a simple app for android api 22 lollipop that takes content from camera and serves it over rtsp to local network

To create an app that captures content from the camera and serves it over RTSP on a local network for Android API 22 (Lollipop), you'll need to integrate camera functionality and an RTSP server. Here's a basic outline to get you started:

1. **Add Camera Permissions**: Update your `AndroidManifest.xml` to include camera permissions.
2. **Capture Camera Content**: Use the Camera2 API to capture video.
3. **Serve Video over RTSP**: Use an RTSP library to stream the captured video.

### Step 1: Add Camera Permissions

Update your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

### Step 2: Capture Camera Content

Create a `CameraCapture` class to handle camera operations:

```kotlin
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView

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
        texture.setDefaultBufferSize(textureView.width, textureView.height)
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
}
```

### Step 3: Serve Video over RTSP

Integrate an RTSP library like `libstreaming` to serve the video. Add the library to your `build.gradle`:

```groovy
dependencies {
    implementation 'net.majorkernelpanic:streaming:4.0.3'
}
```

Create an `RTSPServer` class to handle streaming:

```kotlin
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.rtsp.RtspServer

class RTSPServer : RtspServer() {
    override fun onCreate() {
        super.onCreate()
        val session = SessionBuilder.getInstance()
            .setContext(applicationContext)
            .setAudioEncoder(SessionBuilder.AUDIO_NONE)
            .setVideoEncoder(SessionBuilder.VIDEO_H264)
            .build()
        setSession(session)
    }
}
```

Update your `AndroidManifest.xml` to include the RTSP server:

```xml
<service android:name=".RTSPServer" android:exported="false" />
```

### Putting It All Together

In your `MainActivity`, start the camera and RTSP server:

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var cameraCapture: CameraCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textureView = findViewById<TextureView>(R.id.textureView)
        cameraCapture = CameraCapture(this, textureView)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                cameraCapture.startCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                cameraCapture.stopCamera()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        startService(Intent(this, RTSPServer::class.java))
    }
}
```

This is a basic setup to get you started. You may need to handle additional details like permissions at runtime, error handling, and optimizing the video stream for your specific use case. Let me know if you need further assistance!

Source: Conversation with Copilot, 8/4/2024
(1) github.com. https://github.com/accurascan/Qatar-Android-SDK/tree/026e772b8e8ca5322861ed98112cb784780c0dda/README.md.