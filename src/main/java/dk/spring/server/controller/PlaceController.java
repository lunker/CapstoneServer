package dk.spring.server.controller;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.catalina.connector.Request;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Sorts;

import dk.spring.server.factory.DBFactory;
import dk.spring.server.factory.MapperFactory;
import dk.spring.server.model.ReviewModel;
import dk.spring.util.DatabaseConnector;

@RestController
public class PlaceController {

	private DatabaseConnector connector = DBFactory.getConnector();
	private ObjectMapper mapper = MapperFactory.getMapper();

	@RequestMapping(value = "/place", method = RequestMethod.GET)
	public String getPlace(String code, String latitude, String longitude) {

		String placeInfo = "";
		placeInfo = connector.getPlace(code, latitude, longitude);
		if (placeInfo == null)
			return "0";
		else
			return placeInfo;
	}
	
	

	@RequestMapping(value = "/review", method = RequestMethod.GET)
	public String getReviewInPlace(@RequestParam(value="placeid")String placeId) {

		String reviewInfo = "";

		reviewInfo = connector.getReview(placeId);

		if (reviewInfo.equals(""))
			return "0";
		else
			return reviewInfo;
	}

	@RequestMapping(value = "/savereview", method = RequestMethod.POST)
	public String saveReview(@RequestBody ReviewModel review) {

		String result = connector.saveReview(review);
		
		return result;
	}
	
	@RequestMapping(value="getreview", method=RequestMethod.GET)
	public String getReview( 
			@RequestParam(value="userid", defaultValue="1")String userId,
			@RequestParam(value="placeid", defaultValue="1")String placeId
			){
		
		Document review = connector.getMyCollection(userId).find(new Document("placeid", placeId)).first();
		
		// 유저가 해당 장소에 평가를 하지 않았음 
		if(review == null){
			return "0";
		}
		
		/*
		 * 에러..ㅠㅠ 
		 */
		double userRating = review.getDouble("ratings");
		double totalRating = connector.getMyCollection( 
				connector.codeToCollection(review.getString("code"))).find(new Document("id",placeId)).first().getDouble("ratings");
		String result = "";
		result+=userRating+","+totalRating;
		
		return result;
	}
	
	
}
