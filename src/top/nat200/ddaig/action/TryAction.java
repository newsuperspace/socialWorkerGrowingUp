package top.nat200.ddaig.action;

import java.util.ArrayList;
import java.util.List;

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


	// ====================Actions==================
	
	public String tryRun(){
		this.setName("我是一个测试Action，名字叫TryAction222");
		return SUCCESS;
	}
	
	
	public String getEventSources(){
		Json4FullCalendar  json  =  null;
		List<Json4FullCalendar> list  =  new ArrayList<Json4FullCalendar>();
		for(int i=0;i<5;i++){
			json = new Json4FullCalendar();
			json.setAllDay(false);
			json.setBackgroundColor("yellow");
			json.setEnd("2018-08-20T11:30:00");
			json.setId(""+i);
			json.setStart("2018-08-20T10:00:00");
			json.setTextColor("black");
			json.setTitle("桌游活动"+i);
			json.setUrl("http://www.baidu.com");
			list.add(json);
		}
		
		ActionContext.getContext().getValueStack().push(list);
		return "json";
	}
	
	
	
	
	
}
