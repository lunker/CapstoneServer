package dk.spring.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCursor;

import dk.spring.server.Constant;
import dk.spring.server.factory.DBFactory;

public class GoogleNetwork extends Thread{

	
	private ObjectMapper mapper = new ObjectMapper();
	private DatabaseConnector connector = DBFactory.getConnector();
	
	private String GOOGLE_API_KEY = "AIzaSyASqm0Ry6_-ExEM6rcIY4wbYgFxsR9E0Zs";
	private String GOOGLE_PLACE_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";
	private String GOOGLE_PLACE_DETAIL_URL ="https://maps.googleapis.com/maps/api/place/details/";
	
	private String title="";
	private String longitude="";
	private String latitude="";
	private String myPlaceId="";
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		// get place title, location from database
		String collection ="";
		for(int i=0; i<5;i++){
			switch(i){
				case 0:
					collection = Constant.DAUM_CODE_CAFE;
					break;
				case 1:
					collection = Constant.DAUM_CODE_CULTURE;
					break;
				case 2:
					collection = Constant.DAUM_CODE_FOOD;
					break;
				case 3:
					collection = Constant.DAUM_CODE_REST;
					break;
				case 4:
					collection = Constant.DAUM_CODE_TOUR;
					break;
			}
		}
		collection=connector.codeToCollection(collection);
		Document tmpDoc = null;
		
		MongoCursor<Document> resultCurosr = connector.getMyCollection(collection).find().iterator();
		while(resultCurosr.hasNext()){
			
			tmpDoc = resultCurosr.next();
			title = (String)tmpDoc.get("title");
			longitude = (String) tmpDoc.get("longitude");
			latitude = (String) tmpDoc.get("latitude");
			myPlaceId = (String) tmpDoc.get("id");
			getGooglePlaceID(myPlaceId, collection, title, latitude, longitude);
		}
		
		// call google api
	}
	
	public boolean getGooglePlaceID(String myPlaceId, String code, String title, String latitude, String longitude){
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(GOOGLE_PLACE_SEARCH_URL+"json?"
				+"location="
				+latitude+","
				+longitude
				+"&radius="+1000
				+"&key="+GOOGLE_API_KEY);
		
		String result = "";
		
		try {
			HttpResponse response = client.execute(get);
			result = EntityUtils.toString(response.getEntity());
			JsonNode resultNode = mapper.readTree(result).get("results");
			
			if(resultNode.isArray()){
				JsonNode tmp = resultNode.get(0);
					
				// SAVE PLACES 
				String googlePlaceId = tmp.get("place_id").asText();
				String googleTitle = tmp.get("name").asText();
				
				if(googleTitle.equals(title)){
					// 평점 가져온다!
					System.out.println("[GOOGLE] find same place ! ");
					getGoogleRate(myPlaceId, code, googlePlaceId);
				}
				else{
					connector.savePlaceRate(myPlaceId, "2.5", code);
					System.out.println("[GOOGLE] getGoogleRate(), save the rating in DB");
				}
			}
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			System.out.println(result);
			e.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			System.out.println(result);
			e2.printStackTrace();
		} catch (Exception e3){
			System.out.println(result);
			e3.printStackTrace();
		}
		return false;
	}
	
	public void getGoogleRate(String myPlaceId, String code, String googlePlaceId){
		System.out.println("[GOOGLE] getGoogleRate()");
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(GOOGLE_PLACE_DETAIL_URL+"json?"
				+"placeid="
				+googlePlaceId
				+"&key="+GOOGLE_API_KEY);
		String result = "";
		
		HttpResponse response;
		try {
			response = client.execute(get);
			result = EntityUtils.toString(response.getEntity());
			JsonNode resultNode = mapper.readTree(result).get("result");
			String rating = ""; 
			rating = resultNode.get("rating").asText();
			if(rating.equals("")){
				// no rating
				rating = "2.5";
			}

			connector.savePlaceRate(myPlaceId, rating, code);
			System.out.println("[GOOGLE] getGoogleRate(), save the rating in DB");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
