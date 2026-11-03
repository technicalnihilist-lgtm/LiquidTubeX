package com.liquidtubex.app;

import android.os.Handler;
import android.os.Looper;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.search.SearchInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * YouTube search through NewPipeExtractor, off the main thread, results posted
 * back on it.
 *
 * <p>{@code NewPipe.init(Downloader)} must run once at startup before any call
 * here — the library ships the {@link org.schabi.newpipe.extractor.downloader.Downloader}
 * interface, not an implementation, and every extractor call throws until one
 * is set.
 */
public class VideoExtractor {

    public static class Video {
        public String title;
        public String url;
        public String thumbnail;

        public Video(String title, String url, String thumbnail) {
            this.title = title;
            this.url = url;
            this.thumbnail = thumbnail;
        }
    }

    public interface Callback {
        void onSuccess(List<Video> videos);

        void onError(String error);
    }

    public static void search(String query, Callback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SearchInfo info = SearchInfo.getInfo(
                            ServiceList.YouTube,
                            ServiceList.YouTube
                                    .getSearchQHFactory()
                                    // contentFilters must be an empty list, not null:
                                    // the factory calls isEmpty() on it and throws otherwise.
                                    .fromQuery(query, java.util.Collections.emptyList(), "")
                    );

                    List<Video> videos = new ArrayList<>();

                    for (InfoItem item : info.getRelatedItems()) {

                        String thumbnail = "";

                        List<Image> images = item.getThumbnails();

                        if (images != null && !images.isEmpty()) {
                            thumbnail = images.get(0).getUrl();
                        }

                        videos.add(new Video(
                                item.getName(),
                                item.getUrl(),
                                thumbnail
                        ));
                    }

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(videos);
                        }
                    });

                } catch (Exception e) {

                    final String error = e.toString();

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(error);
                        }
                    });
                }
            }
        }).start();
    }
}
