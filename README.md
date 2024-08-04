# For the ZTE Z959
which runs Android 5.1.1 on API level 22 
I want to write a simple app for android api 22 lollipop that takes content from camera and serves it over rtsp to local network

To create an app that captures content from the camera and serves it over RTSP on a local network for Android API 22 (Lollipop), you'll need to integrate camera functionality and an RTSP server. Here's a basic outline to get you started:

1. **Add Camera Permissions**: Update your `AndroidManifest.xml` to include camera permissions.
2. **Capture Camera Content**: Use the Camera2 API to capture video.
3. **Serve Video over RTSP**: Use an RTSP library to stream the captured video.
