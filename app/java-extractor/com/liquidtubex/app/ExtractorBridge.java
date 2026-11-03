package com.liquidtubex.app;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.NewPipe;

/**
 * The JavaScript bridge between the HTML shell and NewPipeExtractor.
 *
 * <p>Registered from MainActivity as {@code LiquidTubeX}. It lives in the
 * {@code java-extractor} source set, so only the Gradle build carries it; the
 * build.sh APK simply has no bridge attached.
 *
 * <p>Both callbacks hand the page a JSON <em>string</em>:
 * {@code window.onVideosReceived(json)} and {@code window.onVideoError(message)}.
 * The page is expected to {@code JSON.parse} the first one.
 */
public class ExtractorBridge {

    private static final String TAG = "LiquidTube";

    private static boolean initialised;

    private final WebView web;

    public ExtractorBridge(WebView web) {
        this.web = web;
    }

    /** Must run once, before any extractor call. */
    public static synchronized void init() {
        if (initialised) {
            return;
        }
        NewPipe.init(new MyDownloader());
        initialised = true;
        Log.i(TAG, "extractor: NewPipe initialised");
    }

    @JavascriptInterface
    public void searchVideos(String query) {
        Log.i(TAG, "extractor: searchVideos called with \"" + query + "\"");
        if (query == null || query.trim().isEmpty()) {
            send("window.onVideoError", "Empty query");
            return;
        }

        VideoExtractor.search(query.trim(), new VideoExtractor.Callback() {

            @Override
            public void onSuccess(java.util.List<VideoExtractor.Video> videos) {
                try {
                    JSONArray array = new JSONArray();
                    for (VideoExtractor.Video video : videos) {
                        JSONObject object = new JSONObject();
                        object.put("title", video.title == null ? "" : video.title);
                        object.put("url", video.url == null ? "" : video.url);
                        object.put("thumbnail", video.thumbnail == null ? "" : video.thumbnail);
                        array.put(object);
                    }
                    Log.i(TAG, "extractor: " + array.length() + " results");
                    send("window.onVideosReceived", array.toString());
                } catch (Exception e) {
                    send("window.onVideoError", e.toString());
                }
            }

            @Override
            public void onError(String error) {
                Log.i(TAG, "extractor: search failed - " + error);
                send("window.onVideoError", error == null ? "Unknown error" : error);
            }
        });
    }

    private void send(final String function, final String payload) {
        web.post(new Runnable() {
            @Override
            public void run() {
                web.evaluateJavascript(
                        function + "(" + JSONObject.quote(payload) + ");", null);
            }
        });
    }
}
