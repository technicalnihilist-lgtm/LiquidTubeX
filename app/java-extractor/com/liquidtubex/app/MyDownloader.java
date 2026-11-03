package com.liquidtubex.app;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * The {@link Downloader} NewPipeExtractor needs, on top of HttpURLConnection.
 *
 * <p>Install it once at startup with {@code NewPipe.init(new MyDownloader())},
 * before any extractor call.
 */
public class MyDownloader extends Downloader {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36";

    @Override
    public Response execute(Request request) throws IOException {

        HttpURLConnection connection =
                (HttpURLConnection) new URL(request.url()).openConnection();

        connection.setRequestMethod(request.httpMethod());
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        Map<String, List<String>> headers = request.headers();

        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    for (String value : entry.getValue()) {
                        connection.setRequestProperty(entry.getKey(), value);
                    }
                }
            }
        }

        byte[] data = request.dataToSend();

        if (data != null) {
            connection.setDoOutput(true);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(data);
            }
        }

        int status = connection.getResponseCode();

        InputStream input = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        if (input != null) {
            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            input.close();
        }

        Response response = new Response(
                status,
                connection.getResponseMessage(),
                connection.getHeaderFields(),
                output.toString("UTF-8"),
                connection.getURL().toString()
        );

        connection.disconnect();

        return response;
    }
}
