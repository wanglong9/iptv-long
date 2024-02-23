package org.tv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tv.common.SpiderProperties;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CheckM3U8Link {

    private static Logger logger = LoggerFactory.getLogger(CheckM3U8Link.class);

    // 使用一个静态实例来复用HttpClient和其内部的连接池
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // 默认使用HTTP/2
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(SpiderProperties.M3U8_TIMEOUT_MILLISECONDS)) // 设置连接超时时间
            .build();

    public static boolean isM3U8LinkValid(String m3u8Url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(m3u8Url))
                .GET() // 默认就是GET所以其实可以省略
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // 检查 HTTP 响应代码和内容类型
            if (response.statusCode() == HttpURLConnection.HTTP_OK){
                return true;
            }
        } catch (Exception e) {
            logger.warn("{} , connect timed out", m3u8Url, e);
        }
        return false;
    }
}