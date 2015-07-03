package com.codepath.apps.tumblrsnap;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.codepath.apps.tumblrsnap.fragments.PhotosFragment;
import com.codepath.apps.tumblrsnap.models.Photo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

public class TumblrUploadService extends IntentService{
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public TumblrUploadService() {
        super("Tumblr");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("vibhor", "came to service");
        byte[] data = intent.getByteArrayExtra("photo");

        TumblrClient client = new TumblrClient(getApplicationContext());

        client.createPhotoPost("vibhor0806", data, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int code, JSONObject response) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(PhotosFragment.ResponseReceiver.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("key", "data");
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onFailure(Throwable arg0) {
                Log.d("DEBUG", arg0.toString());
            }
        });


    }

    public void executeMultipartPost(byte[] data) throws Exception {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String temp = System.currentTimeMillis()+"";
            HttpPost postRequest = new HttpPost(
                    "http://api.tumblr.com/v2/blog/" + temp + "/post?type=photo&tags=cptumblrsnap");

            HttpParams params = new BasicHttpParams();
            params.setParameter("type","photo");
            params.setParameter("tags","cptumblrsnap");
            params.setParameter("data", new ByteArrayInputStream(data));

            postRequest.setParams(params);
//            ByteArrayBody bab = new ByteArrayBody(data, "forest.jpg");
//            // File file= new File("/mnt/sdcard/forest.png");
//            // FileBody bin = new FileBody(file);
//            MultipartEntity reqEntity = new MultipartEntity(
//                    HttpMultipartMode.BROWSER_COMPATIBLE);
//            reqEntity.addPart("uploaded", bab);
//            reqEntity.addPart("photoCaption", new StringBody("sfsdfsdf"));
//            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            System.out.println("Response: " + s);
        } catch (Exception e) {
            // handle exception here
            Log.e(e.getClass().getName(), e.getMessage());
        }
    }
}
