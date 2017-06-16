package com.kstech.nexecheck.domain.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.entity.CheckItemDetailEntity;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemDetailStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.config.vo.CheckItemParamValueVO;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemStatusEnum;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lenovo on 2016/10/26.
 */

public class CheckItemDetailDao {

    /**
     * 读取检查项目的 参数 列表数据
     *
     * @param excId  挖掘机出厂编号
     * @param itmeId 检验项目ID
     * @return
     * @author wanghaibin
     */
    public static List<CheckItemDetailEntity> getCheckItemDetailList(Context context,String excId, String itemId) {

        List<CheckItemDetailEntity> result = new ArrayList<CheckItemDetailEntity>();

        // 增加排序
        Cursor cursor = DatabaseManager.getInstance(context).rawQuery(
                "select * from check_item_detail where exc_id=? and item_id=? order by check_time desc",
                new String[]{excId, itemId});

        while (cursor.moveToNext()) {
            CheckItemDetailEntity detail = new CheckItemDetailEntity();
            detail.setCheckStatus(cursor.getString(cursor
                    .getColumnIndex("check_status")));
            detail.setCheckError(cursor.getString(cursor
                    .getColumnIndex("check_error")));
            detail.setCheckTime(cursor.getString(cursor
                    .getColumnIndex("check_time")));
            detail.setCheckerCode(cursor.getString(cursor
                    .getColumnIndex("checker_code")));
            detail.setCheckerName(cursor.getString(cursor
                    .getColumnIndex("checker_name")));
            String paramValue = cursor.getString(cursor
                    .getColumnIndex("param_value"));
            detail.setParamValue(paramValue);
            result.add(detail);
        }
        return result;
    }

    ;

    public static List<CheckItemDetailEntity> getCheckItemDetailListLimit(Context context,String excId, String itemId) {

        List<CheckItemDetailEntity> result = new ArrayList<CheckItemDetailEntity>();

        // 增加排序
        Cursor cursor = DatabaseManager.getInstance(context).rawQuery(
                "select * from check_item_detail where exc_id=? and item_id=? order by check_time desc limit 5",
                new String[]{excId, itemId});

        while (cursor.moveToNext()) {
            CheckItemDetailEntity detail = new CheckItemDetailEntity();
            detail.setCheckStatus(cursor.getString(cursor
                    .getColumnIndex("check_status")));
            detail.setCheckError(cursor.getString(cursor
                    .getColumnIndex("check_error")));
            detail.setCheckTime(cursor.getString(cursor
                    .getColumnIndex("check_time")));
            detail.setCheckerCode(cursor.getString(cursor
                    .getColumnIndex("checker_code")));
            detail.setCheckerName(cursor.getString(cursor
                    .getColumnIndex("checker_name")));
            String paramValue = cursor.getString(cursor
                    .getColumnIndex("param_value"));
            detail.setParamValue(paramValue);
            result.add(detail);
        }
        return result;
    }

    ;

    public static List<CheckItemDetailEntity> getCheckItemDetailListLimit(Context context,String excId, String itemId, int size) {

        List<CheckItemDetailEntity> result = new ArrayList<CheckItemDetailEntity>();

        // 增加排序
        Cursor cursor = DatabaseManager.getInstance(context).rawQuery(
                "select * from check_item_detail where exc_id=? and item_id=? order by check_time desc limit " + size,
                new String[]{excId, itemId});

        while (cursor.moveToNext()) {
            CheckItemDetailEntity detail = new CheckItemDetailEntity();
            detail.setCheckStatus(cursor.getString(cursor
                    .getColumnIndex("check_status")));
            detail.setCheckError(cursor.getString(cursor
                    .getColumnIndex("check_error")));
            detail.setCheckTime(cursor.getString(cursor
                    .getColumnIndex("check_time")));
            detail.setCheckerCode(cursor.getString(cursor
                    .getColumnIndex("checker_code")));
            detail.setCheckerName(cursor.getString(cursor
                    .getColumnIndex("checker_name")));
            String paramValue = cursor.getString(cursor
                    .getColumnIndex("param_value"));
            detail.setParamValue(paramValue);
            result.add(detail);
        }
        return result;
    }

    ;

