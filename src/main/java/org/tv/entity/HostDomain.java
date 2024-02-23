package org.tv.entity;

import java.io.Serializable;


public class HostDomain implements Serializable {

    private String host;
    private String url;

    private String referer;

    private String phpUrl;

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getPhpUrl() {
        return phpUrl;
    }

    public void setPhpUrl(String phpUrl) {
        this.phpUrl = phpUrl;
    }
}
