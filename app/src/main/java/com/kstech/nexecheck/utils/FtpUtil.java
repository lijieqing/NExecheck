package com.kstech.nexecheck.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FtpUtil {

	private FTPClient ftp = null;
	/**
	 * Ftp服务器
	 */
	private String server;
	/**
	 * 用户名
	 */
	private String uname;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 连接端口，默认21
	 */
	private int port = 21;

	public FtpUtil(String server, int port, String uname, String password) throws Exception {
		this.server = server;
		if (this.port > 0) {
			this.port = port;
		}
		this.uname = uname;
		this.password = password;
		// 连接ftp服务器
		ftp = connectFTPServer();
	}

	/**
	 * 连接FTP服务器
	 * @param server
	 * @param uname
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private FTPClient connectFTPServer() throws Exception {
		try {
			ftp = new FTPClient();
			ftp.setControlEncoding("GBK");
			// ftp.setControlEncoding("UTF-8");

			ftp.configure(getFTPClientConfig());
			ftp.connect(this.server, this.port);
			if (!ftp.login(this.uname, this.password)) {
				ftp.logout();
				ftp = null;
				return ftp;
			}

			// 文件类型,默认是ASCII
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

			// 设置被动模式
			ftp.enterLocalPassiveMode();
			ftp.setConnectTimeout(10000);
			ftp.setBufferSize(1024);
			// 响应信息
			int replyCode = ftp.getReplyCode();
			if ((!FTPReply.isPositiveCompletion(replyCode))) {
				// 关闭Ftp连接
				closeFTPClient();
				// 释放空间
				ftp = null;
				throw new Exception("登录FTP服务器失败,请检查![Server:" + server + "、"
						+ "User:" + uname + "、" + "Password:" + password);
			} else {
				return ftp;
			}
		} catch (Exception e) {
			ftp.disconnect();
			ftp = null;
			throw e;
		}
	}

	/**
	 * 配置FTP连接参数
	 *
	 * @return
	 * @throws Exception
	 */
	private FTPClientConfig getFTPClientConfig() throws Exception {
		String systemKey = FTPClientConfig.SYST_UNIX;// FTPClientConfig.SYST_NT;
		String serverLanguageCode = "zh";
		FTPClientConfig conf = new FTPClientConfig(systemKey);
		conf.setServerLanguageCode(serverLanguageCode);
		conf.setDefaultDateFormatStr("yyyy-MM-dd");
		return conf;
	}

	/**
	 * 向FTP根目录上传文件
	 *
	 * @param localFile
	 * @param newName
	 *            新文件名
	 * @throws Exception
	 */
	public Boolean uploadFile(String localFile, String newName)
			throws Exception {
		InputStream input = null;
		boolean success = false;
		try {
			File file = null;
			if (checkFileExist(localFile)) {
				file = new File(localFile);
			}
			input = new FileInputStream(file);
			success = ftp.storeFile(newName, input);
			// 上传后，无论成功还是失败，删除sd卡中的临时文件
			file.delete();
			if (!success) {
				throw new Exception("文件上传失败!");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return success;
	}

	/**
	 * 向FTP根目录上传文件
	 *
	 * @param input
	 * @param newName
	 *            新文件名
	 * @throws Exception
	 */
	public Boolean uploadFile(InputStream input, String newName)
			throws Exception {
		boolean success = false;
		try {
			success = ftp.storeFile(newName, input);
			if (!success) {
				throw new Exception("文件上传失败!");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return success;
	}

	/**
	 * 向FTP指定路径上传文件
	 *
	 * @param localFile
	 * @param newName
	 *            新文件名
	 * @param remoteFoldPath
	 * @throws Exception
	 */
	public Boolean uploadFile(String localFile, String newName,
							  String remoteFoldPath) throws Exception {

		InputStream input = null;
		boolean success = false;
		try {
			File file = null;
			if (checkFileExist(localFile)) {
				file = new File(localFile);
			}
			input = new FileInputStream(file);

			// 改变当前路径到指定路径
			if (!this.changeDirectory(remoteFoldPath)) {
				System.out.println("服务器路径不存!");
				return false;
			}
			success = ftp.storeFile(newName, input);
			if (!success) {
				throw new Exception("文件上传失败!");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return success;
	}

	/**
	 * 向FTP指定路径上传文件
	 *
	 * @param input
	 * @param newName
	 *            新文件名
	 * @param remoteFoldPath
	 * @throws Exception
	 */
	public Boolean uploadFile(InputStream input, String newName,
							  String remoteFoldPath) throws Exception {
		boolean success = false;
		try {
			// 改变当前路径到指定路径
			if (!this.changeDirectory(remoteFoldPath)) {
				System.out.println("服务器路径不存!");
				return false;
			}
			success = ftp.storeFile(newName, input);
			if (!success) {
				throw new Exception("文件上传失败!");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return success;
	}

	/**
	 * 从FTP服务器下载文件
	 *
	 * @param remotePath
	 *            FTP路径(不包含文件名)
	 * @param fileName
	 *            下载文件名
	 * @param localPath
	 *            本地路径
	 */
	private Boolean downloadFile(String remotePath, String fileName,
								 String localPath) throws Exception {
		Date begin = new Date();
		BufferedOutputStream output = null;
		boolean success = false;
		try {
			File dir = new File(localPath);
			if (!dir.exists())
				dir.mkdirs();

			// 检查本地路径
			this.checkFileExist(localPath);
			// 改变工作路径
			if (!this.changeDirectory(remotePath)) {
				System.out.println("当前路径 " + ftp.printWorkingDirectory()
						+ " 服务器路径" + remotePath + "不存在");
				return false;
			}
			// 列出当前工作路径下的文件列表
			List<FTPFile> fileList = this.getFileList();
			if (fileList == null || fileList.size() == 0) {
				System.out.println("服务器当前路径下不存在文件！");
				return success;
			}

			File localFilePath = new File(localPath + File.separator + fileName);
			output = new BufferedOutputStream(new FileOutputStream(
					localFilePath));
			success = ftp.retrieveFile(fileName, output);
			if (!success) {
				System.err.println("文件下载失败:" + remotePath + " " + fileName);
			} else {
				System.out.println("下载成功 (耗时:"
						+ (System.currentTimeMillis() - begin.getTime())
						/ 1000d + " s)：" + remotePath + "/" + fileName + "-> "
						+ localPath + "/" + fileName);
			}

			/*
			 * for (FTPFile ftpfile : fileList) { String
			 * ftpFileName=ftpfile.getName(); if (ftpFileName.equals(fileName))
			 * { File localFilePath = new File(localPath + File.separator +
			 * ftpFileName); output = new BufferedOutputStream(new
			 * FileOutputStream( localFilePath)); success =
			 * ftp.retrieveFile(ftpFileName, output); if(!success){
			 * System.err.println("文件下载失败:"+remotePath+" "+fileName); } }else{
			 * System
			 * .err.println("ftpFileName.equals(fileName) 不匹配 "+ftpFileName
			 * +" "+fileName); } }
			 */
			/*
			 * if (!success) { throw new Exception("文件下载失败!"); }
			 */
		} catch (Exception e) {
			throw e;
		} finally {
			if (output != null) {
				output.close();
			}
		}
		return success;
	}

	/**
	 * 从FTP服务器获取文件流
	 *
	 * @param remoteFilePath
	 * @return
	 * @throws Exception
	 */
	private InputStream downloadFile(String remoteFilePath) throws Exception {

		return ftp.retrieveFileStream(remoteFilePath);
	}

	/**
	 * 获取FTP服务器上指定路径下的文件列表
	 *
	 * @param filePath
	 * @return
	 */
	/*
	 * private List<FTPFile> getFtpServerFileList(String remotePath) throws
	 * Exception {
	 *
	 *
	 * FTPListParseEngine engine = ftp.initiateListParsing(remotePath);
	 * List<FTPFile> ftpfiles = Arrays.asList(engine.getNext(25));
	 *
	 *
	 * return ftpfiles; }
	 */

	/**
	 * 获取FTP服务器上[指定路径]下的文件列表
	 *
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private List<FTPFile> getFileList(String remotePath) throws Exception {
		if (remotePath != null && !remotePath.endsWith("/")) {
			remotePath += "/";
		}
		List<FTPFile> ftpfiles = Arrays.asList(ftp.listFiles(remotePath,
				new FTPFileFilter() {
					@Override
					public boolean accept(FTPFile f) {
						/*
						 * if(remotePath2!=null &&
						 * remotePath2.equals(f.getName()) &&
						 * f.getType()==FTPFile.DIRECTORY_TYPE){ return false; }
						 */
						if (".".equals(f.getName()) || "..".equals(f.getName())) {
							return false;
						}
						return true;
					}
				}));

		return ftpfiles;
	}

	/**
	 * 获取FTP服务器[当前工作路径]下的文件列表
	 *
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private List<FTPFile> getFileList() throws Exception {

		List<FTPFile> ftpfiles = Arrays.asList(ftp.listFiles(null,
				new FTPFileFilter() {
					@Override
					public boolean accept(FTPFile f) {
						if (!".".equals(f.getName())
								&& !"..".equals(f.getName())) {
							return true;
						}
						return false;
					}
				}));

		return ftpfiles;
	}

	/**
	 * 改变FTP服务器工作路径
	 *
	 * @param remoteFoldPath
	 */
	private Boolean changeDirectory(String remoteFoldPath) throws Exception {

		return ftp.changeWorkingDirectory(remoteFoldPath);
	}

	/**
	 * 删除文件
	 *
	 * @param remoteFilePath
	 * @return
	 * @throws Exception
	 */
	private Boolean deleteFtpServerFile(String remoteFilePath) throws Exception {

		return ftp.deleteFile(remoteFilePath);
	}

	/**
	 * 创建目录
	 *
	 * @param remoteFoldPath
	 * @return
	 */
	private boolean createFold(String remoteFoldPath) throws Exception {

		boolean flag = ftp.makeDirectory(remoteFoldPath);
		if (!flag) {
			throw new Exception("创建目录失败");
		}
		return false;
	}

	/**
	 * 删除目录
	 *
	 * @param remoteFoldPath
	 * @return
	 * @throws Exception
	 */
	private boolean deleteFold(String remoteFoldPath) throws Exception {

		return ftp.removeDirectory(remoteFoldPath);
	}

	/**
	 * 删除目录以及文件
	 *
	 * @param remoteFoldPath
	 * @return
	 */
	private boolean deleteFoldAndsubFiles(String remoteFoldPath)
			throws Exception {

		boolean success = false;
		List<FTPFile> list = this.getFileList(remoteFoldPath);
		if (list == null || list.size() == 0) {
			return deleteFold(remoteFoldPath);
		}
		for (FTPFile ftpFile : list) {

			String name = ftpFile.getName();
			if (ftpFile.isDirectory()) {
				success = deleteFoldAndsubFiles(remoteFoldPath + "/" + name);
				if (!success)
					break;
			} else {
				success = deleteFtpServerFile(remoteFoldPath + "/" + name);
				if (!success)
					break;
			}
		}
		if (!success)
			return false;
		success = deleteFold(remoteFoldPath);
		return success;
	}

	/**
	 * 检查本地路径是否存在
	 *
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	private boolean checkFileExist(String filePath) throws Exception {
		boolean flag = false;
		File file = new File(filePath);
		if (!file.exists()) {
			throw new Exception("本地路径不存在,请检查!");
		} else {
			flag = true;
		}
		return flag;
	}

	/**
	 * 关闭FTP连接
	 *
	 * @param ftp
	 * @throws Exception
	 */
	private void closeFTPClient(FTPClient ftp) throws Exception {

		try {
			if (ftp.isConnected())
				ftp.logout();
			ftp.disconnect();
		} catch (Exception e) {
			throw new Exception("关闭FTP服务出错!");
		}
	}

	/**
	 * 关闭FTP连接
	 *
	 * @throws Exception
	 */
	public void closeFTPClient() throws Exception {
		try {
			ftp.logout();
			if (ftp.isConnected())
				ftp.disconnect();
		} catch (Exception e) {
			throw new Exception("关闭FTP服务出错!");
		}
	}

	/**
	 * Get Attribute Method
	 *
	 */
	private FTPClient getFtp() {
		return ftp;
	}

	private String getServer() {
		return server;
	}

	private String getUname() {
		return uname;
	}

	private String getPassword() {
		return password;
	}

	private int getPort() {
		return port;
	}

	/**
	 * Set Attribute Method
	 *
	 */
	private void setFtp(FTPClient ftp) {
		this.ftp = ftp;
	}

	private void setServer(String server) {
		this.server = server;
	}

	private void setUname(String uname) {
		this.uname = uname;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	private void setPort(int port) {
		this.port = port;
	}

	private int synFtpFolder(String remotePath, String localPath)
			throws Exception {
		if (remotePath == null) {
			remotePath = "/";
		} else if (!remotePath.startsWith("/")) {
			remotePath = "/" + remotePath;
		}

		List<FTPFile> lst = getFileList(remotePath);
		int fileCount = 0;
		System.out.println("同步文件夹：" + remotePath + " " + lst.size());
		for (int i = 0; i < lst.size(); i++) {
			FTPFile f = lst.get(i);
			if (f.getType() == FTPFile.DIRECTORY_TYPE) {
				fileCount += synFtpFolder((remotePath == "/" ? "" : remotePath)
						+ "/" + f.getName(), localPath + "/" + f.getName());
			} else if (f.getType() == FTPFile.FILE_TYPE) {
				System.out.println("同步文件:" + remotePath + "/" + f.getName()
						+ "  ");
				File dir = new File(localPath);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				downloadFile(remotePath, f.getName(), localPath);
				fileCount++;
			} else {
				System.out.println("ftp文件 特殊类型" + f.getType());
			}
		}
		return fileCount;
	}

	private List<FTPFile> getAllFiles(String remotePath) throws Exception{
		Date begin=new Date();
		if(remotePath==null){
			remotePath="/";
		}else if(!remotePath.startsWith("/")){
			remotePath="/"+remotePath;
		}
		List<FTPFile> files=new ArrayList<FTPFile>();
		List<FTPFile> lst=getFileList(remotePath);
		for(int i=0;i<lst.size();i++){
			FTPFile f=lst.get(i);
			if(f.getType()== FTPFile.DIRECTORY_TYPE){
				files.addAll(getAllFiles((remotePath=="/"?"":remotePath)+"/"+f.getName()));
			}else if(f.getType()== FTPFile.FILE_TYPE){
				files.add(f);
			}else{
				System.out.println("ftp文件 特殊类型"+f.getType());
			}
		}
		System.out.println("遍历"+remotePath+" 耗时 "+(System.currentTimeMillis()-begin.getTime())/1000d+" s");
		return files;
	}

	/**
	 * 主方法(测试)
	 *
	 * 问题：上传时命名的新文件名不能有中文，否则上传失败.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * try { FtpHelper fu = new FtpHelper("192.168.2.18", 21,
		 * "administrator","sunshine"); fu.connectFTPServer(); Element
		 * fatherElement = fu.getCurrentElement(); fu.createDirectoryXML("/",
		 * fatherElement); fu.saveXML(); } catch (Exception e) {
		 * System.out.println("异常信息：" + e.getMessage()); }
		 */

		FtpUtil fu = new FtpUtil("192.168.1.137", 88, "HoPacs", "cnhis");
		// FtpHelper fu = new FtpHelper("192.168.1.213", 88, "martin","martin");
		fu.connectFTPServer();

		// fu.do"ftp://HoPacs:cnhis@192.168.1.137:88/RisImgDir/2016-06-21/US0000000001/US0000000001.jpg"

		/*
		 * fu.changeDirectory("RisImgDir/2013-11-27/US0000000013");
		 *
		 * List<FTPFile> lst=fu.getFileList(); System.out.println(lst.size());
		 * for(int i=0;i<lst.size();i++){ FTPFile f=lst.get(i);
		 *
		 * System.out.println("类型:"+f.getType()+"  "+f);
		 * if(f.getType()==FTPFile.DIRECTORY_TYPE){
		 *
		 * }else if(f.getType()==FTPFile.FILE_TYPE){
		 * fu.downloadFile("/RisImgDir/2013-11-27/US0000000013", f.getName(),
		 * "D:/workspace/testFTP/ftp"); }else{
		 * System.out.println("ftp文件 特殊类型"+f.getType()); } }
		 */

		// 同步文件夹
		/*
		 * int count=fu.synFtpFolder("/2015-08-18","D:/workspace/testFTP/ftp");
		 * System.out.println("文件同步总数量"+count);
		 */

		// 遍历根目录
		List<FTPFile> allFiles = fu.getAllFiles(null);
		for (FTPFile f : allFiles) {
			System.out.println(f.getName()
					+ " "
					+ (f.getTimestamp() == null ? null : f.getTimestamp()
					.getTime().toLocaleString()));
		}

		// fu.downloadFile("/test","《技术服务部制度》修订版V1.4.pdf","D:/workspace/testFTP/ftp/localftp")
		// ;

		// fu.downloadFile("/21877","《技术服务部制度》修订版V1.4.pdf","D:/workspace/testFTP/ftp")
		// ;

		/*
		 * for(FTPFile f: fu.getFileList("/21877")){
		 * System.out.println(f.getName()); }
		 */

		/*
		 * fu.changeDirectory("/2012-07-11/US0000000009");
		 * System.out.println(fu.getFileList("/").size());
		 */

		/*
		 * fu.changeDirectory("/2012-07-11/US0000000009"); boolean
		 * b=fu.changeDirectory("/2012-07-11/US0000000009");
		 * System.out.println("存在目录? "+b);
		 */

		fu.closeFTPClient();
	}
}
