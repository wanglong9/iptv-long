package org.tv;

import org.tv.common.SpiderProperties;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        String searchKey = "浙江卫视";

        Request request = new Request("https://foodieguide.com/iptvsearch/");
        request.setMethod(HttpConstant.Method.POST);
        Map<String, Object> formMap = new HashMap<>();
        formMap.put(SpiderProperties.SEARCH_KEY, searchKey);
        HttpRequestBody requestBody = HttpRequestBody.form(formMap, "utf-8");

        request.setRequestBody(requestBody);
        request.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        request.addHeader("Host", "foodieguide.com");

        Spider.create(new FoodieguidePageProcessor(searchKey)).addRequest(request)
                .addPipeline(new FilePipeline("./iptv/test"))
                .thread(1).run();
    }
}
