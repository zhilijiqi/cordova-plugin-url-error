
package org.apache.cordova.urlerror;

import android.app.Activity;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UrlError extends CordovaPlugin {
    private static final String LOG_TAG = "UrlError";
    private static String errorFile = null;
    private static String failingUrl;

    @Override
    protected void pluginInitialize() {
        super.initialize(cordova, webView);
        String errorPage = preferences.getString("UrlError", "errorPage.html");
        if(errorPage != null){
            errorFile = "file:///android_asset/www/"+errorPage;
        }
    }


    @Override
    public Object onMessage(String id, Object data) {
        if ("onReceivedError".equals(id)) {
            JSONObject d = (JSONObject)data;
            try {
                int code = d.getInt("errorCode");
                if(code ==-2){
                    this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
                    return data;
                }
            } catch (JSONException e) {
                LOG.e(LOG_TAG,e.getMessage());
            }
        }
        return null;
    }

    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
        if ((errorFile != null) && (!failingUrl.equals(errorFile)) && (webView != null)) {
            this.failingUrl = failingUrl;
            // Load URL on UI thread
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    webView.loadUrlIntoView(errorFile,true);
                }
            });
        }
    }

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        LOG.v(LOG_TAG, "Executing action: " + action);
        if ("errorUrl".equals(action)) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, failingUrl);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        return false;
    }
}