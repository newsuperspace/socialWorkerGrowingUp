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

	// ===================属性驱动==================
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// 提供给结果相应页面显示处理结果
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	// 前端文件上传的文件（前端负责上传文件的input的name=“file”）
	private File file;
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}

	// ====================Actions==================
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

	public String upload() {

		System.out.println("name=" + this.name);
		String target = ServletActionContext.getServletContext().getRealPath("/upload/" + name);
		// 获得上传的文件
		File targetFile = new File(target);
		// 通过struts2提供的FileUtils类拷贝
		try {
			FileUtils.copyFile(file, targetFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// return null 表示不跳转到任何JSP页面，也不返回JSON。 所有处理结果都已HttpResponse响应正文中的内容为返回结果
		return null;
	}

}
