package com.kstech.nexecheck.domain.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemStatusEnum;
import com.kstech.nexecheck.utils.Globals;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/10/26.
 */

public class CheckItemDao {

    /**
     * 初始化添加检查项
     * @param excId
     */
    public static void addCheckItem(String excId, Context context) {

        List<CheckItemVO> checkItemList = Globals.getModelFile().getCheckItemList();

        for (CheckItemVO checkItem : checkItemList) {
            ContentValues cv = new ContentValues();
            cv.put("exc_id", excId);
            cv.put("item_id", checkItem.getId());
            cv.put("item_name", checkItem.getName());
            cv.put("param_value", checkItem.getJsonParams());
            cv.put("sum_times", "0");
            cv.put("check_status",
                    CheckItemStatusEnum.UN_CHECK.getCode());
            cv.put("check_desc", "");
            cv.put("checker_name", Globals.getCurrentUser()
                    .getName());
            DatabaseManager.getInstance(context).insert("check_item", cv);
        }
    }

    public static void updateCheckDesc(String excId, String itemId,
                                       String checkDesc, Context context) {
        // 保存说明
        DatabaseManager
                .getInstance(context)
                .execSQL(
                        "update check_item set check_desc = ? where exc_id = ? and item_id = ?",
                        new String[] { checkDesc, excId, itemId });
    }

    /**
     * 从数据库中读取 机型 检验项目列表
     *
     * @param cr
     * @return
     */
    public static List<CheckItemEntity> getCheckItemListFromDB(String excId,Context context) {
        List<CheckItemEntity> result = new ArrayList<CheckItemEntity>();
        Cursor c = DatabaseManager.getInstance(context).query("check_item", null,
                "exc_id=? ", new String[] { excId }, null, null, null);
        while (c.moveToNext()) {
            CheckItemEntity item = new CheckItemEntity();
            item.setExcId(excId);
            item.setItemId(c.getString(c.getColumnIndex("item_id")));
            item.setItemName(c.getString(c.getColumnIndex("item_name")));
            item.setParamValue(c.getString(c.getColumnIndex("param_value")));
            item.setSumTimes(CheckItemDetailDao.getAllTimesForItme(context,excId,item.getItemId()));
            item.setSumTimesNoPass(CheckItemDetailDao.getAllTimesNoPassForItme(context,excId, item.getItemId()));
            item.setCheckStatus(c.getString(c.getColumnIndex("check_status")));
            item.setCheckDesc(c.getString(c.getColumnIndex("check_desc")));
            item.setCheckerName(c.getString(c.getColumnIndex("checker_name")));
            result.add(item);
        }
        return result;
    }

    /**
     * 从数据库中读取单个检验项目信息
     *
     * @param cr
     * @return
     */
    public static CheckItemEntity getSingleCheckItemFromDB(String excId, String itemId,Context context) {
        CheckItemEntity result = new CheckItemEntity();
        Cursor c = DatabaseManager.getInstance(context).query("check_item", null,
                "exc_id=? and item_id=?", new String[] { excId, itemId }, null,
                null, null);
        if (c.moveToNext()) {
            result.setExcId(excId);
            result.setItemId(itemId);
            result.setParamValue(c.getString(c.getColumnIndex("param_value")));
            result.setSumTimes(c.getInt(c.getColumnIndex("sum_times")));
            result.setCheckDesc(c.getString(c.getColumnIndex("check_desc")));
            result.setItemName(c.getString(c.getColumnIndex("item_name")));
            result.setCheckStatus(c.getString(c.getColumnIndex("check_status")));
            result.setCheckerName(c.getString(c.getColumnIndex("checker_name")));
            return result;
        }
        return result;
    }

    /**
     * 更新检查次数和状态
     * @param checkStatusCode
     * @param times
     * @param excId
     * @param itemId
     */
    public static void updateCheckItem(String checkStatusCode, int times,String excId,String itemId,Context context) {
        DatabaseManager.getInstance(context).execSQL(
                "update check_item set check_status = ?,sum_times = ?  where exc_id = ? and item_id = ?",
                new String[] { checkStatusCode, String.valueOf(times),
                        excId,itemId });
    }
}
