package com.liquidtubex.app;

import android.os.Handler;
import android.os.Looper;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.services.ServiceList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs a YouTube search through NewPipeExtractor off the main thread and hands
 * the results back on it.
 *
 * <p>Before the first call, {@code NewPipe.init(Downloader)} must have run once
 * with a {@link org.schabi.newpipe.extractor.downloader.Downloader}
 * implementation — the library ships the interface, not an implementation, and
 * every extractor call throws until it is set. That belongs in the app's
 * startup path (see MainActivity), not here.
 */
public class VideoExtractor {

    public interface Callback {
        void onSuccess(List<String> videos);

        void onError(String error);
    }

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public static void search(String query, Callback callback) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SearchInfo info = SearchInfo.getInfo(
                            ServiceList.YouTube,
                            ServiceList.YouTube
                                    .getSearchQHFactory()
                                    .fromQuery(query, null, null)
                    );

                    List<String> results = new ArrayList<>();

                    for (InfoItem item : info.getRelatedItems()) {
                        results.add(
                                item.getName() + "\n" + item.getUrl()
                        );
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(results);
                        }
                    });

                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        });
    }
}
