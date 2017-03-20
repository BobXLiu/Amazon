package com.android.test.amazon.avs;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import com.willblaschko.android.alexa.connection.ClientUtil;
import com.willblaschko.android.alexa.data.Event;
import com.willblaschko.android.alexa.interfaces.AvsException;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceAllItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceEnqueuedItem;
import com.willblaschko.android.alexa.interfaces.response.ResponseParser;
import com.willblaschko.android.alexa.interfaces.speechsynthesizer.AvsSpeakItem;
import com.willblaschko.android.alexa.requestbody.DataRequestBody;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Time：16-11-23 09:29
 * Author：bob
 */
public class SendLive {
    private Context mContext;
    DataRequestBody requestBody;
    //OkHttpClient for transfer of data
    Request.Builder mRequestBuilder = new Request.Builder();
    MultipartBody.Builder mBodyBuilder;
    protected ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();


    public SendLive(Context context) {
        mContext = context;
        Log.i("Bob","init SendLive");
    }

    private String getEventsUrl() {
        return new StringBuilder()
                .append(mContext.getString(com.willblaschko.android.alexa.R.string.alexa_api))
                .append("/")
                .append(mContext.getString(com.willblaschko.android.alexa.R.string.alexa_api_version))
                .append("/")
                .append("events")
                .toString();
    }

    private String getAccessToken() {
        String token = Common.common_access_token;
        SharedPreferences preferences = mContext.getSharedPreferences(Common.TOKEN_PREFERENCE_KEY, Context
                .MODE_PRIVATE);
        token = preferences.getString(Common.PREF_ACCESS_TOKEN, token);
        return token;
    }

    public void sendAudio( @NotNull DataRequestBody requestBody)
            throws IOException {
        this.requestBody = requestBody;
        Log.i("Bob", "Starting SpeechSendAudio procedure");
        long start = System.currentTimeMillis();

        //call the parent class's prepareConnection() in order to prepare our URL POST
        try {
//            prepareConnection(url, accessToken);
            prepareConnection(getEventsUrl(), getAccessToken());
            final AvsResponse response = completePost();

            if (response != null && response.isEmpty()) {
                Log.i("Bob", "Nothing come back");
//                if (callback != null) {
//                    callback.failure(new AvsAudioException("Nothing came back"));
//                }
                return;
            }

//            if (callback != null) {
            if (response != null) {
                Log.i("Bob", "send request success: " + response.toString());
//                    callback.success(response);
//                }
//                callback.complete();
                handleResponse(response);
            }

            Log.i("Bob", "Audio sending process took: " + (System.currentTimeMillis() - start));
        } catch (IOException | AvsException e) {
            e.printStackTrace();
            Log.i("Bob", "send exception: " + e.toString());
            if(liveListener != null){
                liveListener.failure();
            }
        }
    }

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
//                    avsQueue.clear();
                    //remove item
                    response.remove(i);
                }
            }
//            avsQueue.addAll(response);

            AvsItem item = response.get(0);
            AvsSpeakItem avsSpeakItem = (AvsSpeakItem) item;
            saveAudioFile(avsSpeakItem, "avs");

//            if (!audioPlayer.isPlaying()) {
//                audioPlayer.stop();
//            }
//            audioPlayer.playItem(avsSpeakItem);
        } else {
            Log.i("Bob", "avsresponse is null;");
        }
