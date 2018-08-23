package top.nat200.ddaig.action;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

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


	public String tryRun(){
		this.setName("我是一个测试Action，名字叫TryAction222");
		return SUCCESS;
	}
}
