package top.nat200.ddaig.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import top.nat200.ddaig.json.Json4FullCalendar;

@Component("tryAction")
@Scope("prototype")
public class TryAction extends ActionSupport {

	// ===================【属性驱动】==================
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * 保存通过weui.js上传上来的图片的图片名称,是通过onBeforeSend() 自定义的一个请求参数
	 */
	private String fileName;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	// 提供给结果相应页面显示处理结果
	private String result;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}

	/*
	 *  weui.uploader()的option中的fileVal字段所标注的上传文件的请求参数名
	 */
	private File file;
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * weui.js自带
	 * 形如： image/jpeg   的上传file类型
	 */
	private String fileContentType;
	
	/**
	 * weui.js自带
	 * 形如：1.jpg
	 */
	private String fileFileName;
	
	/**
	 * weui.js 自带
	 * 
	 */
	private String lastModifiedDate;
	
	/**
	 * weui.js 自带
	 * 上传file的字节数
	 */
	private int size;
	
	/**
	 * 与fileContentType相同
	 */
	private String type;
	
	public String getFileContentType() {
		return fileContentType;
	}
	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}
	public String getFileFileName() {
		return fileFileName;
	}
	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		if("undefined".equals(lastModifiedDate)){
			this.lastModifiedDate = "0";
		}else{
			this.lastModifiedDate = lastModifiedDate;
		}
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	// ====================【Actions】==================
	public String tryRun() {
		this.setName("我是一个测试Action，名字叫TryAction222");
		return SUCCESS;
	}

	/**
	 * 测试Fullcalendar时通过AJAX获取数据源的测试方法
	 * 
	 * @return
	 */
	public String getEventSources() {
		Json4FullCalendar json = null;
		List<Json4FullCalendar> list = new ArrayList<Json4FullCalendar>();
		for (int i = 0; i < 5; i++) {
			json = new Json4FullCalendar();
			json.setAllDay(false);
			json.setBackgroundColor("yellow");
			json.setEnd("2018-08-20T11:30:00");
			json.setId("" + i);
			json.setStart("2018-08-20T10:00:00");
			json.setTextColor("black");
			json.setTitle("桌游活动" + i);
			json.setUrl("http://www.baidu.com");
			list.add(json);
		}

		ActionContext.getContext().getValueStack().push(list);
		return "json";
	}

	/**
	 * 响应从weui.js上传上来的图片的AJAX请求
	 * 
	 * @return
	 */
	public String upload() {

		System.out.println("fileName=" + this.fileName);
		Result4Ajax  r  =  new  Result4Ajax();
		
		// 检查用于存放上传图片的/upload路径是否存在，不存在则创建出来
		String dir  =  ServletActionContext.getServletContext().getRealPath("/upload");
		// 这里的targetFile是一个路径不是文件
		File targetFile = new File(dir);
		if(!targetFile.exists()){
			targetFile.mkdirs();
		}
		// 正式创建用来存放上传图片的路径和file
		String target = ServletActionContext.getServletContext().getRealPath("/upload/" + this.fileName);
		// 而这里的targetFile才是真正的文件
		targetFile  =  new  File(target);
		// 通过struts2提供的FileUtils类拷贝
		try {
			FileUtils.copyFile(file, targetFile);
		} catch (IOException e) {
			e.printStackTrace();
			r.setMessage("上传图片失败");
			r.setResult(false);
			
			ActionContext.getContext().getValueStack().push(r);
			return "json";
		}

		r.setMessage("上传图片成功！");
		r.setResult(true);
		
		ActionContext.getContext().getValueStack().push(r);
		return "json";
	}

	
	
	// ==========================【内部类】用于AJAX响应的domain======================
	
	/**
	 * 内部类必须也要加上public才能被struts-json.jar插件在组装JSON响应的时候被侦测到
	 * @author Administrator
	 *
	 */
	public class Result4Ajax {
		private String message;
		private boolean result;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public boolean isResult() {
			return result;
		}

		public void setResult(boolean result) {
			this.result = result;
		}
	}

}
