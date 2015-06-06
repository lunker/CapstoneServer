package dk.spring.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewModel {
	
	// review id
	@JsonIgnore
	private String id;
	// review content
	// written date
	private String date;
	private String userId;
	private String placeId;
	private String code;
	private String rating ;
	
	
	public ReviewModel(){
		
	}
	
	@JsonCreator
	public ReviewModel(
			@JsonProperty("id")String id, 
			@JsonProperty("date")String date, 
			@JsonProperty("userid")String userId, 
			@JsonProperty("placeid")String placeId, 
			@JsonProperty("code")String code, 
			@JsonProperty("ratings") String rating) {
		super();
		
		this.id = id;
		this.date = date;
		this.userId = userId;
		this.placeId = placeId;
		this.code = code;
		this.rating = rating;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPlaceId() {
		return placeId;
	}
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
	
	
	
	
}
