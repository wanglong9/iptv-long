package org.tv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tv.common.SpiderProperties;

import java.net.HttpURLConnection;
import java.net.URL;

public class CheckM3U8Link {

    private static Logger logger = LoggerFactory.getLogger(CheckM3U8Link.class);

    public static boolean isM3U8LinkValid(String m3u8Url) {
        try {
            URL url = new URL(m3u8Url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(SpiderProperties.M3U8_TIMEOUT_MILLISECONDS);
            connection.connect();

            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            // 检查 HTTP 响应代码和内容类型
            if (responseCode == HttpURLConnection.HTTP_OK &&
                    (contentType.contains("application/vnd.apple.mpegurl")
                            || contentType.contains("audio/mpegurl")
                            || contentType.contains("application/octet-stream")
            )) {
                return true;
            }
        } catch (Exception e) {
            logger.warn("{} , connect timed out", m3u8Url);
        }
        return false;
    }
}