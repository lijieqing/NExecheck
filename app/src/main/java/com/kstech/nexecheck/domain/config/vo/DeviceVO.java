package com.kstech.nexecheck.domain.config.vo;

import java.util.List;

/**
 * Created by lenovo on 2016/9/28.
 */

public class DeviceVO {
    /**
     * 机型Id
     */
    private String deviceId;
    /**
     * 机型名称
     */
    private String deviceName;
    /**
     * 机型启用日期，格式为"yyyy-mm"
     */
    private String devBornDate;
    /**
     * 机型停产日期，格式为"yyyy-mm"
     */
    private String devDieDate;
    /**
     * 机型当前状态
     */
    private String devStatus;
    /**
     * 子机型集合
     */
    private List<SubDeviceVO> subDeviceList;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevBornDate() {
        return devBornDate;
    }

    public void setDevBornDate(String devBornDate) {
        this.devBornDate = devBornDate;
    }

    public String getDevDieDate() {
        return devDieDate;
    }

    public void setDevDieDate(String devDieDate) {
        this.devDieDate = devDieDate;
    }

    public String getDevStatus() {
        return devStatus;
    }

    public void setDevStatus(String devStatus) {
        this.devStatus = devStatus;
    }

    public DeviceVO(String deviceId, String deviceName, String devBornDate,
                    String devDieDate, String devStatus, List<SubDeviceVO> subDeviceList) {
        super();
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.devBornDate = devBornDate;
        this.devDieDate = devDieDate;
        this.devStatus = devStatus;
        this.subDeviceList = subDeviceList;
    }

    public List<SubDeviceVO> getSubDeviceList() {
        return subDeviceList;
    }

    public void setSubDeviceList(List<SubDeviceVO> subDeviceList) {
        this.subDeviceList = subDeviceList;
    }
}
