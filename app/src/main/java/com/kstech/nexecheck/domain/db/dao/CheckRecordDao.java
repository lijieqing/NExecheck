package com.kstech.nexecheck.domain.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.entity.CheckItemDetailEntity;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.domain.db.dbenum.CheckRecordStatusEnum;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2016/10/26.
 */

public class CheckRecordDao {

    /**
     * 修改整机的检验说明
     * @param excId 挖机编号
     * @param checkDesc 检验说明
     */
    public static void updateCheckDesc(Context context,String excId, String checkDesc) {
        // 保存说明
        DatabaseManager.getInstance(context).execSQL(
                "update check_record set desc = ? where exc_id = ?",
                new String[] { checkDesc, excId });
    }

    /**
     * 修改整机的检验状态
     * @param excId 挖机编号
     * @param checkDesc 检验说明
     */
    public static void updateCheckStatus(Context context,String excId, String status,String finishTime) {
        DatabaseManager.getInstance(context).execSQL(
                "update check_record set check_status = ?,checker_code = ?, checker_name = ?,finish_time = ? where exc_id = ?",
                new String[] { status, Globals.getCurrentUser().getName(),
                        Globals.getCurrentUser().getName(),finishTime,excId });
    }

    /**
     * 向数据库表check_record中加入一条检验记录，同时为这条记录初始化对应的check_item表数据
     *
     * @param excId
     *            挖机出厂编号
     * @param deviceId
     *            机型ID
     * @param deviceName
     *            机型名字
     * @param subDeviceId
     *            子机型ID
     * @param subDeviceName
     *            子机型名字
     */
    public static void addCheckRecord(Context context,String excId, String deviceId, String deviceName, String subDeviceId, String subDeviceName) {
        ContentValues cv = new ContentValues();
        cv.put("exc_id", excId);
        cv.put("device_id", deviceId);
        cv.put("device_name", deviceName);
        cv.put("subdevice_id", subDeviceId);
        cv.put("subdevice_name", subDeviceName);
        cv.put("check_status", CheckRecordStatusEnum.UN_FINISH.getCode());
        cv.put("create_time", DateUtil.getDateTimeFormat(new Date()));
        cv.put("checker_code", Globals.getCurrentUser().getCode());
        cv.put("checker_name", Globals.getCurrentUser().getName());
        cv.put("checkline_name", Globals.getCurrentCheckLine().getName());
        DatabaseManager.getInstance(context).insert("check_record", cv);

        // 将检验项目信息插入到数据库 检验项目表
        CheckItemDao.addCheckItem(excId,context);

    }

    /**
     * 通过挖掘机出厂编号，读取检查记录 一个车辆最多只能有一条检测记录
     *
     * @return
     */
    public static CheckRecordEntity findCheckRecordByExcId(Context context,String excId) {
        Cursor c = DatabaseManager.getInstance(context).query("check_record", null,
                "exc_id=?", new String[] { excId }, null, null, null);
        CheckRecordEntity cr = null;
        if (c.moveToNext()) {
            cr = new CheckRecordEntity(c.getString(c.getColumnIndex("exc_id")),
                    c.getString(c.getColumnIndex("device_id")), c.getString(c
                    .getColumnIndex("device_name")), c.getString(c
                    .getColumnIndex("subdevice_id")), c.getString(c
                    .getColumnIndex("subdevice_name")),
                    CheckRecordStatusEnum.getName(c.getString(c
                            .getColumnIndex("check_status"))), c.getString(c
                    .getColumnIndex("create_time")), c.getString(c
                    .getColumnIndex("finish_time")), c.getString(c
                    .getColumnIndex("manager_code")), c.getString(c
                    .getColumnIndex("manager_name")), c.getString(c
                    .getColumnIndex("checker_code")), c.getString(c
                    .getColumnIndex("checker_name")), c.getString(c
                    .getColumnIndex("desc")), c.getString(c
                    .getColumnIndex("checkline_name")), c.getString(c
                    .getColumnIndex("checkline_ip")));
        }
        // 关闭流
        return cr;
    }

    /**
     * 读取所有检查记录
     */
    public static List<CheckRecordEntity> findAllCheckRecord(Context context) {
        Cursor c = DatabaseManager.getInstance(context).query("check_record", null,
                null, null, null, null, null);
        List<CheckRecordEntity> result = new ArrayList<CheckRecordEntity>();
        while (c.moveToNext()) {
            CheckRecordEntity cr = new CheckRecordEntity(c.getString(c
                    .getColumnIndex("exc_id")), c.getString(c
                    .getColumnIndex("device_id")), c.getString(c
                    .getColumnIndex("device_name")), c.getString(c
                    .getColumnIndex("subdevice_id")), c.getString(c
                    .getColumnIndex("subdevice_name")), c.getString(c
                    .getColumnIndex("check_status")), c.getString(c
                    .getColumnIndex("create_time")), c.getString(c
                    .getColumnIndex("finish_time")), c.getString(c
                    .getColumnIndex("manager_code")), c.getString(c
                    .getColumnIndex("manager_name")), c.getString(c
                    .getColumnIndex("checker_code")), c.getString(c
                    .getColumnIndex("checker_name")), c.getString(c
                    .getColumnIndex("desc")), c.getString(c
                    .getColumnIndex("checkline_name")), c.getString(c
                    .getColumnIndex("checkline_ip")));

            result.add(cr);
        }
        // 关闭流
        return result;
    }

