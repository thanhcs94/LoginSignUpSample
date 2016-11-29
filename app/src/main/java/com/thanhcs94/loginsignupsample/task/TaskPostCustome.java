package com.thanhcs94.loginsignupsample.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thanhcs94 on 9/22/2016.
 */
public class TaskPostCustome extends AsyncTask<Void, Void , Void> {
    String TAG = "TaskPostCustome";
    private final OkHttpClient client = new OkHttpClient();
    Response response = null;
    ProgressDialog dialog;
    Activity activity;
    PostCustome loginListener;
    String token;
    String url;
    HashMap<String, String> hashParams;
    int TRACK = -9999999;
    public interface PostCustome {
        public void checkDataFromTask(Response respond, int TRACK);
    }

    public TaskPostCustome(Activity activity , String url, String token , HashMap<String, String> hashParams, int TRACK){
        this.activity = activity;
        this.token = token;
        this.hashParams = hashParams;
        this.url = url;
        this.TRACK = TRACK;
        loginListener = (PostCustome) activity;
        Log.wtf("TASK", TRACK+"");
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            dialog = new ProgressDialog(activity);
            dialog.setMessage("Đang tải...");
            dialog.setCancelable(true);
            dialog.show();
        }catch (Exception e){
            Log.wtf(TAG, "dialog can't show");
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        FormEncodingBuilder mForm = new FormEncodingBuilder();
        for (Map.Entry<String,String> entry : hashParams.entrySet()) {
                System.out.printf("%s -> %s%n", entry.getKey(), entry.getValue());
                mForm.add(entry.getKey(), entry.getValue());
            }

        RequestBody formBody = mForm.build();
        Request request = null;
        if(token!=null) {
           request =new Request.Builder()
                    .url(url).post(formBody)
                    .addHeader("token", token)
                    .build();
        }else{
            request =new Request.Builder()
                    .url(url).post(formBody)
                    .build();
        }
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response.toString());
        } catch (Exception e) {
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(dialog!=null)
        dialog.dismiss();
        loginListener.checkDataFromTask(response, TRACK);
        super.onPostExecute(aVoid);
    }
}