    /**
     * 插入详情记录，并且更新检查项信息
     *
     * @param detailStatus
     * @param excId
     * @param checkItemEntity
     * @param headers
     * @param checkItemVO
     */
    public static void insertDetailAndUpdateItem(Context context,String detailStatus, CheckItemEntity checkItemEntity, List<CheckItemParamValueVO> headers, CheckItemVO checkItemVO) {
        // 插入check_item_detail 表 检查项详情记录
        long l = addCheckItemDetail(context,checkItemEntity.getExcId(),
                checkItemEntity.getItemId(), headers, detailStatus);
        if (l == -1) {
            return;
        }

        // 查询检验次数 从数据库中查找 不能使用之前的
        int times = CheckItemDao.getSingleCheckItemFromDB(checkItemEntity.getExcId(),checkItemEntity.getItemId(),context).getSumTimes() + 1;
        String checkStatusCode = "";
        // 测量次数没有达到要求的统计次数
        int itemTimeConfig = checkItemVO.getTimes();
        if (times < itemTimeConfig) {
            // 第一次检测，设置该项目的检查状态为 未完成
            checkStatusCode = CheckItemStatusEnum.UN_FINISH.getCode();
        } else if (times >= itemTimeConfig) {
            // 测量次数达到或超过了要求的统计次数，并且程序根据标准值和限值
            // 自动判断每次测量记录（最近的统计次数）结果都合格；则程序判定检验项目合格，并根据规则计算检验记录的参数值
            List<CheckItemDetailEntity> checkItemDetailList = CheckItemDetailDao
                    .getCheckItemDetailList(context,checkItemEntity.getExcId(),
                            checkItemEntity.getItemId());
            // 先赋值为合格
            checkStatusCode = CheckItemStatusEnum.PASS.getCode();
            // 判断最近检测次数是否全部合格（要求的连续合格次数）
            for (int i = 0; i < itemTimeConfig; i++) {
                if (!checkItemDetailList.get(i).getCheckStatus()
                        .equals(CheckItemDetailStatusEnum.PASS.getCode())) {
                    checkStatusCode = CheckItemStatusEnum.UN_PASS.getCode();
                    break;
                }
            }
        }
        // 更新 check_item 检查项状态 和 检查次数
        CheckItemDao.updateCheckItem(checkStatusCode, times, checkItemEntity.getExcId(),
                checkItemEntity.getItemId(),context);
    }

    /**
     * 添加检查项详情
     *
     * @param excId
     * @param itemId
     * @param headers
     * @param status
     */
    private static long addCheckItemDetail(Context context,String excId, String itemId, List<CheckItemParamValueVO> headers, String status) {
        ContentValues checkItemDetailCV = new ContentValues();
        checkItemDetailCV.put("exc_id", excId);
        checkItemDetailCV.put("item_id", itemId);
        String param_value = JsonUtils.toJson(headers);
        checkItemDetailCV.put("param_value", param_value);
        checkItemDetailCV.put("check_time", DateUtil.getDateTimeFormat(new Date()));
//		checkItemDetailCV.put("check_error", CheckItemDetailStatusEnum
//				.getName(status == 3 ? "status=3有值" : ""));
        checkItemDetailCV.put("check_status", status);
        checkItemDetailCV.put("checker_code", Globals.getCurrentUser()
                .getCode());
        checkItemDetailCV.put("checker_name", Globals.getCurrentUser()
                .getName());
        return DatabaseManager.getInstance(context).insertWithOnConflict("check_item_detail", checkItemDetailCV, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * 获取检测总次数
     *
     * @param excId
     * @return
     */
    public static int getAllTimes(Context context,String excId) {
        int times = 0;
        String sql = "select count(*) as times from check_item_detail where exc_id = ?";
        String[] selectionArgs = new String[]{excId};
        Cursor rawQuery = DatabaseManager.getInstance(context).rawQuery(sql,
                selectionArgs);
        if (rawQuery.moveToNext()) {
            times = rawQuery.getInt(rawQuery.getColumnIndex("times"));
        }
        return times;
    }

    /**
     * 获取检测不合格总次数
     *
     * @param excId
     * @return
     */
    public static int getAllTimesNoPass(Context context,String excId) {
        int times = 0;
        String sql = "select count(*) as times from check_item_detail where exc_id = ? and check_status != ?";
        String[] selectionArgs = new String[]{excId, CheckItemDetailStatusEnum.PASS.getCode()};
        Cursor rawQuery = DatabaseManager.getInstance(context).rawQuery(sql,
                selectionArgs);
        if (rawQuery.moveToNext()) {
            times = rawQuery.getInt(rawQuery.getColumnIndex("times"));
        }
        return times;
    }

    /**
     * 获取某一个检查项目，检测总次数
     *
     * @param excId
     * @return
     */
    public static int getAllTimesForItme(Context context,String excId, String itemId) {
        int times = 0;
        String sql = "select count(*) as times from check_item_detail where exc_id = ? and item_id = ?";
        String[] selectionArgs = new String[]{excId, itemId};
        Cursor rawQuery = DatabaseManager.getInstance(context).rawQuery(sql,
                selectionArgs);
        if (rawQuery.moveToNext()) {
            times = rawQuery.getInt(rawQuery.getColumnIndex("times"));
        }
        return times;
    }

    /**
     * 获取某一个检查项目，检测不合格总次数
     *
     * @param excId
     * @return
     */
    public static int getAllTimesNoPassForItme(Context context,String excId, String itemId) {
        int times = 0;
        String sql = "select count(*) as times from check_item_detail where exc_id = ? and check_status != ? and item_id = ?";
        String[] selectionArgs = new String[]{excId, CheckItemDetailStatusEnum.PASS.getCode(), itemId};
        Cursor rawQuery = DatabaseManager.getInstance(context).rawQuery(sql,
                selectionArgs);
        if (rawQuery.moveToNext()) {
            times = rawQuery.getInt(rawQuery.getColumnIndex("times"));
        }
        return times;
    }
}
