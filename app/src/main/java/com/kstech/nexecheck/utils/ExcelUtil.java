package com.kstech.nexecheck.utils;

import android.os.Environment;
import android.util.Log;

import com.kstech.nexecheck.domain.config.vo.CheckItemParamValueVO;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemDetailStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckItemDetailEntity;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.domain.excel.CellTemplate;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.JsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelUtil {
	//内存地址
	public static String root = Environment.getExternalStorageDirectory()
			.getPath();

	/**
	 * 根据模板，生成excel文件
	 * @return
	 */
	public static Map<String, String> UpdateExcelByTemplate(List<CheckRecordEntity> uploadData) throws Exception {
		WritableFont font = new WritableFont(WritableFont.TIMES, 11,
				WritableFont.BOLD);// 定义字体
		WritableCellFormat format = new WritableCellFormat(font);
		format.setAlignment(jxl.format.Alignment.CENTRE);// 左对齐
		format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
		format.setBorder(Border.ALL, BorderLineStyle.THIN,Colour.BLACK);// 黑色边框
		format.setWrap(true);//自动换行

		Map<String, String> filePathMap = new HashMap<String, String>();
		for (CheckRecordEntity record:uploadData) {

			StringBuffer desc = new StringBuffer();
			//检验状态：1：合格2：未合格 3：强制合格
			String wholedesc = getwholestatue(record.getCheckStatus())+" 描述："+record.getDesc();
			int descNum = 0;
			// 解析，放入到map中
			Map<String,List<CheckItemDetailEntity>> itemDetailMap = new HashMap<String, List<CheckItemDetailEntity>>();
			List<CheckItemEntity> checkItemList = record.getCheckItemList();
			for (CheckItemEntity item:checkItemList){
				List<CheckItemDetailEntity> detailList = item.getCheckItemDetailList();
				itemDetailMap.put(item.getItemId(), detailList);
				String s = item.getCheckDesc();
				if(s != null){
					if(s.contains("ignore$")){
						descNum++;
						desc.append("项目"+descNum+" "+item.getItemName()+": "+s.replace("ignore$","")+"。\n");
					}
				}
			}

			String modelname = "";

			if("0005".equals(record.getDeviceId())){
				modelname = "15se";
			}else {
				modelname = "normal";
			}

			// 读取模板
			//Workbook mWorkbook = Workbook.getWorkbook(new File("/storage/sdcard1/Models/repTemplate/"+record.getExcId()+".xls"));
			Workbook mWorkbook = Workbook.getWorkbook(new File("/storage/sdcard1/Models/repTemplate/"+ modelname+".xls"));
			Sheet mSheet = mWorkbook.getSheet(0);
			int sRow = mSheet.getRows();
			int sColumns = mSheet.getColumns();
			Log.i("W","Total sRow: " + sRow + ", Total sColumns: " + sColumns);
			String fileName = record.getExcId() +"-"+ DateUtil.getDateTimeFormat14(new Date());
			// 根据模板创建新excel，填充模板内容
			WritableWorkbook mWritableWorkbook = Workbook.createWorkbook(new File("/storage/sdcard1/"+fileName+".xls"), mWorkbook);
			WritableSheet mWritableSheet = mWritableWorkbook.getSheet(0);
			int dRow = mSheet.getRows();
			int dColumns = mSheet.getColumns();
			Log.i("EXCEL","Total dRow: " + dRow + ", Total dColumns: " + dColumns);
			for(int i= 0 ; i < dRow ; i ++){
				// 获取单元格行
				int cols = mWritableSheet.getRow(i).length;
				for(int j = 0 ; j < cols ; j ++){
					// 获取单元格列
					Cell temp = mWritableSheet.getCell(j, i);
					String content = temp.getContents();
					if(content.contains("{")){
						try {
							// 如果此区域为json内容，则为模板配置，需要填充
							CellTemplate cellTemplate = (CellTemplate) JsonUtils.fromJson(content, CellTemplate.class);
							String dataType = cellTemplate.getDataType();
							if (dataType.equals("detail")) {
								boolean ifHaveValue = false;
								// 根据itemid 获得对应的详情记录
								List<CheckItemDetailEntity> list = itemDetailMap.get(cellTemplate.getItemId());
								if(list != null){
									// 若还未开始检测
									if (list.size() > (cellTemplate.getTimes()-1)) {
										// 根据次数，获取详情中倒数第几次
										CheckItemDetailEntity checkItemDetailEntity = list.get(cellTemplate.getTimes()-1);
											// 取到某一次检测的参数值
										List<CheckItemParamValueVO> param = checkItemDetailEntity.getParam();
										Log.e("√√","!!!!DETAIL"+"param===="+cellTemplate.getParamName()+"--times--"+cellTemplate.getTimes());
										for (CheckItemParamValueVO p:param) {
											// 遍历参数名称，获得对应的值
											if (p.getParam().equals(cellTemplate.getParamName())){
												((Label)temp).setString((p.getValue()==null||p.getValue().equals(""))?"-":p.getValue());
												ifHaveValue = true;
												break;
											}
										}
									}else {
										ifHaveValue = false;
									}
									if (!ifHaveValue) {
										((Label)temp).setString("-");
									}
								}else {
									((Label)temp).setString("-");
								}
							} else if (dataType.equals("dateTime")) {
								((Label)temp).setString(record.getFinishTime()==null?"检验日期：未完成":"检验日期："+record.getFinishTime());
							} else if (dataType.equals("item")) {
								((Label)temp).setString("编号："+record.getExcId());
							}else if(dataType.equals("desc")){
								temp.getCellFormat();
								((Label)temp).setString(desc.toString());

							}else if(dataType.equals("wholedesc")){
								((Label)temp).setString(wholedesc);
							} else if (dataType.equals("checkerName")) {
								List<String> checkerNameList = new ArrayList<String>();
								// 获取所有项目最后几次的检验员
								for (List<CheckItemDetailEntity> detailList:itemDetailMap.values()){
									for (int k = 0;k < detailList.size() ;k++) {
										if (!checkerNameList.contains(detailList.get(k).getCheckerName())){
											checkerNameList.add(detailList.get(k).getCheckerName());
										}
									}
								}
								StringBuffer sb = new StringBuffer();
								sb.append("检验员：");
								for (String name:checkerNameList){
									sb.append(" " + name);
								}
								((Label)temp).setString(sb.toString());
							} else if (dataType.equals("totalTimes")) {
								// 根据itemid 获得对应的详情记录
								List<CheckItemDetailEntity> list = itemDetailMap.get(cellTemplate.getItemId());
								boolean ifPass = true;
								if(list != null){
									Log.e("√√","TRUE"+"id===="+cellTemplate.getItemId()+"--celltimes=="+cellTemplate.getTotalTimes()+"==list size="+list.size());
									if (list.size() >= cellTemplate.getTotalTimes()) {
										// 获取最近几次的检测结果，进行判定，是否合格
										for (int m = 0; m < cellTemplate.getTotalTimes(); m++){
											CheckItemDetailEntity checkItemDetailEntity = list.get(m);
											if (CheckItemDetailStatusEnum.PASS.getCode().equals(checkItemDetailEntity.getCheckStatus())){
												ifPass = true;
												break;
											}else {
												ifPass = false;
												break;
											}
										}
									} else {
										ifPass = false;
									}
									if (ifPass) {
										((Label)temp).setString("√");
									} else {
										((Label)temp).setString("×");
									}
								}else {
									((Label)temp).setString("-");
								}
							}
							((Label)temp).setCellFormat(format);
						} catch (Exception e) {
							// 非模板配置，不需要填充
							System.out.println("生成模版异常"+e.toString());
						}
					}
				}
			}

			// 关闭资源流
			mWritableWorkbook.write();
			mWritableWorkbook.close();
			mWorkbook.close();
			filePathMap.put(fileName+".xls","/storage/sdcard1/"+fileName+".xls");
		}
		return filePathMap;
	}

	private static String getwholestatue(String s) {
		String re = null;
		if(s.equals("1")){
			re = "合格";
		}
		if(s.equals("2")){
			re = "未合格";
		}
		if(s.equals("3")){
			re = "强制合格";
		}
		return re;
	}
}
