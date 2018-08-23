package top.nat200.ddaig.domain;

import java.io.Serializable;

/**
 * 这是一个Hibernate的持久化状态对象，对应socialworker数据库中的一个名叫first的数据库表
 * @author Administrator
 *
 */
public class First implements Serializable {

	private String id;
	private String name;
	private int age;
	private String sex;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}

}
