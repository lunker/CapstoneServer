package dk.spring.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.data.Json;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import dk.spring.server.factory.DBFactory;
import dk.spring.server.factory.MapperFactory;
import dk.spring.server.mining.ModelGenerator;
import dk.spring.server.model.CourseModel;
import dk.spring.util.DatabaseConnector;


@RestController
public class CourseController {

	private Logger logger = Logger.getLogger(CourseController.class);
	private DatabaseConnector connector = DBFactory.getConnector();
	private ObjectMapper mapper = MapperFactory.getMapper();
	
	/*
	 * 구현해야함 !
	 */
	
	/*
	@RequestMapping(value="/saecourse", method=RequestMethod.POST)
	public String saveCourse(
			@RequestParam(value="userid", defaultValue="1", required=false)String userid, 
			@RequestParam(value="firstplaceid", defaultValue="1", required=false)String firstPlaceId, 
			@RequestParam(value="secondplaceid", defaultValue="1", required=false)String secondPlaceId, 
			@RequestParam(value="thirdplaceid", defaultValue="1", required=false)String thirdPlaceId){
		
		connector.saveCourse(userid, firstPlaceId, secondPlaceId, thirdPlaceId);
		
		return "";
	}
	*/
	
	@RequestMapping(value="/loadcourse", method=RequestMethod.GET)
	public String loadCourse(
			@RequestParam(value="userid")String userId
			){
		System.out.println("[LOAD_COURSE] " + userId);
		Document course = connector.getMyCollection(userId).find(new Document("id",userId)).first();
		
		String placeIds = course.getString("placeids");
		System.out.println("before split : "+placeIds);
		
		ObjectNode root = new ObjectNode(mapper.getNodeFactory());
		ArrayNode courseArrayNode = root.putArray("course");
		JsonNode placeIdsNode = null;
		
		try {
			placeIdsNode = mapper.readTree(placeIds);
			JsonNode place = null;
			Document tmpPlace = null;
			
			if(placeIdsNode.isArray()){
				
				for(int i=0; i<placeIdsNode.size(); i++){
					place = placeIdsNode.get(i);
					tmpPlace = connector.getMyCollection(connector.codeToCollection(place.get("code").asText()))
					.find(new Document("id", place.get("placeid").asText())).first();
					
					courseArrayNode.add( makeObjectNode(tmpPlace));
				}
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		courseArrayNode = null;
		placeIdsNode = null;
		course = null;
		
		return root.toString();
	}
	
	public String deletePlace(){
		
		
		
		
		return "";
	}
	/*
	 * 수정해야함 . . . .
	 * json 방식 바꿨음 !!
	 */
	
	@RequestMapping(value="/savecourse", method=RequestMethod.POST)
	public String saveCourse(
//			@RequestParam(value="userid", defaultValue="1", required=false) String userId,
//			@RequestParam(value="placeids", defaultValue="1", required=false) String placeIds
			@RequestBody CourseModel course
			){
		
		System.out.println("userid:"+ course.getUserId() + "placeids" + course.getPlaceIds());
		
		/*
		int totalCourses = 0;
		
		try{
			MongoCursor<Document> courseIterator = connector.getMyCollection(course.getUserId()).find().projection(Projections.include("courseid")).iterator();
			
			while(courseIterator.hasNext()){
				courseIterator.next();
				totalCourses++;
			}
		} catch(Exception e){
			totalCourses = 0;
			System.out.println("projection error, null?");
		}
		*/
		
		// generate course information
//		CourseModel course = new CourseModel();
		course.setCourseId("0");
		
		Document doc = new Document();
		doc.append("userid", course.getUserId());
		doc.append("placeids", course.getPlaceIds());
		doc.append("courseid", course.getCourseId());
		
		try{
			connector.getMyCollection(course.getUserId()).findOneAndUpdate(new Document("id",course.getUserId()), new Document("$set", doc));
			
		} catch(MongoException me){
			me.printStackTrace();
			return "0";
		}
		
		return "1";
	}
	
	/*
	 * 임시땜빵용 !
	 */
	
	@RequestMapping(value = "/course", method=RequestMethod.GET)
	public String recommendCourse(
			@RequestParam(value="latitude", defaultValue="1", required=false)String latitude, 
			@RequestParam(value="longitude", defaultValue="1", required=false)String longitude,
			@RequestParam(value="userid", defaultValue="1", required=false) String userId
			){

		logger.info("[COURSE_RECOMMEND] in course recommend");
		System.out.println("lat,lng, userid : "+latitude+ ","+longitude + "," +userId);
		
		/*
		 * 
		 * Get user category
		 */
		
		// 1 코스에 사용자가 설정한 카테고리 수 만큼 장소가 들어간다.
		// 전체적인 코스는 3개 
		
		String preferCategorys = connector.getMyCollection(userId).find(new Document("id", userId)).first().getString("prefercategory");
		System.out.println("categorys"+preferCategorys);
		String[] categorys = preferCategorys.split(",");
		ArrayList<ArrayList<ObjectNode>> placesTaker = new ArrayList<ArrayList<ObjectNode>>();
		
		/*
		 * Get recommend from mining
		 */
		
		for(int num=0; num<categorys.length; num++){
			try {
				
				// 추천 받은 장소의 아이디를 받아온다.
				List<RecommendedItem> recommendedList = getRecommender(categorys[num]).
						recommend(Integer.parseInt( userId.substring(1)) , 3 );
				
				// 추천을 3개 미만으로 받을 경우, 나머지는 평점으로 가져온다.
				if(recommendedList.size()<3){
					// ArrayList<ObjectNode>를 반환.
					placesTaker.get(num).addAll(findPlace(categorys[num], latitude, longitude, 3-recommendedList.size()));
					
					for(int i=0; i<recommendedList.size(); i++){
						
						placesTaker.
						get(num).
							add(
								makeObjectNode(
										connector.getPlaceById(
												categorys[num], recommendedList.get(i).getItemID()+"")));
					}
				}
			} catch (NumberFormatException | TasteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * Get recommend from ratings
		 */
		/*
		for(int num=0; num<categorys.length; num++){
			placesTaker.add(findPlace(categorys[num], latitude, longitude,3));
		}
		*/
		
		/*
		 * find minimum
		 */
		
		/*
		 * Generate Course
		 */
		
		ObjectNode root = new ObjectNode(mapper.getNodeFactory());
		ArrayNode courseArrayNode = null;
		for(int course=0; course<3; course++){
			 courseArrayNode = root.putArray("course"+course);
			for(int num=0; num<categorys.length; num++){
				courseArrayNode.add(placesTaker.get(num).get(course));
			}
		}
		
//		root.putArray("course1").add(placesTaker.get().get(0)).add(placeSecondStack.get(0)).add(placeThirdStack.get(0));
//		root.putArray("course2").add(placeFirstStack.get(1)).add(placeSecondStack.get(1)).add(placeThirdStack.get(1));
//		root.putArray("course3").add(placeFirstStack.get(2)).add(placeSecondStack.get(2)).add(placeThirdStack.get(2));
		placesTaker = null;
		
		return root.toString();
	}

	public ArrayList<ObjectNode> findPlace(String code, String latitude, String longitude, int count){
		
		ArrayList<ObjectNode> placeStack = new ArrayList<ObjectNode>();
		Document place = null;
		// 평점순으로 정렬된 place list
		MongoCursor<Document> allDocuments = connector.getMyCollection(connector.codeToCollection(code)).
						find().sort(Sorts.descending("ratings")).iterator();

				// firstPlaceList.
		while (allDocuments.hasNext()) {

			place = allDocuments.next();
					
			try{
				if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
						Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 1500){
					System.out.println("first bb");
					if(placeStack.size()<count){
						placeStack.add(makeObjectNode(place));
					}
					else
						break;
				}
			} catch(Exception e){
				System.out.println("error in first place");
				continue;
			}
		}// end while
				
		/*
		if(placeStack.size()<=3){
//			allDocuments = connector.getMyCollection(connector.codeToCollection(code)).find().sort(Sorts.descending("ratings")).iterator();
//			allDocuments.
			while (allDocuments.hasNext()) {
				place = allDocuments.next();
						
				try{
					if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
							Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 3500){
						System.out.println("thr bb");
								
						if(placeStack.size()<=3){
							for(int i=0; i<placeStack.size(); i++){
								if( !placeStack.get(i).get("title").equals(place.getString("title")))
									placeStack.add(makeObjectNode(place));
							}
						}
						else
							break;
					}
				} catch(Exception e){
					System.out.println("error in third place");
					continue;
				}
			}// end while
		}// end if
		*/

		place = null;
		allDocuments = null;
		return placeStack;
	}
	
	public Recommender getRecommender(String category){
		
		Recommender rcm = null;
		
		if(category.equals("FD6")){
			rcm = ModelGenerator.getFoodRcm();
		}
		else if(category.equals("CE7")){
			rcm = ModelGenerator.getCafeRcm();
		}
		else if(category.equals("AD5")){
			rcm = ModelGenerator.getRestRcm();
		}
		else if(category.equals("AT4")){
			rcm = ModelGenerator.getTourRcm();
		}
		else{
			rcm = ModelGenerator.getCultureRcm();
		}
		
		return rcm; 
	}
	
	@RequestMapping(value="/courseGPS")
	public String recommendCourseByGPS(
			@RequestParam(value="latitude")String latitude,
			@RequestParam(value="longitude")String longitude,
			@RequestParam(value="categorys")String categorys){
		
		String collection = connector.codeToCollection(categorys);
		ArrayList<String> nearPlacesList = new ArrayList<String>();
		
		MongoCursor<Document> allPlacesInColleciton = connector.getMyCollection(collection).find().iterator();
		
		while (allPlacesInColleciton.hasNext()) {
			Document place = allPlacesInColleciton.next();
			
			// 인접한 곳에 있는 장소일경우
			// 평가 정보를 가져온다 
			if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
					Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 10000){
				
				System.out.println(place.get("review"));
			}
		}// end while
		
		// mining~
		// run CF
		return "";
	}
	
	/*
	 * 나중에 ~
	 */
	
	
	@RequestMapping(value="/courseTheme")
	public String recommedCourseByTheme(
			
			){
		
		return "";
	}
	
	@RequestMapping(value="/courseRegion")
	public String recommendCourseByRegion(){
		
		
		return "";
	}
	
	@RequestMapping(value="/courseCondition")
	public String recommendCourseByCondition(){
		
		return "";
	}
	
	public ObjectNode makeObjectNode(Document place){
		
		ObjectNode tmp = new ObjectNode(mapper.getNodeFactory());
		
		tmp.put("id", place.getString("id"));
		tmp.put("phone", place.getString("phone"));
		tmp.put("newAddress", place.getString("newAddress"));
		tmp.put("imageUrl", place.getString("imageUrl"));
		
		tmp.put("direction", place.getString("direction"));
		tmp.put("placeUrl", place.getString("placeUrl"));
		tmp.put("title", place.getString("title"));
		tmp.put("category", place.getString("category"));
		tmp.put("address", place.getString("address"));
		tmp.put("longitude", place.getString("longitude"));
		tmp.put("latitude", place.getString("latitude"));
		tmp.put("addressBCode", place.getString("addressBCode"));
		tmp.put("ratings", place.getDouble("ratings"));
		tmp.put("code",place.getString("code"));
		
		return tmp;
	}
	
	public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
		double earthRadius = 6371000; // meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);

		return dist;
	}
	
	
}

