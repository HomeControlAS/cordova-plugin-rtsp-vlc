package com.cordovaVLC;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 * <p>
 * Created at: 1/09/2018
 */

public class VideoPlayerVLC extends CordovaPlugin {
    private final String TAG = "VideoPlayerVLC";
    public final static String BROADCAST_METHODS = "com.cordovaVLC";

    private CallbackContext callbackContext;
    private CallbackContext callbackContextExternalData;
    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String method = intent.getStringExtra("method");
                String data = intent.getStringExtra("data");
                Log.d(TAG, "Method: " + method + " Data: " + data);

                if (method != null) {
                    if (method.equals("onPlayVlc")) {
                        _cordovaSendResult("onPlayVlc", data);
                    }
                    else if (method.equals("onPauseVlc")) {
                        _cordovaSendResult("onPauseVlc", data);
                    }
                    else if (method.equals("onStopVlc")) {
                        _cordovaSendResult("onStopVlc", data);
                    }
                    else if (method.equals("onVideoEnd")) {
                        _cordovaSendResult("onVideoEnd", data);
                    }
                    else if (method.equals("onDestroyVlc")) {
                        _cordovaSendResult("onDestroyVlc", data);
                    }
                    else if (method.equals("onError")) {
                        _cordovaSendResult("onError", data);
                    }
                    else if (method.equals("getPosition")) {
                        _cordovaSendResult("getPosition", data);
                    }
                    else if (method.equals("player_camera_move_request")) {
                        _cordovaSendExternal(data);
                    }
                    else if (method.equals("player_recording_request")) {
                        _cordovaSendExternal(data);
                    }
                   
                }
            }
        }
    };
    private Activity activity;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // application context
        //cordova input
        activity = cordova.getActivity();

        String url;
        JSONObject object;

        if (action.equals("play")) {
            this.callbackContext = callbackContext;
            url = args.getString(0);
            _play(url, true, true);
            return true;
        }
        else if (action.equals("pause")) {
            _filters("pause");
            return true;
        }
        else if (action.equals("stop")) {
            _filters("stop");
            return true;
        }
        else if (action.equals("close")) {
            _filters("close");
            return true;
        } else if (action.equals("receiveExternalData")) {


            String externalData = args.getString(0);
            if(externalData.equals("set_external_callback")) {
                    this.callbackContextExternalData = callbackContext;
                return true;
            }

            try {
                JSONObject jsonObject = new JSONObject(externalData);
                String type = jsonObject.getString("type");
                if(type.equals("webview_show_ptz_buttons")){
                    boolean showPtzArrowsRequest = jsonObject.getBoolean("value");
                    _filters("webview_show_ptz_buttons", showPtzArrowsRequest);
                }
                else if(type.equals("webview_show_recording_button")) {
                    boolean showRecordingButtonRequest = jsonObject.getBoolean("value");
                    _filters("webview_show_recording_button", showRecordingButtonRequest);
                }
                else if (type.equals("webview_update_rec_status")) {
                    boolean updateRecordingStatusRequest = jsonObject.getBoolean("value");
                    // tu primam status da je recording fkt poceo /true or false
                    _filters("webview_update_rec_status", updateRecordingStatusRequest);
                }
                return true;
            }catch (JSONException err){
                Log.d("Error", err.toString());
            }
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);

        return false;
    }

    @Override
    public void onResume(boolean p) {
        super.onPause(p);
    }

    @Override
    public void onPause(boolean p) {
        super.onPause(p);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.unregisterReceiver(br);

        _filters("stop");
    }

    private void _play(String uri, boolean autoPlay, boolean hideControls) {
        _broadcastRCV();

        Intent intent = new Intent(activity, VLCActivity.class);
        intent.putExtra("url", uri);
        intent.putExtra("autoPlay", autoPlay);
        intent.putExtra("hideControls", hideControls);
        cordova.startActivityForResult(this, intent, 1000);
    }

    private void _playNext(String uri, boolean autoPlay, boolean hideControls) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_METHODS);
        intent.putExtra("method", "playNext");

        intent.putExtra("url", uri);
        intent.putExtra("autoPlay", autoPlay);
        intent.putExtra("hideControls", hideControls);
        activity.sendBroadcast(intent);
    }

    private void _seekPosition(float position) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_METHODS);
        intent.putExtra("method", "seekPosition");
        intent.putExtra("position", position);
        activity.sendBroadcast(intent);
    }

    private void _filters(String methodName) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_METHODS);
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }

    private void _filters(String methodName, boolean data) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_METHODS);
        intent.putExtra("method", methodName);
        intent.putExtra("data", data);
        activity.sendBroadcast(intent);
    }

    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter(VLCActivity.BROADCAST_LISTENER);
        activity.registerReceiver(br, filter);
    }

    private void _cordovaSendResult(String event, String data) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, event);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void _cordovaSendExternal(String data) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
        pluginResult.setKeepCallback(true);
        if(callbackContextExternalData != null) {
            callbackContextExternalData.sendPluginResult(pluginResult);
        }
    }
}
