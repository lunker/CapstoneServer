package dk.spring.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/***
 * 
 * @author Lee Dong Kyoo
 *
 */
public class CourseModel {
	
	
	private String courseId;
	private String placeIds;
	private String userId;
	
	public CourseModel(){
		;
	}
	
	@JsonCreator
	public CourseModel(
			@JsonProperty(value="userid") String userId,
			@JsonProperty(value="courseid") String courseId, 
			@JsonProperty(value="placeids") String placeIds
			) {
		
		super();
		this.courseId = courseId;
		this.placeIds = placeIds;
		this.userId = userId;
	}
	
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getPlaceIds() {
		return placeIds;
	}
	public void setPlaceIds(String placeIds) {
		this.placeIds = placeIds;
	}
	
	
	

}
