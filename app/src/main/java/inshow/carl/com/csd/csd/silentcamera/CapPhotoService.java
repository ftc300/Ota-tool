package inshow.carl.com.csd.csd.silentcamera;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/10/16
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */

public class CapPhotoService extends Service {
    private SurfaceHolder sHolder;
    private Camera mCamera;
    private Camera.Parameters parameters;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CAM", "start");
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Thread myThread = null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (Camera.getNumberOfCameras() >= 2) {
//
//            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//        }
//
//        if (Camera.getNumberOfCameras() < 2) {
//
//            mCamera = Camera.open();
//        }
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        SurfaceView sv = new SurfaceView(getApplicationContext());


        try {
            mCamera.setPreviewDisplay(sv.getHolder());
            parameters = mCamera.getParameters();
            mCamera.setParameters(parameters);
            SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
            mCamera.setPreviewTexture(st);
            mCamera.startPreview();

            mCamera.takePicture(null, null, mCall);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sHolder = sv.getHolder();
//        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        return super.onStartCommand(intent, flags, startId);
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(final byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            try {

                File sd = new File(Environment.getExternalStorageDirectory(), "A");
                if (!sd.exists()) {
                    sd.mkdirs();
                    Log.i("FO", "folder" + Environment.getExternalStorageDirectory());
                }

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String tar = (sdf.format(cal.getTime()));

                outStream = new FileOutputStream(sd + "/"+  tar + ".jpg");
                outStream.write(data);
                outStream.close();

                Log.i("CAM", data.length + " byte written to:" + sd + tar + ".jpg");
                camkapa(sHolder);

            } catch (FileNotFoundException e) {
                Log.d("CAM", e.getMessage());
            } catch (IOException e) {
                Log.d("CAM", e.getMessage());
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void camkapa(SurfaceHolder sHolder) {
        if (null == mCamera)
            return;
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        Log.i("CAM", " closed");
    }

}