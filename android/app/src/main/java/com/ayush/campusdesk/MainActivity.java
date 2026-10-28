package com.ayush.campusdesk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String START_URL = "https://campusdesk-student-management.vercel.app";
    private static final Set<String> ALLOWED_HOSTS = new HashSet<>(Arrays.asList(
            "campusdesk-student-management.vercel.app",
            "campusdesk-student-management.onrender.com"
    ));

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setSupportZoom(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                String host = uri.getHost();

                if ("https".equalsIgnoreCase(scheme) && host != null && ALLOWED_HOSTS.contains(host)) {
                    return false;
                }

                if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception ignored) {
                        // Keep unsupported external links from crashing the app.
                    }
                }
                return true;
            }
        });

        setContentView(webView);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            if (webView.getUrl() == null) {
                webView.loadUrl(START_URL);
            }
        } else {
            webView.loadUrl(START_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
