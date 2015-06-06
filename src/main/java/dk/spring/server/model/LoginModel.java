package dk.spring.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginModel {

	
	private String email;
	private String password;
	
	
	public LoginModel(){
		super();
	}
	
	
	@JsonCreator
	public LoginModel(@JsonProperty("email") String email, 
			@JsonProperty("password")String password ) {
		super();
		this.email = email;
		this.password = password;
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
	
	
	
	
	
}
