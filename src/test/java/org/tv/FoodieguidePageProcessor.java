package org.tv;

import org.apache.commons.lang3.StringUtils;
import org.tv.utils.CheckM3U8Link;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodieguidePageProcessor implements PageProcessor {

    private String[] keys;

    public FoodieguidePageProcessor(String... keys) {
        this.keys = keys;
    }

    private Site site = Site.me()
            .setRetryTimes(3)
            .setTimeOut(10000)
            .setSleepTime(3000);

    @Override
    public void process(Page page) {
        List<Selectable> nodes = page.getHtml().css("div.tables > div.result").nodes();
        Map<String, String> m3u8Map = getM3u8Map(nodes);
        for (String key : keys) {
            addPage(page, m3u8Map, key);
        }
    }

    private void addPage(Page page, Map<String, String> m3u8Map, String key) {
        String m3u8 = m3u8Map.get(key.toUpperCase());
        if (StringUtils.isNotBlank(m3u8)) {
            page.putField(key.toUpperCase(), m3u8);
        }
    }

    private Map<String, String> getM3u8Map(List<Selectable> nodes) {
        Map<String, String> m3u8Map = new HashMap<>();
        for (Selectable n : nodes) {
            String name = n.xpath("//div[@class='channel']/a/div/text()").toString();
            String m3u8 = n.xpath("//div[@class='m3u8']/table/tbody").regex("<td style=\\\"padding-left: 6px;\\\">(.*?)</td>", 1).toString();
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(m3u8)) {
                if (CheckM3U8Link.isM3U8LinkValid(m3u8)) {
                    m3u8Map.put(name.trim().toUpperCase(), m3u8.trim());
                    break;
                }
            }
        }
        return m3u8Map;
    }

    @Override
    public Site getSite() {
        return site;
    }
}