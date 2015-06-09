package dk.spring.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveCategoryModel {

	
	
	private String prefercategory;
	private String userid;
	
	
	
	public SaveCategoryModel(){
		
	}
	
	
	@JsonCreator
	public SaveCategoryModel(
			
			@JsonProperty(value="prefercategory")String prefercategory, 
			@JsonProperty(value="userid")String userid) {
		super();
		this.prefercategory = prefercategory;
		this.userid = userid;
	}
	public String getPrefercategory() {
		return prefercategory;
	}
	public void setPrefercategory(String prefercategory) {
		this.prefercategory = prefercategory;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	
	
	
}
