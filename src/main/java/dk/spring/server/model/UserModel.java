package dk.spring.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/***
 * 
 * @author Lee Dong Kyoo
 *
 */
public class UserModel {

	
	private String email;
	private String password;
	private String gender;
	private String preferCategory;
	
	
//	@JsonIgnore
//	private String[] reviewItem; 
	

	public UserModel() {
		super();
	}
	
	@JsonCreator
	public UserModel(
			
			@JsonProperty("email") String email, 
			@JsonProperty("password")String password, 
			@JsonProperty("gender")String gender,
			@JsonProperty("prefercategory") String preferCategory
			
			) {
		super();
		this.email = email;
		this.password = password;
		this.gender = gender;
		this.preferCategory = preferCategory;
//		this.reviewItem = reviewItem;
	}


	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPreferCategory() {
		return preferCategory;
	}

	public void setPreferCategory(String preferCategory) {
		this.preferCategory = preferCategory;
	}
	
	
	/*
	public String[] getReviewItem() {
		return reviewItem;
	}
	public void setReviewItem(String[] reviewItem) {
		this.reviewItem = reviewItem;
	}
	
	*/
	
}
