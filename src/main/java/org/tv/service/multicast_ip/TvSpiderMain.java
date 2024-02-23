package org.tv.service.multicast_ip;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tv.common.M3UFilePipeline;
import org.tv.common.SpiderProperties;
import org.tv.service.multicast_ip.entity.HostDomain;
import org.tv.utils.PropertiesUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ResultItemsCollectorPipeline;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.utils.HttpConstant;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class TvSpiderMain {
    private static Logger logger = LoggerFactory.getLogger(TvSpiderMain.class);

    private static List<HostDomain> hostUrlArr = new ArrayList<>();

    static {
        HostDomain tonkiang = new HostDomain();
        tonkiang.setHost("tonkiang.us");
        tonkiang.setUrl("http://tonkiang.us/hoteliptv.php");
        tonkiang.setPhpUrl("http://tonkiang.us/9dlist2.php?s=%s&c=false");
        tonkiang.setReferer("http://tonkiang.us/hotellist.html?s=%s");
        hostUrlArr.add(tonkiang);

        HostDomain foodieguide = new HostDomain();
        foodieguide.setHost("foodieguide.com");
        foodieguide.setUrl("https://foodieguide.com/iptvsearch/hoteliptv.php");
        foodieguide.setPhpUrl("https://foodieguide.com/iptvsearch/alllist.php?s=%s");
        foodieguide.setReferer("https://foodieguide.com/iptvsearch/hotellist.html?s=%s");
        hostUrlArr.add(foodieguide);
    }

    public static List<String> getMulticastAddress(HostDomain hostDomain, String location) {
        DuplicateRemovedScheduler duplicateRemovedScheduler = new QueueScheduler()
                .setDuplicateRemover(new BloomFilterDuplicateRemover(100000));
        List<String> result = new ArrayList<>();
        int time = 10;
        while (result.size() == 0 && time > 0) {
            time--;
            String hostUrl = hostDomain.getUrl();
            String host = hostDomain.getHost();
            Request request = new Request(hostUrl);
            request.setMethod(HttpConstant.Method.GET);
            request.addHeader("Host", host);
            request.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            ResultItemsCollectorPipeline resultItemsCollectorPipeline = new ResultItemsCollectorPipeline();
            Spider.create(new MulticastIpPageProcessor(location))
                    .addRequest(request)
                    .setScheduler(duplicateRemovedScheduler)
                    .addUrl(hostUrl)
                    .addPipeline(resultItemsCollectorPipeline)
                    .thread(1).run();
            List<ResultItems> collected = resultItemsCollectorPipeline.getCollected();
            if (CollectionUtils.isNotEmpty(collected)) {
                collected.forEach(c -> c.getAll().values().forEach(v -> result.add(v.toString())));
            }
        }
        return result;
    }


    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        String filePath = "./iptv";
        String location = "浙江|江苏|上海";
        if (Objects.nonNull(args)) {
            if (StringUtils.isNotBlank(args[0])) {
                filePath = args[0];
            }
            if (args.length>1 && StringUtils.isNotBlank(args[1])) {
                location = args[1];
            }
        }
        for (HostDomain hostUrl : hostUrlArr) {
            List<String> addressList = getMulticastAddress(hostUrl, location);
            if (CollectionUtils.isNotEmpty(addressList)) {
                for (String address : addressList) {
                    String host = hostUrl.getHost();
                    String url = String.format(hostUrl.getPhpUrl(), address);
                    String referer = String.format(hostUrl.getReferer(), address);
                    logger.info("flush m3u from url:{}", url);

                    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                            new FileOutputStream(filePath + "/iptv.m3u"),
                            StandardCharsets.UTF_8));
                    printWriter.println("#EXTM3U");

                    Request request = new Request(url);
                    request.setMethod(HttpConstant.Method.GET);
                    request.addHeader("Referer", referer);
                    request.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                    request.addHeader("Host", host);

                    Properties properties = PropertiesUtils.load("keyword.properties");
                    Set<Object> propertiesKeys = properties.keySet();
                    int totalCount = 0;
                    int successCount = 0;
                    for (Object key : propertiesKeys) {
                        String[] propertyValues = properties.get(key).toString().split(",");
                        M3UFilePipeline m3UFilePipeline = new M3UFilePipeline(printWriter);
                        m3UFilePipeline.setGroupName(key.toString());
                        Spider.create(new MulticastIpPageProcessor(location, propertyValues))
                                .addRequest(request)
                                .addUrl(url)
                                .addPipeline(m3UFilePipeline)
                                .thread(1).run();
                        logger.info("完成 【{}】 刷新，总数量: {};获取成功数量:{}", key, propertyValues.length, m3UFilePipeline.getCount());
                        successCount += m3UFilePipeline.getCount();
                        totalCount += propertyValues.length;
                        Thread.sleep(5000);
                    }

                    if (successCount > totalCount * 0.3) {
                        // 搜索频道达到六成结束
                        printWriter.close();
                        logger.info("更新完毕");
                        System.exit(1);
                    }
                    printWriter.close();
                }
            }
        }
        logger.info("更新完毕");
    }
}
