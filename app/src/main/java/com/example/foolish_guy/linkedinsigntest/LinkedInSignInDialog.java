package com.example.foolish_guy.linkedinsigntest;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInApiConsumer;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static com.example.foolish_guy.linkedinsigntest.Constants.TWITTER_CALLBACK_URL;
import static com.example.foolish_guy.linkedinsigntest.Constants.URL_TWITTER_OAUTH_VERIFIER;

/**
 * Created by Foolish_Guy on 3/8/2017.
 */

public class LinkedInSignInDialog extends Dialog{
    public static LinkedInApiClientFactory clientFactory;
    public static LinkedInOAuthService oAuthService;
    public static LinkedInRequestToken requestToken;
    private LinkedInApiConsumer apiConsumer;


    public static Twitter twitter;
    public static RequestToken twitterRequestToken;
    public static TwitterFactory factory;

    private WebView webView;
    private Context mContext;
    private String mNewtork;
    public LinkedInSignInDialog (Context context, String network) {
        super(context);
        mContext = context;
        mNewtork = network;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ln_dialog);

        initWebView ();
    }

    private void initWebView () {
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        if (mNewtork.equalsIgnoreCase(NETWORK.Linkedin.toString())) {

            try {
                apiConsumer = new LinkedInApiConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

                oAuthService = LinkedInOAuthServiceFactory.getInstance()
                        .createLinkedInOAuthService(apiConsumer);
                clientFactory = LinkedInApiClientFactory.newInstance(apiConsumer);
                requestToken = oAuthService.getOAuthRequestToken(Constants.OAUTH_CALLBACK_URL);

                webView.loadUrl(requestToken.getAuthorizationUrl());
                webView.setWebViewClient(new MyWebViewClient());

            } catch (ExceptionInInitializerError e) {
                AppLogs.printLogs("ExceptionInInitializerError :: ", " err ::");
                this.dismiss();
                webView.goBack();
            } catch (NoClassDefFoundError ex) {
                AppLogs.printLogs("NoClassDefFoundError :: ", " err ::");
                ex.printStackTrace();
                this.dismiss();
                webView.goBack();
            } catch (Exception ee) {
                this.dismiss();
                webView.goBack();
            }
        } else if (mNewtork.equalsIgnoreCase(NETWORK.Twitter.toString())) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.TWITTER_KEY);
            builder.setOAuthConsumerSecret(Constants.TWITTER_SECRET);

            Configuration configuration = builder.build();

            factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                twitterRequestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                webView.loadUrl(twitterRequestToken.getAuthorizationURL());

                webView.setWebViewClient(new MyWebViewClient());

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        cancel();
                    }

                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


    private class MyWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(url, view);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUrl(request.getUrl().toString(), view);
        }

        private Boolean handleUrl (final  String url, WebView view) {

            Uri uri = Uri.parse(url);

            Log.e ("URL :", url);
            if (url.contains(Constants.OAUTH_CALLBACK_URL) || url.contains(Constants.OAUTH_CALLBACK_HOST))
            {
                String verifier = uri.getQueryParameter("oauth_verifier");

                cancel();

                for (OnVerifyListener d : listeners)
                {
                    d.onVerify(verifier, NETWORK.Linkedin.toString());
                }
            }
            else if (url != null && url.contains(TWITTER_CALLBACK_URL) ) {
                String verifier = uri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                cancel();

                for (OnVerifyListener d : listeners)
                {
                    d.onVerify(verifier, NETWORK.Twitter.toString());
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
    private List<OnVerifyListener> listeners = new ArrayList<OnVerifyListener>();

    public void setOnverifyListener (OnVerifyListener data) {
        listeners.add(data);
    }

    public interface OnVerifyListener {
        public void onVerify(String verifier, String netWork);
    }
}
