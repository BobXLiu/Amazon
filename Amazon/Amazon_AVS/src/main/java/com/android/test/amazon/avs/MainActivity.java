package com.android.test.amazon.avs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.shared.APIListener;
import com.willblaschko.android.alexa.AlexaManager;
import com.willblaschko.android.alexa.audioplayer.AlexaAudioPlayer;
import com.willblaschko.android.alexa.callbacks.AsyncCallback;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayContentItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayRemoteItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceAllItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceEnqueuedItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsStopItem;
import com.willblaschko.android.alexa.interfaces.speechrecognizer.AvsExpectSpeechItem;
import com.willblaschko.android.alexa.interfaces.speechsynthesizer.AvsSpeakItem;
import com.willblaschko.android.alexa.requestbody.DataRequestBody;
import com.willblaschko.android.recorderview.RecorderView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ee.ioc.phon.android.speechutils.RawAudioRecorder;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {
    private TextView tvInformation;
    private AmazonAuthorizationManager mAuthManager;
    private String[] APP_SCOPES = {"alexa:all"};
    private final static String PRODUCT_ID = "android_avs";//INSERT YOUR PRODUCT ID FROM Amazon developer
    // portal
    private final static String PRODUCT_DSN = Build.SERIAL; //INSERT UNIQUE DSN FOR YOUR DEVICE;
    private String accessToken = "";

    private boolean isRecord = false;

    private boolean isDebug = false;
    private boolean isHasPermission = false;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET};

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = 0;
            for (; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
                    break;
                }
            }
            if (i >= permissions.length) {
                isHasPermission = true;
                init();
                initAlexaAndroid();
            }
        } else {
            isHasPermission = true;
            init();
            initAlexaAndroid();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            this,
                            "You should agree all of the permissions, force exit! please retry",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            isHasPermission = true;
            init();
            initAlexaAndroid();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            isHasPermission = true;
            init();
            initAlexaAndroid();
        }
    }

    private boolean isSendLive = false;

    private void init() {
        tvInformation = (TextView) findViewById(R.id.information);
        tvInformation.setMovementMethod(ScrollingMovementMethod.getInstance());

        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            output(e.toString());
        }

        Button btnSendLive = (Button) findViewById(R.id.send_live);

        btnSendLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                isSendLive = !isSendLive;
//                if(isSendLive){
//                startListening();
                startGSVoiceRecorder();
//                }else {
//                    stopListening();
//                }
            }
        });

