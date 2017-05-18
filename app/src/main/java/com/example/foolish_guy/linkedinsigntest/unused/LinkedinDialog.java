package com.example.foolish_guy.linkedinsigntest.unused;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.foolish_guy.linkedinsigntest.AppLogs;
import com.example.foolish_guy.linkedinsigntest.Constants;
import com.example.foolish_guy.linkedinsigntest.R;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Foolish_Guy on 3/7/2017.
 */

public class LinkedinDialog extends Dialog
{
    private ProgressDialog progressDialog = null;

    public static LinkedInApiClientFactory factory;
    public static LinkedInOAuthService oAuthService;
    public static LinkedInRequestToken liToken;
    private WebView mWebView;
    private Context mContext;

    public LinkedinDialog(Context context, ProgressDialog progressDialog)
    {
        super(context);
        mContext = context;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// must call before super.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ln_dialog);
        setWebView();
    }

    /**
     * set webview.
     */
    private void setWebView()
    {
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        try {
            LinkedinDialog.oAuthService = LinkedInOAuthServiceFactory.getInstance()
                    .createLinkedInOAuthService(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
            LinkedinDialog.factory = LinkedInApiClientFactory.newInstance(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
            LinkedinDialog.liToken = LinkedinDialog.oAuthService.getOAuthRequestToken(Constants.OAUTH_CALLBACK_URL);

            mWebView.loadUrl(LinkedinDialog.liToken.getAuthorizationUrl());
            mWebView.setWebViewClient(new HelloWebViewClient());

            mWebView.setPictureListener(new WebView.PictureListener() {
                @Override
                public void onNewPicture(WebView view, Picture picture) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                }
            });
        }catch (ExceptionInInitializerError e){
            AppLogs.printLogs("ExceptionInInitializerError :: " , " err ::");
            LinkedinDialog.this.dismiss();
            mWebView.goBack();
        }catch (NoClassDefFoundError ex){
            AppLogs.printLogs("NoClassDefFoundError :: " , " err ::");
            ex.printStackTrace();
            LinkedinDialog.this.dismiss();
            mWebView.goBack();
        }catch (Exception ee){
            LinkedinDialog.this.dismiss();
            mWebView.goBack();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        cancel();
                    }

                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    class HelloWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if (url.contains(Constants.OAUTH_CALLBACK_URL))
            {
                Uri uri = Uri.parse(url);
                String verifier = uri.getQueryParameter("oauth_verifier");

                cancel();

                for (OnVerifyListener d : listeners)
                {
                    // call listener method
                    d.onVerify(verifier);
                }
            }
            else if (url.contains("https://www.google.co.in/"))
            {
                cancel();
            }
            else
            {
                Log.e("LinkedinSample", "url: " + url);
                view.loadUrl(url);
            }

            return true;
        }
    }

    /**
     * List of listener.
     */
    private List<OnVerifyListener> listeners = new ArrayList<OnVerifyListener>();

    /**
     * Register a callback to be invoked when authentication have finished.
     *
     * @param data
     *            The callback that will run
     */
    public void setVerifierListener(OnVerifyListener data)
    {
        listeners.add(data);
    }

    /**
     * Listener for oauth_verifier.
     */
    public interface OnVerifyListener
    {
        /**
         * invoked when authentication have finished.
         *
         * @param verifier
         *            oauth_verifier code.
         */
        public void onVerify(String verifier);
    }
}