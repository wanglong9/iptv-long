package org.tv.service.multicast_ip;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.tv.common.SpiderProperties;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MulticastIpPageProcessor implements PageProcessor {

    private String[] keys;
    private String targetLocation = "浙江";

    public MulticastIpPageProcessor(String location, String... keys) {
        this.targetLocation = location;
        this.keys = keys;
    }

    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        if (url.endsWith("hoteliptv.php")) {
            String expression = "//div[@class='box'][2]//a/@href";
            List<Selectable> nodes = page.getHtml().xpath(expression).nodes();
            if (CollectionUtils.isNotEmpty(nodes)) {
                List<String> multicastIps = nodes.stream().map(Selectable::toString).collect(Collectors.toList());
                for (String newUrl : multicastIps) {
                    page.addTargetRequest(newUrl);
                    page.setSkip(true);
                }
            }
        } else if (url.contains("hoteliptv.php?s")) {  // hoteliptv.php?s=ip 解析 multicastIpList
//            String address = page.getHtml().xpath("//div[@class='tables']//div[@class='channel']//a/b/text()").toString();
            String address = page.getHtml().xpath("//div[@class='tables']//div[@class='channel']//a/b/text()").toString();
            String location = page.getHtml().xpath("//div[@class='tables']//div[5]/i/text()").toString();
            if (StringUtils.isNotBlank(address) && location.contains(targetLocation)) {
                page.putField(SpiderProperties.MULTICAST_IPS, address.trim());
            } else {
                page.setSkip(true);
            }
        } else {
//            List<Selectable> nodes = page.getHtml().css("div.tables > div.result").nodes();
            List<Selectable> nodes = page.getHtml().xpath("//div[@class='tables']//div[@class='result']").nodes();
            Map<String, String> m3u8Map = getM3u8Map(nodes);
            for (String key : keys) {
                addPage(page, m3u8Map, key);
            }
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
            String name = n.regex("<div style=\\\"float: left;\\\">(.*?)</div>", 1).toString();
            String m3u8 = n.regex("<td style=\\\"padding-left: 6px;\\\">(.*?)</td>", 1).toString();
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(m3u8)) {
                m3u8Map.put(name.trim().toUpperCase(), m3u8);
            }
        }
        return m3u8Map;
    }

    private Site site = Site.me()
            .setRetryTimes(0)
            .setTimeOut(10000)
            .setSleepTime(3000);

    @Override
    public Site getSite() {
        return site;
    }
}
