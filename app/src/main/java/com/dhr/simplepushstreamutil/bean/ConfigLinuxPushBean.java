package com.dhr.simplepushstreamutil.bean;

public class ConfigLinuxPushBean {
    private int status;// 0  每次都弹  1  每次都是后台  2 每次都是前台

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
