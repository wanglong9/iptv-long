package org.tv.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.FilePipeline;

import java.io.PrintWriter;
import java.util.Map;

public class M3UFilePipeline extends FilePipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * create a FilePipeline with default path"/data/webmagic/"
     */

    private PrintWriter printWriter;
    private String groupName = "default";

    private int count = 0;


    public M3UFilePipeline(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        try {
            for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
                if (entry.getValue() instanceof Iterable) {
                    Iterable value = (Iterable) entry.getValue();
                    printWriter.println(entry.getKey() + ":");
                    for (Object o : value) {
                        printWriter.println(o);
                    }
                } else {
                    logger.info("刷新[{},{}]", entry.getKey(), entry.getValue());
                    printWriter.println(String.format(SpiderProperties.MULTICAST_GROUP, groupName, entry.getKey()));
                    printWriter.println(entry.getValue());
                    printWriter.flush();
                    count++;
                }
            }
        } catch (Exception e) {
            logger.warn("write file error", e);
        }
    }
}