    public static LinkedList<String> findCheckRecordByUserName(Context context,String username){
        LinkedList<String> result = new LinkedList<>();
        Cursor c = null;
        if("1".equals(Globals.getCurrentUser().getType().getCode())){
            c = DatabaseManager.getInstance(context).query("check_record", null,
                    "checker_name=? order by create_time desc", new String[] { username }, null, null, null);
        }else {
            c = DatabaseManager.getInstance(context).query("check_record", null,
                    null, null, null, null, "desc");
        }

        CheckRecordEntity cr = null;
        while (c.moveToNext()) {
            cr = new CheckRecordEntity(c.getString(c.getColumnIndex("exc_id")),
                    c.getString(c.getColumnIndex("device_id")), c.getString(c
                    .getColumnIndex("device_name")), c.getString(c
                    .getColumnIndex("subdevice_id")), c.getString(c
                    .getColumnIndex("subdevice_name")),
                    CheckRecordStatusEnum.getName(c.getString(c
                            .getColumnIndex("check_status"))), c.getString(c
                    .getColumnIndex("create_time")), c.getString(c
                    .getColumnIndex("finish_time")), c.getString(c
                    .getColumnIndex("manager_code")), c.getString(c
                    .getColumnIndex("manager_name")), c.getString(c
                    .getColumnIndex("checker_code")), c.getString(c
                    .getColumnIndex("checker_name")), c.getString(c
                    .getColumnIndex("desc")), c.getString(c
                    .getColumnIndex("checkline_name")), c.getString(c
                    .getColumnIndex("checkline_ip")));
            result.add(c.getString(c.getColumnIndex("exc_id")));
        }
        // 关闭流
        return  result;
    }

    /**
     * 通过挖掘机出厂编号进行模糊查询
     */
    public static List<CheckRecordEntity> findCheckRecordByCondition(Context context,String finishTime, String checkStatus, String excId, String deviceId, String subdeviceId) {
        String name = Globals.getCurrentUser().getName();
        StringBuffer selection = new StringBuffer();
        selection.append("1=1");
        List<String> selectionArgs = new ArrayList<String>();
        if("1".equals(Globals.getCurrentUser().getType().getCode())){
            selection.append(" and checker_name = ?");
            selectionArgs.add(Globals.getCurrentUser().getName());
        }
        if (null != finishTime && !"".equals(finishTime)) {
            selection.append(" and finish_time = ?");
            selectionArgs.add(finishTime);
        }
        if (null != checkStatus && !"".equals(checkStatus)) {
            selection.append(" and check_status = ?");
            selectionArgs.add(checkStatus);
        }
        if (null != excId && !"".equals(excId)) {
            selection.append(" and exc_id like ?");
            selectionArgs.add("%" + excId + "%");
        }
        if (null != deviceId && !"".equals(deviceId)) {
            selection.append(" and device_name = ?");
            selectionArgs.add(deviceId);
        }
        if (null != subdeviceId && !"".equals(subdeviceId)) {
            selection.append(" and subdevice_name = ?");
            selectionArgs.add(subdeviceId);
        }
        selection.append(" order by create_time desc");
        Cursor c = DatabaseManager.getInstance(context).query(
                "check_record",
                null,
                selection.toString(),
                (String[]) selectionArgs.toArray(new String[selectionArgs
                        .size()]), null, null, null);
        List<CheckRecordEntity> result = new ArrayList<CheckRecordEntity>();
        while (c.moveToNext()) {
            CheckRecordEntity cr = new CheckRecordEntity(c.getString(c
                    .getColumnIndex("exc_id")), c.getString(c
                    .getColumnIndex("device_id")), c.getString(c
                    .getColumnIndex("device_name")), c.getString(c
                    .getColumnIndex("subdevice_id")), c.getString(c
                    .getColumnIndex("subdevice_name")), c.getString(c
                    .getColumnIndex("check_status")), c.getString(c
                    .getColumnIndex("create_time")), c.getString(c
                    .getColumnIndex("finish_time")), c.getString(c
                    .getColumnIndex("manager_code")), c.getString(c
                    .getColumnIndex("manager_name")), c.getString(c
                    .getColumnIndex("checker_code")), c.getString(c
                    .getColumnIndex("checker_name")), c.getString(c
                    .getColumnIndex("desc")), c.getString(c
                    .getColumnIndex("checkline_name")), c.getString(c
                    .getColumnIndex("checkline_ip")));
            cr.setSumTimes(CheckItemDetailDao.getAllTimes(context,cr.getExcId()));
            cr.setSumTimesNoPass(CheckItemDetailDao.getAllTimesNoPass(context,cr.getExcId()));
            cr.setCheckBoxState(false);
            result.add(cr);
        }
        // 关闭流
        return result;
    }

    /**
     * 获取所有检查状态
     */
    public static List<Map<String, Object>> findAllCheckStatus() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", CheckRecordStatusEnum.getName("0"));
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("name", CheckRecordStatusEnum.getName("1"));
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("name", CheckRecordStatusEnum.getName("2"));
        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put("name", CheckRecordStatusEnum.getName("3"));
        result.add(map);
        result.add(map1);
        result.add(map2);
        result.add(map3);
        return result;
    }

    /**
     * 查询选中的检查记录，对应的检查项与检查项详情信息
     * @param listData
     * @return
     */
    public static List<CheckRecordEntity> findUploadData(Context context,List<CheckRecordEntity> listData){
        for (CheckRecordEntity record:listData) {
            if (record.getCheckBoxState()) {
                List<CheckItemEntity> checkItemList = CheckItemDao.getCheckItemListFromDB(record.getExcId(),context);
                record.setCheckItemList(checkItemList);
                for (CheckItemEntity item:checkItemList){
                    List<CheckItemDetailEntity> checkItemDetailList = CheckItemDetailDao.getCheckItemDetailList(context,record.getExcId(), item.getItemId());
                    item.setCheckItemDetailList(checkItemDetailList);
                }
            }
        }
        return listData;
    }

}
