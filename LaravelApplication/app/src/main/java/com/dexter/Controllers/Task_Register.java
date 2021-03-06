package com.dexter.Controllers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.dexter.Constants.Constant_URL;
import com.dexter.Models.Model_Application;
import com.dexter.Receivers.Receiver_Register;
import com.dexter.Utils.ResourceManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dexter.n on 08/06/2015.
 */
public class Task_Register extends AsyncTask<Void, Void, Boolean> {

    private final JSONObject sendJsonObj;
    private final Context mContext;
    private HttpURLConnection urlConnect;
    private String str_result;
    private StringBuilder strBuilder;
    private InputStream is;
    private String outputStr;

    public Task_Register(Context context, JSONObject jsonObj)
    {
        mContext = context;
        sendJsonObj = jsonObj;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            URL url = new URL("http://dexter-laravelframe.rhcloud.com/register");

            urlConnect = (HttpURLConnection) url.openConnection();
            urlConnect.setRequestMethod("POST");
            urlConnect.setDoOutput(true);
            urlConnect.setDoInput(true);
            urlConnect.setConnectTimeout(10000);
            urlConnect.setRequestProperty("Content-Type", "application/json");
//            urlConnect.setRequestProperty("x-csrf-token", Model_Application.token_csrf);
            urlConnect.connect();

            OutputStreamWriter outWrite = new OutputStreamWriter(urlConnect.getOutputStream());
            outWrite.write(sendJsonObj.toString());
            outWrite.flush();
            outWrite.close();

            int code = urlConnect.getResponseCode();

            if (code == 200)
            {
                is = urlConnect.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                outputStr = builder.toString();

                getData();
            }

            urlConnect.disconnect();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getData() {
        JSONObject jsonObj = null;
        try {
            jsonObj = (JSONObject) new JSONTokener(outputStr).nextValue();

            if (jsonObj.has("id")) {
                ResourceManager.UserProfile.id = jsonObj.getInt("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Intent broadcastRegister = new Intent();
        broadcastRegister.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastRegister.setAction(Receiver_Register.ACTION_REGISTER);

        mContext.sendBroadcast(broadcastRegister);
    }
}