//        checkQueue();
    }

    private void saveAudioFile(AvsSpeakItem item, String name) {
//        File path = new File(context.getCacheDir(), name + ".mp3");
        File path = new File(mContext.getExternalFilesDir(null), name + ".mp3");
        if(path.exists()){
            path.delete();
        }
        Log.i("Bob", "save audio file");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(item.getAudio());
            fos.close();
            //play our newly-written file
//            getMediaPlayer().setDataSource(path.getPath());
            //save success and play it
            playAudioFile(path);
        } catch (IOException e) {
            Log.i("Bob", "save audio file exception: " + e.toString());
            e.printStackTrace();
            //bubble up our error
//            bubbleUpError(e);
        }
    }

    private void playAudioFile(File audio){
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i("Bob","play finish");
                mediaPlayer.release();
                mediaPlayer = null;
//                stopListening();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.i("Bob","play error");
                mediaPlayer.release();
                mediaPlayer = null;
//                stopListening();
                return false;
            }
        });

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(audio.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Bob","mediaplayer set data source exception: " + e.toString());
        }
    }


    private void prepareConnection(String url, String accessToken) {

        //set the request URL
        mRequestBuilder.url(url);

        //set our authentication access token header
        mRequestBuilder.addHeader("Authorization", "Bearer " + accessToken);

        mBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse
                        ("application/json; charset=UTF-8"), getEvent()));

//        Log.i("Bob","getEvent(): " + getEvent());

        //reset our output stream
        mOutputStream = new ByteArrayOutputStream();
    }

    private String getEvent() {
        return getSpeechRecognizerEvent();
    }

    public static String getSpeechRecognizerEvent() {
        Event.Builder builder = new Event.Builder();
        builder.setHeaderNamespace("SpeechRecognizer")
                .setHeaderName("Recognize")
                .setHeaderMessageId(getUuid())
                .setHeaderDialogRequestId("dialogRequest-321")
                .setPayloadFormat("AUDIO_L16_RATE_16000_CHANNELS_1")
//                .setPayloadProfile("CLOSE_TALK");
                .setPayloadProfile("FAR_FIELD");
        return builder.toJson();
    }

    private static String getUuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    private AvsResponse completePost() throws IOException, AvsException, RuntimeException {
        addFormDataParts(mBodyBuilder);
        mRequestBuilder.post(mBodyBuilder.build());
        return parseResponse();
    }

    private void addFormDataParts(MultipartBody.Builder builder){
        builder.addFormDataPart("audio", "speech.wav", getRequestBody());
    }

    private RequestBody getRequestBody() {
        return requestBody;
    }

    private AvsResponse parseResponse() throws IOException, AvsException, RuntimeException {
        Request request = mRequestBuilder.build();

        Log.i("Bob","reset okHttpClient start");
        OkHttpClient okHttpClient = ClientUtil.getTLS12OkHttpClient();
       OkHttpClient.Builder builder= okHttpClient.newBuilder();
       builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5,TimeUnit.SECONDS);
        OkHttpClient client = builder.build();

        Log.i("Bob","reset okHttpClient stop");

//        Call currentCall = okHttpClient.newCall(request);
        Call currentCall = client.newCall(request);

        Log.i("Bob","prepare execute");
        try {
            Response response = currentCall.execute();
            Log.i("Bob","execute finish");

            int code = response.code();
            Log.i("Bob","response code: " + code );

            final AvsResponse val = code == HttpURLConnection.HTTP_NO_CONTENT ? new AvsResponse() :
                    ResponseParser.parseResponse(response.body().byteStream(), getBoundary(response));

            response.body().close();
            return val;
        } catch (IOException exp) {
            Log.i("Bob","current call IOException: " + exp.toString());
            if(liveListener != null){
                liveListener.failure();
            }
            if (!currentCall.isCanceled()) {
                return new AvsResponse();
            }
        }
        return null;
    }

    protected String getBoundary(Response response) throws IOException {
        Headers headers = response.headers();
        String header = headers.get("content-type");
        String boundary = "";

        if (header != null) {
            Pattern pattern = Pattern.compile("boundary=(.*?);");
            Matcher matcher = pattern.matcher(header);
            if (matcher.find()) {
                boundary = matcher.group(1);
            }
        } else {
            Log.i("Bob", "Body: " + response.body().string());
        }
        return boundary;
    }


    public interface SendLiveListener{
        void failure();
    }
    private SendLiveListener liveListener = null;
    public void setSendLiveListener(SendLiveListener liveListener){
        this.liveListener = liveListener;
    }
}