//        if(isDebug){
//            btnSendLive.setVisibility(View.GONE);
//        }
    }

    private void output(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvInformation.append("\n" + content);
            }
        });
    }

    public void btnLogin(View view) {
//        Bundle options = new Bundle();
//        String scope_data = "{\"alexa:all\":{\"productID\":\"" + PRODUCT_ID +
//                "\", \"productInstanceAttributes\":{\"deviceSerialNumber\":\"" +
//                PRODUCT_DSN + "\"}}}";
//        options.putString(AuthzConstants.BUNDLE_KEY.SCOPE_DATA.val, scope_data);
//
//        mAuthManager.authorize(APP_SCOPES, options, new AuthorizeListener());


        new Thread(new Runnable() {
            @Override
            public void run() {
                AccessToken accessToken = new AccessToken(MainActivity.this,PRODUCT_ID);
                accessToken.authorizeUser();
            }
        }).start();
    }

    public void btnRefreshToken(View view){
        RefreshToken refreshToken = new RefreshToken(MainActivity.this);
        refreshToken.getRefreshToken(mAuthManager,Common.common_refresh_token);
    }

    public void btnConnect(View view) {
        MakeConnectionWithAmazon makeConnectionWithAmazon = new MakeConnectionWithAmazon();
        makeConnectionWithAmazon.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void btnPing(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url("https://avs-alexa-na.amazon" +
                        ".com/ping")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .get()
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("Bob", "Ping exception: " + e.getMessage());
                    output("Ping Connect Exception: " + e.getMessage());
                }
                if (response != null) {
                    try {
                        int codeStatus = response.code();
                        Log.i("Bob", "Ping response code: " + codeStatus);
                        output("Ping response code: " + codeStatus);
                        Headers responseHeaders = response.headers();
                        for (int i = 0; i < responseHeaders.size(); i++) {
                            Log.i("Bob", "Ping response headers: " + responseHeaders.name(i) + ": " +
                                    responseHeaders.value(i));
                            output("Ping response headers: " + responseHeaders.name(i) + ": " +
                                    responseHeaders
                                            .value(i));
                        }

                        String tempResponse = response.body().string();
                        Log.i("Bob", "Ping response body message: " + tempResponse);
                        output("Ping response body message: " + tempResponse);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("Bob", e.getMessage());
                    }
                    return;
                } else {
                    Log.i("Bob", "response is null");
                    output("response is null");
                    return;
                }
            }
        }).start();
    }


    public class AuthorizeListener implements AuthorizationListener {

        @Override
        public void onCancel(Bundle bundle) {
            output("Author Cancel");
        }

        @Override
        public void onSuccess(Bundle bundle) {
            output("Author Success");
            mAuthManager.getToken(new String[]{"alexa:all"}, new TokenListener());
        }

        @Override
        public void onError(AuthError authError) {
            output("Author Error: " + authError.getMessage());
        }
    }

    public class TokenListener implements APIListener {

        @Override
        public void onSuccess(Bundle bundle) {
            output("Token Success");

            if (bundle == null) {
                output("bundle is null");
            }

//            accessToken = bundle.getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            if (!TextUtils.isEmpty(accessToken)) {
                output("Access Token: " + accessToken);
                Log.i("Bob", "Access Token: " + accessToken);
            } else {
                output("access Token is null");
            }
        }

        @Override
        public void onError(AuthError authError) {
            output("Token Error: " + authError.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isHasPermission) {
            if (!isRecord) {
//                mAuthManager.getToken(new String[]{"alexa:all"}, new TokenListener());
            } else {
                isRecord = false;
            }
        }
    }

    @SuppressLint("NewApi")
    private class MakeConnectionWithAmazon extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences preferences = getSharedPreferences(Common.TOKEN_PREFERENCE_KEY, Context.MODE_PRIVATE);
            accessToken = preferences.getString(Common.PREF_ACCESS_TOKEN,Common.common_access_token);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            useOkHttpConnect();
            return null;
        }

        private void useOkHttpConnect() {
            Request request = new Request.Builder().url("https://avs-alexa-na.amazon" +
                    ".com/v20160207/directives")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();
            Log.i("Bob","access Token: " + accessToken);

            OkHttpClient client = new OkHttpClient();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Bob", "exception: " + e.getMessage());
                output("Connect Exception: " + e.getMessage());
            }
            if (response != null) {
                try {
                    int codeStatus = response.code();
                    Log.i("Bob", "response code: " + codeStatus);
                    output("response code: " + codeStatus);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.i("Bob", "response headers: " + responseHeaders.name(i) + ": " +
                                responseHeaders.value(i));
                        output("response headers: " + responseHeaders.name(i) + ": " + responseHeaders
                                .value(i));
                    }

                    if (codeStatus == 200) {
                        //SynchronizeState event
                        synchronizeStateEvent(client);
                    }

                    String tempResponse = response.body().string();
                    Log.i("Bob", "response body message: " + tempResponse);
                    output("response body message: " + tempResponse);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("Bob", e.getMessage());
                }
                return;
            } else {
                Log.i("Bob", "response is null");
                output("response is null");
                return;
            }
        }

        private void synchronizeStateEvent(OkHttpClient client) {
            MediaType media_type = MediaType.parse("application/json; charset=UTF-8");

            MultipartBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of(
                            "Content-Disposition",
                            "form-data; name=\"metadata\""),
                            RequestBody.create(media_type, ""))
                    .build();

            Request request = new Request.Builder()
                    .url("https://avs-alexa-na.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(body)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Bob", "exception: " + e.getMessage());
                output("Connect Exception: " + e.getMessage());
            }

            if (response != null) {
                try {
                    int codeStatus = response.code();
                    Log.i("Bob", "response code: " + codeStatus);
                    output("response code: " + codeStatus);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.i("Bob", "response headers: " + responseHeaders.name(i) + ": " +
                                responseHeaders.value(i));
                        output("response headers: " + responseHeaders.name(i) + ": " + responseHeaders
                                .value(i));
                    }
                    String tempResponse = response.body().string();
                    Log.i("Bob", "response body message: " + tempResponse);
                    output("response body message: " + tempResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("Bob", e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.record) {
            Intent intent = new Intent(this, RecordActivity.class);
            startActivity(intent);
            isRecord = true;
        }
        return false;
    }

    File president_10_file = new File("/mnt/sdcard/RecordFile/president_10/president.raw");
    File president_60_file = new File("/mnt/sdcard/RecordFile/president_60/RawAudio.raw");

    File weather_10_file = new File("/mnt/sdcard/RecordFile/weather_10/tokyo.raw");
    File weather_60_file = new File("/mnt/sdcard/RecordFile/weather_60/RawAudio.raw");

    public void btnSend(View view) {
//        sendPrerecordedAudio();
//        sendTextRequest();

//        sendPrerecordedAudio(president_10_file);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SendLive sendLive = new SendLive(MainActivity.this);
                sendLive.setSendLiveListener(new SendLive.SendLiveListener() {
                    @Override
                    public void failure() {
                        stopListening();
                    }
                });
                try {
                    sendLive.sendAudio(new DataRequestBody() {
                        @Override
                        public void writeTo(BufferedSink sink) throws IOException {
                            try {
//                                InputStream inputStream = new BufferedInputStream(new FileInputStream(president_10_file));
//                                InputStream inputStream = new BufferedInputStream(new FileInputStream(president_60_file));
                                InputStream inputStream = new BufferedInputStream(new FileInputStream(weather_10_file));
//                                InputStream inputStream = new BufferedInputStream(new FileInputStream(weather_60_file));
                                byte[] fileBytes = new byte[inputStream.available()];
                                inputStream.read(fileBytes);
                                inputStream.close();
                                sink.write(fileBytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("Bob","send live exception: " + e.toString());
                }
            }
        }).start();
    }

    public void btnSendText(View view){
        alexaManager.sendTextRequest("what is the whether like today", requestCallback);

//        android.speech.tts.TextToSpeech;
    }

    private AlexaManager alexaManager;
    private AlexaAudioPlayer audioPlayer;
    private List<AvsItem> avsQueue = new ArrayList<>();

    private void initAlexaAndroid() {
        //get our AlexaManager instance for convenience
        alexaManager = AlexaManager.getInstance(this, PRODUCT_ID);

        //instantiate our audio player
        audioPlayer = AlexaAudioPlayer.getInstance(this);

        //Callback to be able to remove the current item and check queue once we've finished playing an item
        audioPlayer.addCallback(alexaAudioPlayerCallback);

    }

    //Our callback that deals with removing played items in our media player and then checking to see if
    // more items exist
    private AlexaAudioPlayer.Callback alexaAudioPlayerCallback = new AlexaAudioPlayer.Callback() {
        @Override
        public void playerPrepared(AvsItem pendingItem) {

        }

        @Override
        public void playerProgress(AvsItem currentItem, long offsetInMilliseconds, float percent) {

        }

        @Override
        public void itemComplete(AvsItem completedItem) {
            avsQueue.remove(completedItem);
            checkQueue();
            Log.i("Bob", "item complete");
            stopListening();
        }

        @Override
        public boolean playerError(AvsItem item, int what, int extra) {
            return false;
        }

        @Override
        public void dataError(AvsItem item, Exception e) {

        }
    };

    //async callback for commands sent to Alexa Voice
    private AsyncCallback<AvsResponse, Exception> requestCallback = new AsyncCallback<AvsResponse,
            Exception>() {
        @Override
        public void start() {
            //your on start code
            Log.i("Bob", "request call back start");
        }

        @Override
        public void success(AvsResponse result) {
            Log.i("Bob", "Voice Success");
            handleResponse(result);
        }

        @Override
        public void failure(Exception error) {
            //your on error code
            Log.i("Bob", "request call back failure");

            stopListening();
        }

        @Override
        public void complete() {
            //your on complete code
            Log.i("Bob", "request call back complete");
        }
    };

    /**
     * Handle the response sent back from Alexa's parsing of the Intent, these can be any of the AvsItem
     * types (play, speak, stop, clear, listen)
     *
     * @param response a List<AvsItem> returned from the mAlexaManager.sendTextRequest() call in
     *                 sendVoiceToAlexa()
     */
    private void handleResponse(AvsResponse response) {

        if (response != null) {
            Log.i("Bob", "avsresponse is not null;");
            //if we have a clear queue item in the list, we need to clear the current queue before proceeding
            //iterate backwards to avoid changing our array positions and getting all the nasty errors that
            // come
            //from doing that
            for (int i = response.size() - 1; i >= 0; i--) {
                if (response.get(i) instanceof AvsReplaceAllItem || response.get(i) instanceof
                        AvsReplaceEnqueuedItem) {
                    //clear our queue
                    avsQueue.clear();
                    //remove item
                    response.remove(i);
                }
            }
            avsQueue.addAll(response);

            AvsItem item = response.get(0);
            AvsSpeakItem avsSpeakItem = (AvsSpeakItem) item;
            saveAudioFile(avsSpeakItem, "88888");

            if (!audioPlayer.isPlaying()) {
                audioPlayer.stop();
            }
            audioPlayer.playItem(avsSpeakItem);
        } else {
            Log.i("Bob", "avsresponse is null;");
        }
//        checkQueue();
    }

    /**
     * Check our current queue of items, and if we have more to parse (once we've reached a play or listen
     * callback) then proceed to the
     * next item in our list.
     * <p>
     * We're handling the AvsReplaceAllItem in handleResponse() because it needs to clear everything
     * currently in the queue, before
     * the new items are added to the list, it should have no function here.
     */
    private void checkQueue() {
        //if we're out of things, hang up the phone and move on
        if (avsQueue.size() == 0) {
            return;
        }

        AvsItem current = avsQueue.get(0);
        if (current instanceof AvsPlayRemoteItem) {
            Log.i("Bob", "AvsPlayRemoteItem");
            //play a URL
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playItem((AvsPlayRemoteItem) current);
            }
        } else if (current instanceof AvsPlayContentItem) {
            Log.i("Bob", "AvsPlayContentItem");
            //play a URL
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playItem((AvsPlayContentItem) current);
            }
        } else if (current instanceof AvsSpeakItem) {
            Log.i("Bob", "AvsSpeakItem");
            //play a sound file
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playItem((AvsSpeakItem) current);
            }

            saveAudioFile((AvsSpeakItem) current, "12345");
        } else if (current instanceof AvsStopItem) {
            Log.i("Bob", "AvsStopItem");
            //stop our play
            audioPlayer.stop();
            avsQueue.remove(current);
        } else if (current instanceof AvsReplaceAllItem) {
            Log.i("Bob", "AvsReplaceAllItem");
            audioPlayer.stop();
            avsQueue.remove(current);
        } else if (current instanceof AvsReplaceEnqueuedItem) {
            Log.i("Bob", "AvsReplaceEnqueuedItem");
            avsQueue.remove(current);
        } else if (current instanceof AvsExpectSpeechItem) {
            Log.i("Bob", "AvsExpectSpeechItem");
            //listen for user input
            audioPlayer.stop();
//            startListening();
        }
    }


    private void sendPrerecordedAudio() {
        //send prerecorded audio to Alexa, parse the callback in requestCallback
        try {
            AudioFileFunc audioFileFunc = new AudioFileFunc(MainActivity.this);
            File file = new File(audioFileFunc.getRawFilePath());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            //open asset file
//            InputStream is = getActivity().getAssets().open("intros/joke.raw");
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();
            alexaManager.sendAudioRequest(fileBytes, requestCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPrerecordedAudio(File file) {
        //send prerecorded audio to Alexa, parse the callback in requestCallback
        try {
//            AudioFileFunc audioFileFunc = new AudioFileFunc(MainActivity.this);
//            File file = new File(audioFileFunc.getRawFilePath());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            //open asset file
//            InputStream is = getActivity().getAssets().open("intros/joke.raw");
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();
            alexaManager.sendAudioRequest(fileBytes, requestCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAudioFile(AvsSpeakItem item, String name) {
        File path = new File(this.getCacheDir(), name + ".mp3");
        Log.i("Bob", "save audio file");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(item.getAudio());
            fos.close();
            //play our newly-written file
//            getMediaPlayer().setDataSource(path.getPath());
        } catch (IOException e) {
            Log.i("Bob", "save audio file exception: " + e.toString());
            e.printStackTrace();
            //bubble up our error
//            bubbleUpError(e);
        }
    }

    private static final int AUDIO_RATE = 16000;
    private RawAudioRecorder recorder;
    private RecorderView recorderView;

    public void startListening() {
        if (recorder == null) {
            recorder = new RawAudioRecorder(AUDIO_RATE);
        }
        recorder.start();
        Log.i("Bob", "start record");
//        alexaManager.sendAudioRequest(requestBody, requestCallback);

       new Thread(new Runnable() {
           @Override
           public void run() {
               SendLive sendLive = new SendLive(MainActivity.this);
               sendLive.setSendLiveListener(new SendLive.SendLiveListener() {
                   @Override
                   public void failure() {
                       stopListening();
                   }
               });
               try {
                   sendLive.sendAudio(requestBody);
               } catch (IOException e) {
                   e.printStackTrace();
                   Log.i("Bob","send live exception: " + e.toString());
               }
           }
       }).start();
    }

    //our streaming data requestBody
    private DataRequestBody requestBody = new DataRequestBody() {
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            //while our recorder is not null and it is still recording, keep writing to POST data
//            while (gsVoiceRecorder != null && gsVoiceRecorder.getRecorderStart()) {
            while (recorder != null && !recorder.isPausing()) {
                    final float rmsdb = recorder.getRmsdb();
                    if (sink != null && recorder != null) {
                        sink.write(recorder.consumeRecording());
                    }

                //sleep and do it all over again
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Bob", "prepare stop listen");
            stopListening();
        }
    };

    //tear down our recorder
    private void stopListening() {
        if (recorder != null) {
            Log.i("Bob", "stop record");
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }


    //for test GSVoiceRecorder
    private GSVoiceRecorder gsVoiceRecorder = null;
    private ByteArrayOutputStream byteArrayOutputStream = null;
    private void startGSVoiceRecorder(){
        if(gsVoiceRecorder == null){
            gsVoiceRecorder = new GSVoiceRecorder(callback);
        }
        gsVoiceRecorder.start();
    }

    GSVoiceRecorder.Callback  callback = new GSVoiceRecorder.Callback() {
        @Override
        public void onVoiceStart() {
            super.onVoiceStart();
            Log.i("Bob","Reset ByteArrayOutputStream");
            byteArrayOutputStream = new ByteArrayOutputStream();
        }

        @Override
        public void onVoice(byte[] data, int size) {
            super.onVoice(data, size);
            try {
                byteArrayOutputStream.write(data);
                Log.i("Bob","byteArrayOutputStream size: " + byteArrayOutputStream.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onVoiceEnd() {
            super.onVoiceEnd();
            gsVoiceRecorder.stop();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SendLive sendLive = new SendLive(MainActivity.this);
                    sendLive.setSendLiveListener(new SendLive.SendLiveListener() {
                        @Override
                        public void failure() {
                        }
                    });
                    try {
                        sendLive.sendAudio(new DataRequestBody() {
                            @Override
                            public void writeTo(BufferedSink sink) throws IOException {
                                try {
                                    sink.write(byteArrayOutputStream.toByteArray());
                                    byteArrayOutputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("Bob","send live exception: " + e.toString());
                    }
                }
            }).start();
        }
    };
}
