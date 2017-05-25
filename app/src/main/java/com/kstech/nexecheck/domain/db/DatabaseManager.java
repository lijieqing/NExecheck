/**
 * 
 */
package com.kstech.nexecheck.domain.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kstech.nexecheck.domain.db.dao.UserDao;


/**
 * @author tan
 * 
 */
public class DatabaseManager extends SQLiteOpenHelper {

	private static final String DB_NAME = "newBee";
	private static final int DB_VERSION = 1;

	private static DatabaseManager instance;

	private DatabaseManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public static synchronized DatabaseManager getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseManager(context);
		}

		return instance;
	}

	public synchronized Cursor query(String table, String[] columns,
									 String selection, String[] selectionArgs, String groupBy,
									 String having, String orderBy) {
		return getWritableDatabase().query(table, columns, selection,
				selectionArgs, groupBy, having, orderBy);
	}

	public synchronized Cursor rawQuery(String sql, String[] selectionArgs) {
		return getWritableDatabase().rawQuery(sql, selectionArgs);
	}

	public synchronized void execSQL(String sql, String[] bindArgs) {
		getWritableDatabase().execSQL(sql, bindArgs);
	}

	public synchronized int update(String table, ContentValues values,
								   String whereClause, String[] whereArgs) {
		return getWritableDatabase().update(table, values, whereClause,
				whereArgs);
	}

	public synchronized void insert(String sql) {
		getWritableDatabase().execSQL(sql);
	}

	public  long insertWithOnConflict(String table, ContentValues initialValues,int conflictAlgorithm) {
		return getWritableDatabase().insertWithOnConflict(table,null,initialValues,conflictAlgorithm);
	}

	public synchronized long insert(String table, ContentValues values) {
		return getWritableDatabase().insert(table, null, values);
	}

	public synchronized int delete(String table, String whereClause,
								   String[] whereArgs) {
		return getWritableDatabase().delete(table, whereClause, whereArgs);
	}

	public void onCreate(SQLiteDatabase db) {
		// 初始化表
		initTable(db);
		// 初始化数据
		UserDao.initData(db);
	}


	/**
	 * 初始化表
	 *
	 * @param db
	 */
	private void initTable(SQLiteDatabase db) {
		// 用户表
		db.execSQL("CREATE TABLE user("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "name VARCHAR(16)," + "code VARCHAR(16),"
				+ "pwd VARCHAR(16)," + "type SMALLINT," + "status SMALLINT,"
				+ "create_time DATETIME," + "creator_code VARCHAR(16),"
				+ "stop_time DATETIME," + "stop_user_code VARCHAR(16) )");

		// 检查记录表
		db.execSQL("CREATE TABLE check_record("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "exc_id VARCHAR(16)," + "device_id VARCHAR(16),"
				+ "device_name VARCHAR(16)," + "subdevice_id VARCHAR(16),"
				+ "subdevice_name VARCHAR(16)," + "check_status SMALLINT,"
				+ "create_time DATETIME," + "finish_time DATETIME,"
				+ "manager_code VARCHAR(16)," + "manager_name VARCHAR(16),"
				+ "checker_code VARCHAR(16)," + "checker_name VARCHAR(16),"
				+ "desc TEXT," + "checkline_name VARCHAR(64),"
				+ "checkline_ip VARCHAR(32) )");

		// 检查项目表
		db.execSQL("CREATE TABLE check_item("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "exc_id VARCHAR(16),"
				+ "item_id VARCHAR(16),"
				+ "item_name VARCHAR(16),"
				+ "param_value VARCHAR(256),"
				+ "sum_times INT,"
				+ "check_status SMALLINT,check_desc TEXT,checker_name VARCHAR(16))");

		// 检查项目明细表
		db.execSQL("CREATE TABLE check_item_detail("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "exc_id VARCHAR(16)," + "item_id VARCHAR(16),"
				+ "param_value VARCHAR(256)," + "check_time DATETIME UNIQUE,"
				+ "checker_code VARCHAR(16)," + "checker_name VARCHAR(16),"
				+ "check_error VARCHAR(256)," + "check_status SMALLINT )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

	}
}
