package com.kstech.nexecheck.domain.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.entity.User;
import com.kstech.nexecheck.domain.db.dbenum.UserStatusEnum;
import com.kstech.nexecheck.domain.db.dbenum.UserTypeEnum;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class UserDao {

	private static final String TABLE_NAME = "user";

	private static final String ADMIN = "admin";

	public static String getAdminName() {
		return ADMIN;
	}

	public static void initData(SQLiteDatabase db) {
		// 初始化管理员
		ContentValues admin = new ContentValues();
		admin.put("name", ADMIN);
		admin.put("code", ADMIN);
		admin.put("pwd", "111111");
		admin.put("type", "0");
		admin.put("status", "1");
		admin.put("create_time", DateUtil.getDateTimeFormat(new Date()));
		admin.put("creator_code", "000001");
		admin.put("stop_time", "");
		admin.put("stop_user_code", "");
		db.insert(TABLE_NAME,null, admin);
		// 初始化检验员
		ContentValues check1 = new ContentValues();
		check1.put("name", "check1");
		check1.put("code", "000010");
		check1.put("pwd", "111111");
		check1.put("type", "1");
		check1.put("status", "1");
		check1.put("create_time", DateUtil.getDateTimeFormat(new Date()));
		check1.put("creator_code", "000001");
		check1.put("stop_time", "");
		check1.put("stop_user_code", "");
		db.insert(TABLE_NAME, null,check1);
	}

	/**
	 * 用户登录
	 *
	 * @param userName
	 * @param pwd
	 * @return 登录失败返回null，登录成功返回用户实体
	 */
	public static User login(String userName, String pwd, Context context) {
		User result = null;
		// 获取用户名和密码
		Cursor c = DatabaseManager.getInstance(context)
				.query(TABLE_NAME,
						null,
						"name=? and pwd=? and status=?",
						new String[] { userName, pwd,
								UserStatusEnum.ENABLE.getCode() }, null, null,
						null);
		// 用户名与密码正确
		if (c.moveToNext()) {
			User currentUser = new User();
			currentUser.setName(userName);
			currentUser.setCode(c.getString(c.getColumnIndex("code")));
			currentUser.setType(c.getInt(c.getColumnIndex("type")));
			currentUser.setPwd(c.getString(c.getColumnIndex("pwd")));
			// 保存到上下文中
			Globals.setCurrentUser(currentUser);
			// 保存到shared中
			ConfigFileManager.getInstance(context).saveLastUserName(userName);
			result = currentUser;
		}
		// 关闭流
		return result;
	}

	/**
	 * 读取用户列表
	 *
	 * @return
	 */
	public static List<Map<String, Object>> findUserListReturnListMap(String status, Context context) {
		Cursor c = DatabaseManager.getInstance(context).query(TABLE_NAME, null,
				"status=?", new String[] { status }, null, null, null);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		while (c.moveToNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("code", c.getString(c.getColumnIndex("code")));
			map.put("image",
					c.getString(c.getColumnIndex("type")).equals(
							UserTypeEnum.MANAGER.getCode()) ? R.drawable.admin
							: R.drawable.check);
			map.put("name", c.getString(c.getColumnIndex("name")));
			list.add(map);
		}
		return list;
	}

	/**
	 * 读取可用的用户名数组
	 *
	 * @return
	 */
	public static String[] findUserNameAry(Context context) {
		List<String> userlist = new ArrayList<String>();
		Cursor c = DatabaseManager.getInstance(context).query(TABLE_NAME, null,
				"status=?", new String[] { UserStatusEnum.ENABLE.getCode() },
				null, null, null);
		while (c.moveToNext()) {
			String name = c.getString(c.getColumnIndex("name"));
			userlist.add(name);
		}
		return userlist.toArray(new String[userlist.size()]);
	}

}
