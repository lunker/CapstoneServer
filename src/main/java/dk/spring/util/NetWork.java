package dk.spring.util;

import java.io.IOException;

import org.apache.catalina.connector.Connector;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dk.spring.server.factory.DBFactory;

public class NetWork extends Thread{

	
	private String TOUR_END_POINT = "http://api.visitkorea.or.kr/openapi/service";
	private String TOUR_API_KEY = "olfw6UH4MTaVEUgy6LeSxRPuJ%2BBZzpgmX6DaLPV5NslIhRJIY%2FP%2F3zqGOtY9K7LJmkCZyD8DLB4GXTAFdKfOmg%3D%3D";
	private String IP = "";
	
	/*
	 * NAVER API 
	 */
	private String NAVER_API_KEY = "c1b406b32dbbbbeee5f2a36ddc14067f";
	private String NAVER_URL = "http://openapi.naver.com/search";
	
	
	/*
	 * 다음 
	 */
	private String DAUM_API_KEY = "40848833e3eb2542a1a0d42a9d78e896";
	private String DAUM_URL = "https://apis.daum.net/local/v1/search/category.json?apikey=";
	
	private String DAUM_CODE_FOOD = "FD6";
	private String DAUM_CODE_CAFE = "CE7";
	private String DAUM_CODE_REST = "AD5";
	private String DAUM_CODE_TOUR = "AT4";
	private String DAUM_CODE_CULTURE="CT1";
	private int DAUM_CODE_TOTAL = 5;
	/*
	 * 구글 
	 */
	
	private ObjectMapper mapper = new ObjectMapper();

	
	private DatabaseConnector connector = null;
	
	// West 37.553125, 126.818575
	// East 37.552581, 127.139239
		
	// South 37.468701, 127.014269
	// North 37.681484, 127.006029
	
	///////////////////////////////////////
	// south->north : 37.46: 두번째 +0.01 
	// WEST -> east : 126.81 + 0.01
	
	private double latitude = 0;
	private double longitude = 0;
	private String code = "";
	
	private int page = 1;
	private int radius = 0;// 반경
	
	private int radius_one = 2000;
	private int radius_two = 10000;
	private int radius_three = 15000;
	private int radius_four = 20000;
	private int RADIUS_TOTAL = 1;
	private boolean flag = true;
	
	// OK 
//	http://maps.googleapis.com/maps/api/geocode/json?sensor=false&language=ko&address=서울시+용산
	
	// URLString encodeResult = URLEncoder.encode(String encodingString, String charsetName);
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		connector = DBFactory.getConnector();
		System.out.println("run the thread . . . .");
		
		
		//long증가 -> 동
		// lat 증가 -> 북으로 
		
		// 현재 37.756207,126.927728 까지 됐음.
		//37.236848, 126.969955
		latitude = 37.236848;
		longitude = 126.969955;
		// south to north 
		for(int i = 0; i<12; i++){
			
			// move 2km 
			latitude += 0.008;
			// west to east
			for(int s=0; s<12; s++){
				longitude+=0.008;
				getAll();
			}
			longitude=126.969955;
		}
		
	}// end method
	
	public void getAll(){
		for(int index = 0 ;  index < DAUM_CODE_TOTAL ; index++){
			switch(index){
				case 0: code = DAUM_CODE_FOOD;
				System.out.println("[code] - food");
				for(int i = 0 ; i < RADIUS_TOTAL; i++){
					switch(i){
						case 0 : radius = radius_one; break;
//						case 1 : radius = radius_two; break;
//						case 2 : radius = radius_three; break;
//						case 3 : radius = radius_four; break;
					}
					getPlaces();
				}// end for 
				
				break;
				case 1: code = DAUM_CODE_REST; 
				System.out.println("[code] - rest");
					for(int i = 0 ; i < RADIUS_TOTAL; i++){
						switch(i){
							case 0 : radius = radius_one; break;
//							case 1 : radius = radius_two; break;
//							case 2 : radius = radius_three; break;
//							case 3 : radius = radius_four; break;
						}
						getPlaces();
					}// end for 
					break;
				case 2: code = DAUM_CODE_CAFE; 
				System.out.println("[code] - cafe");
					for(int i = 0 ; i < RADIUS_TOTAL; i++){
						switch(i){
							case 0 : radius = radius_one; break;
//							case 1 : radius = radius_two; break;
//							case 2 : radius = radius_three; break;
//							case 3 : radius = radius_four; break;
						}
						getPlaces();
					}// end for 	
				break;
				
				case 3 : code = DAUM_CODE_TOUR;
				System.out.println("[code] - tour");
				for(int i = 0 ; i < RADIUS_TOTAL; i++){
					switch(i){
						case 0 : radius = radius_one; break;
//						case 1 : radius = radius_two; break;
//						case 2 : radius = radius_three; break;
//						case 3 : radius = radius_four; break;
					}
					getPlaces();
				}// end for 	
				break;
				
				case 4 : code = DAUM_CODE_CULTURE;
				System.out.println("[code] - culture");
				for(int i = 0 ; i < RADIUS_TOTAL; i++){
					switch(i){
						case 0 : radius = radius_one; break;
//						case 1 : radius = radius_two; break;
//						case 2 : radius = radius_three; break;
//						case 3 : radius = radius_four; break;
					}
					getPlaces();
				}// end for 	
				break;
			}
		}
	}
	
	public void getPlaces(){
		System.out.println("[radius] " + radius);
		flag = true;
		page = 1;
		
		while(flag){
			flag = requestGet(makeRequesURL(latitude, longitude, radius,page, code));
			page++;
		}
	}
	
	public String makeRequesURL(double latitude, double longitude, int radius, String code){
		return makeRequesURL(latitude, longitude, radius, 1, code);
	}
	
	public String makeRequesURL(double latitude, double longitude, int radius, int page, String code){
		StringBuilder builder = new StringBuilder();
		builder.append(DAUM_URL);
		builder.append(DAUM_API_KEY);
		builder.append("&code=");
		builder.append(code);
		builder.append("&location=");
		builder.append(latitude);
		builder.append(",");
		builder.append(longitude);
		builder.append("&radius=");
		builder.append(radius);
		builder.append("&page=");
		builder.append(page);
		
		return builder.toString();
	}
	
	public boolean requestGet(String URL){
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(URL);
		String result = "";
		try {
			HttpResponse response = client.execute(get);
			result = EntityUtils.toString(response.getEntity());
			JsonNode resultNode = mapper.readTree(result).get("channel").get("item");
			JsonNode infoNode = mapper.readTree(result).get("channel").get("info");
			
			int page = infoNode.get("page").asInt();
			int count = infoNode.get("count").asInt();
			int totalCount = infoNode.get("totalCount").asInt();
			
			System.out.println("[page : " + page + "] " );
			
			if(totalCount == 0){
				return false;
			}
			if(resultNode.isArray()){
				for(int i = 0 ; i < resultNode.size(); i++){
					
					JsonNode tmp = resultNode.get(i);
					System.out.println("current position : " + latitude+","+longitude);
					// SAVE PLACES 
					if( !connector.isSaved( tmp.get("id").asText(), code)){
						connector.savePlace(tmp, code);
						System.out.println("writing. .  ." + tmp.toString());
					}
				}
			}
			if( page < Math.ceil( totalCount / count)){
				return true;
			}
			else
				return false;
			
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

}
