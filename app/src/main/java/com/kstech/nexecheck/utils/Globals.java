package com.kstech.nexecheck.utils;

import android.app.Activity;
import android.content.Context;

import com.kstech.nexecheck.domain.config.DeviceModelFile;
import com.kstech.nexecheck.domain.config.ResConfigFile;
import com.kstech.nexecheck.domain.config.vo.CheckLineVO;
import com.kstech.nexecheck.domain.checkline.CheckLineManager;
import com.kstech.nexecheck.domain.db.entity.User;
import com.kstech.nexecheck.exception.ExcException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/9/19.
 */
public class Globals {
    public static String fs = File.separator;
    public static String REFPATH = fs + "storage" + fs + "sdcard1" + fs +"Models" + fs;
    public static String MODELPATH = fs + "storage" + fs + "sdcard1" + fs +"Models" + fs +"model" + fs;
    public static String UPDATE = fs + "storage" + fs + "sdcard1" + fs +"MyApp" + fs+"update.apk";
    /**
     * 当前登录的用户
     */
    private static User currentUser;

    /**
     * 当前检线信息
     */
    private static CheckLineVO currentCheckLine;

    /**
     * 对应res_config.xml配置文件的内容
     */
    private static ResConfigFile resConfig;

    /**
     * 当前选中的机型对应的机型配置文件
     */
    private static DeviceModelFile modelFile;

    /**
     * 当前系统Activity信息
     */
    private static List<Activity> acitvityList = new ArrayList<Activity>();

    /**
     * 加载当前使用的机型文件
     * @param deviceId 机型ID
     * @param subDeviceId 子机型ID
     * @throws ExcException
     */
    public static void loadDeviceModelFile(String deviceId,String subDeviceId,Context context) throws ExcException {
        String fileName = deviceId;
        if (null !=subDeviceId && !"".equals(subDeviceId)) {
            fileName = fileName + "%" + subDeviceId;
        }
        modelFile = DeviceModelFile.readFromFile(fileName+".xml",context);
    }

    // initResource 初始化res文件 之后 加载机型文件 loadDeviceModelFile
    public static void initResource(Context context) {
        resConfig = ResConfigFile.readFromFile(context);
        currentCheckLine = CheckLineManager.getDefaultCheckLine(context);
    }

    public static DeviceModelFile getModelFile() {
        return modelFile;
    }

    public static void setModelFile(DeviceModelFile modelFile) {
        Globals.modelFile = modelFile;
    }

    public static ResConfigFile getResConfig() {
        return resConfig;
    }

    public static CheckLineVO getCurrentCheckLine() {
        return currentCheckLine;
    }

    public static void setCurrentCheckLine(CheckLineVO checkLine) {
        Globals.currentCheckLine = checkLine;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        Globals.currentUser = currentUser;
    }

}
