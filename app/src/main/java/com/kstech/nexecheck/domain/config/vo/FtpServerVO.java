package com.kstech.nexecheck.domain.config.vo;

/**
 * Created by lenovo on 2016/10/25.
 */

public class FtpServerVO {

    private String ip;

    private int port;

    private String user;

    private String password;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(String strPort) {
        if (strPort != null) {
            this.port = Integer.valueOf(strPort);
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
