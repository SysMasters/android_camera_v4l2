package com.hsj.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.hsj.camera.CameraAPI;
import com.hsj.camera.IFrameCallback;

import java.nio.ByteBuffer;
import java.util.Collection;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private SurfaceView surface;

    private CameraAPI camera;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        surface = findViewById(R.id.surfaceView);
        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                create();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void create() {
        stop();
        destroy();
        CameraAPI camera = new CameraAPI();

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Collection<UsbDevice> values = usbManager.getDeviceList().values();
        int size = values.size();
        if (size == 0) {
            showToast("No Usb device to be found");
            return;
        }
        // stop and destroy
        stop();
        destroy();
        UsbDevice usbDevice = values.stream().filter(device -> device.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_VIDEO).findFirst().get();
        int pid = usbDevice.getProductId();
        int vid = usbDevice.getVendorId();

        boolean ret = camera.create(pid, vid);
        int[][] supportFrameSize = camera.getSupportFrameSize();
        if (supportFrameSize == null || supportFrameSize.length == 0) {
            showToast("Get support preview size failed.");
        } else {
            final int index = supportFrameSize.length / 2;
            final int width = supportFrameSize[index][0];
            final int height = supportFrameSize[index][1];
            Log.d(TAG, "width=" + width + ", height=" + height);
            if (ret) ret = camera.setFrameSize(width, height, CameraAPI.FRAME_FORMAT_MJPEG);
            if (ret) this.camera = camera;
        }
        start();
    }


    private void start() {
        if (this.camera != null) {
            if (surface != null) this.camera.setPreview(surface.getHolder().getSurface());
            this.camera.setFrameCallback(new IFrameCallback() {
                @Override
                public void onFrame(ByteBuffer data) {
                    Log.i(TAG, "=====================data:" + data.toString());
                }
            });
            this.camera.start();
        } else {
            showToast("Camera have not create");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private void stop() {
        if (this.camera != null) {
            this.camera.stop();
        }
    }

    private void destroy() {
        if (this.camera != null) {
            this.camera.destroy();
            this.camera = null;
        }
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
