package com.kstech.nexecheck.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SystemUtil {
	// 此方法只是关闭软键盘
	public static void hideSoftKeyBoard(Activity ac) {
		InputMethodManager imm = (InputMethodManager) ac
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive() && ac.getCurrentFocus() != null) {
			if (ac.getCurrentFocus().getWindowToken() != null) {
				imm.hideSoftInputFromWindow(ac.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	public static boolean copyDB(String local,Context context) {
		File target = new File("/data/data/com.kstech.nexecheck/databases/newBee");
		File f = new File("/storage/sdcard1/DB/"+local);
		if(f.exists()){
			try {
				FileChannel outF = new FileOutputStream(target).getChannel();
				FileChannel inF = new FileInputStream(f).getChannel();
				inF.transferTo(0,f.length(),outF);
				inF.close();
				outF.close();
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}else {
			Toast.makeText(context,"数据库导入失败，请重试",Toast.LENGTH_SHORT).show();
			return false;
		}
	}
}
