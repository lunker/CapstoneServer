package dk.spring.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

/***
 * 
 * @author Lee Dong Kyoo 
 *
 * 데이트 코스와 관련된 요청을 처리한다. 
 */

@RestController
public class CourseController {

	private Logger logger = Logger.getLogger(CourseController.class);
	private DatabaseConnector connector = DBFactory.getConnector();
	private ObjectMapper mapper = MapperFactory.getMapper();
	
	/***
	 * 
	 * @param userId
	 * @return course
	 * 
	 * 사용자가 저장해놓은 데이트 코스를 불러온다.
	 * 
	 */
	@RequestMapping(value="/loadcourse", method=RequestMethod.GET)
	public String loadCourse(
			@RequestParam(value="userid")String userId
			){
		logger.info("[LOAD_COURSE] REQUEST : " + userId);
//		System.out.println("[LOAD_COURSE] " + userId);
		Document course = null;
		try{
			course = connector.getMyCollection(userId).find(new Document("id",userId)).first();
			
			
			// 마이코스에 저장한 장소들의 아이디 
			String placeIds = course.getString("placeids");
			System.out.println("before split : "+placeIds);
			
			ObjectNode root = new ObjectNode(mapper.getNodeFactory());
			ArrayNode courseArrayNode = root.putArray("course");
			JsonNode placeIdsNode = null;
			
			if(placeIds!=null){
				try {
					placeIdsNode = mapper.readTree(placeIds);
					JsonNode place = null;
					Document tmpPlace = null;
					String placeId = "";
					double userRatings = -1;
					if(placeIdsNode.isArray()){
						
						for(int i=0; i<placeIdsNode.size(); i++){
							userRatings = -1;
							
							place = placeIdsNode.get(i);
							placeId = place.get("placeid").asText();
							
							/*
							 * 사용자가 해당 장소에 평가한 평점을 가져온다 
							 */
							Document reviewedPlace = connector.getMyCollection(userId).find(new Document("placeid", placeId)).first();
							
							if(reviewedPlace!=null)
								userRatings = reviewedPlace.getDouble("ratings");
							
							
							tmpPlace = connector.getMyCollection(connector.codeToCollection(place.get("code").asText()))
							.find(new Document("id", place.get("placeid").asText())).first();
							
							courseArrayNode.add( makeObjectNode(tmpPlace.append("userRatings", userRatings)));
						}
					}
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				courseArrayNode = null;
				placeIdsNode = null;
				course = null;
				
				return root.toString();
			}// end if
			else{
				return "0";
			}
		} catch(Exception e){
			return "0";
		}
		
		
	}
	
	
	/***
	 * 
	 * @param course
	 * @return
	 * 
	 * 사용자가 선택한 데이트 코스를 저장한다. 
	 */
	@RequestMapping(value="/savecourse", method=RequestMethod.POST)
	public String saveCourse(
//			@RequestParam(value="userid", defaultValue="1", required=false) String userId,
//			@RequestParam(value="placeids", defaultValue="1", required=false) String placeIds
			@RequestBody CourseModel course
			){
		
//		System.out.println("userid:"+ course.getUserId() + "placeids" + course.getPlaceIds());
		logger.info("[SAVE_COURSE] REQEUST : " + course.getUserId());
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
	 * Input : 
	 *  - latitude : 위도 
	 *  - longitude : 경도 
	 *  - userid : 사용자 고유 아이디
	 *  
	 * Output : 
	 *  - course 
	 *   
	 * 사용자가 선택한 위치에 따라 코스를 추천해준다. 
	 *   
	 */
	@RequestMapping(value = "/course", method=RequestMethod.GET)
	public String recommendCourse(
			@RequestParam(value="latitude", defaultValue="1", required=false)String latitude, 
			@RequestParam(value="longitude", defaultValue="1", required=false)String longitude,
			@RequestParam(value="userid", defaultValue="1", required=false) String userId
			){

		logger.info("[COURSE_RECOMMEND] REQUEST : " + userId);
		
		// 사용자가 가입시에 저장한 카테고리의 종류로 코스를 구성한다. 
		// 추천하는 코스는 최대 3개. 
		
		String preferCategorys = connector.getMyCollection(userId).find(new Document("id", userId)).first().getString("prefercategory");
		System.out.println("categorys"+preferCategorys);
		String[] categorys = preferCategorys.split(","); 
		ArrayList<ArrayList<ObjectNode>> placesTaker = new ArrayList<ArrayList<ObjectNode>>(); // 코스정보를 저장한다 
		
		Recommender tmpRcm = null;
		
		// 사용자가 저장한 카테고리의 수 만큼 1개의 코스를 구성 
		// num -> 카테고리를 지정한다. 
		for(int num=0; num<categorys.length; num++){
			try {
				
				// 해당 카테고리의 추천기를 가져온다 
				tmpRcm = getRecommender(categorys[num]);
				placesTaker.add(new ArrayList<ObjectNode>());
				
				
				// 추천기가 있는 경우 
				if(tmpRcm!=null){
					System.out.println("[COURSE_RECOMMEND]" +"in recommender , user : " + userId.substring(1));
					
					// 추천기로부터 장소를 추천받는다. 
					List<RecommendedItem> recommendedList = tmpRcm.
							recommend(Integer.parseInt( userId.substring(1)) , 3 );
					
					// 추천을 3개 이하일경우, 나머지는 평점으로 가져온다.
					if(recommendedList.size()<=3){
						
						System.out.println("[COURSE_RECOMMEND]" +"Recommender size : " +recommendedList.size() );
						placesTaker.get(num).addAll(findPlace(categorys[num], latitude, longitude, 3-recommendedList.size()));
						
						for(int i=0; i<recommendedList.size(); i++){
							
							placesTaker.
							get(num).
								add(
									makeObjectNode(
											connector.getPlaceById(
													categorys[num], recommendedList.get(i).getItemID()+"")));
							System.out.println("[RECOMMEND_GPS]recommended item : " + recommendedList.get(i).getItemID());
						}
					}
					
					else{
						
						System.out.println("[COURSE_RECOMMEND]" +"in recommender, else");
					}
				}
				
				//추천기가 없어서 평점으로만 받아오는 경우 
				else{
					System.out.println("[COURSE_RECOMMEND]" +"no recommender");
					placesTaker.get(num).addAll(findPlace(categorys[num], latitude, longitude, 3));
				}
				
			} catch (NumberFormatException | TasteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		/*
		 * Generate Course
		 */
		
		ObjectNode root = new ObjectNode(mapper.getNodeFactory());
		ArrayNode courseArrayNode = null;
		for(int course=0; course<3; course++){
			 courseArrayNode = root.putArray("course"+course);
			for(int num=0; num<categorys.length; num++){
				//error..
				courseArrayNode.add(placesTaker.get(num).get(course));
			}
		}
		
//		root.putArray("course1").add(placesTaker.get().get(0)).add(placeSecondStack.get(0)).add(placeThirdStack.get(0));
//		root.putArray("course2").add(placeFirstStack.get(1)).add(placeSecondStack.get(1)).add(placeThirdStack.get(1));
//		root.putArray("course3").add(placeFirstStack.get(2)).add(placeSecondStack.get(2)).add(placeThirdStack.get(2));
		placesTaker = null;
		
		return root.toString();
	}

	
	/***
	 * 
	 * @param code
	 * @param latitude
	 * @param longitude
	 * @param count
	 * @return
	 * 
	 * 해당 collection으로부터 현재위치에서 가까운 장소들을 찾는다.
	 */
	public ArrayList<ObjectNode> findPlace(String code, String latitude, String longitude, int count){
		
		ArrayList<ObjectNode> placeStack = new ArrayList<ObjectNode>();
		ArrayList<String> projectionFields = new ArrayList<String>();
		projectionFields.add("latitude");
		projectionFields.add("longitude");
		projectionFields.add("id");
		projectionFields.add("ratings");
		Document place = null;
		// 평점순으로 정렬된 place list
		MongoCursor<Document> allDocuments = connector.getMyCollection(connector.codeToCollection(code)).
						find().projection(Projections.include(projectionFields)).sort(Sorts.descending("ratings")).iterator();

				// firstPlaceList.
		while (allDocuments.hasNext()) {

			place = allDocuments.next();
					
			try{
				if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
						Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 1500){
					System.out.println("first bb");
					if(placeStack.size()<count){
						placeStack.add(makeObjectNode(connector.getPlaceById(code, place.getString("id"))));
					}
					else
						break;
				}
			} catch(Exception e){
				System.out.println("error in first place");
				continue;
			}
		}// end while
				

		place = null;
		allDocuments = null;
		return placeStack;
	}
	
	
	/***
	 * 
	 * @param category
	 * @return recommender 
	 * 
	 * 카테고리에 맞는 추천기를 반환한다. 
	 */
	public Recommender getRecommender(String category){
		
		Recommender rcm = null;
		
		// 식당 
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
	
	/***
	 * 
	 * @param latitude
	 * @param longitude
	 * @param userId
	 * @return
	 * 
	 * 현재 사용자의 위치에 따른 데이트 코스 추천 
	 * 
	 */
	@RequestMapping(value="/courseGPS")
	public String recommendCourseByGPS(
			@RequestParam(value="latitude")String latitude,
			@RequestParam(value="longitude")String longitude,
			@RequestParam(value="userid", defaultValue="1", required=false) String userId
			){
		
	
		logger.info("[COURSE_RECOMMEND_GPS] in course recommend");
		
		/*
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
		
		Recommender tmpRcm = null;
		for(int num=0; num<categorys.length; num++){
			try {
				
				// 추천 받은 장소의 아이디를 받아온다.
				
				tmpRcm = getRecommender(categorys[num]);
				placesTaker.add(new ArrayList<ObjectNode>());
				// 추천기가 있는 경
				if(tmpRcm!=null){
					List<RecommendedItem> recommendedList = tmpRcm.
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
							
							System.out.println("[RECOMMEND_GPS]recommended item : " + recommendedList.get(i).getItemID());
						}
					}
				}
				
				//추천기가 없어서 평점으로만 받아오는 경우 
				else{
					placesTaker.get(num).addAll(findPlace(categorys[num], latitude, longitude, 3));
				}
				
			} catch (NumberFormatException | TasteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
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
	
	@RequestMapping(value="/courseCondition", method=RequestMethod.GET)
	public String recommendCourseByCondition(
			@RequestParam(value="latitude", defaultValue="1", required=false) String latitude,
			@RequestParam(value="longitude", defaultValue="1", required=false) String longitude,
			@RequestParam(value="placeid", defaultValue="1", required=false) String placeId,
			@RequestParam(value="code", defaultValue="1", required=false) String code,
			@RequestParam(value="userid", defaultValue="1", required=false) String userId
			){

		logger.info("[COURSE_RECOMMEND_CONDITION] in course recommend");
		
		/*
		 * Get user category
		 */
		
		// 1 코스에 사용자가 설정한 카테고리 수 만큼 장소가 들어간다.
		// 전체적인 코스는 3개 
		
		String preferCategorys = connector.getMyCollection(userId).find(new Document("id", userId)).first().getString("prefercategory");
		System.out.println("categorys"+preferCategorys);
		String[] categorys = preferCategorys.split(",");
		int length = categorys.length;
		ArrayList<ArrayList<ObjectNode>> placesTaker = new ArrayList<ArrayList<ObjectNode>>();
		
		/*
		 * Get recommend from mining
		 */
		Recommender tmpRcm = null;
		boolean isHere = false;
		for(int num=0; num<length; num++){
			if(categorys[num].equals(code)){
				isHere = true;
			}
		}
		
		// 선택한 장소가 유저 선호하는 카테고리에 없을때 
		// 
		// 추천 시작 
		for(int num=0; num<length; num++){
			
			try {
				
				// 추천 받은 장소의 아이디를 받아온다.
				placesTaker.add(new ArrayList<ObjectNode>());
				if(categorys[num].equals(code)){
					ObjectNode node = makeObjectNode(connector.getPlaceById(code, placeId));
					placesTaker.add(new ArrayList<ObjectNode>());
					placesTaker.get(num).add(node);
					placesTaker.get(num).add(node);
					placesTaker.get(num).add(node);
					continue;
				}
				tmpRcm = getRecommender(categorys[num]);
				
				// 추천기가 있는 경우 
				if(tmpRcm!=null){
					List<RecommendedItem> recommendedList = tmpRcm.
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
				}
				
				//추천기가 없어서 평점으로만 받아오는 경우 
				else{
					placesTaker.get(num).addAll(findPlace(categorys[num], latitude, longitude, 3));
				}
				
			} catch (NumberFormatException | TasteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}// end for
		
		// 선택한 장소의 카테고리가 유저의 선호도에 없을때, 
		// 별도로 추가한다 
		if(!isHere){
			length++;
			ObjectNode node = makeObjectNode(connector.getPlaceById(code, placeId));
			placesTaker.add(new ArrayList<ObjectNode>());
			placesTaker.get(length).add(node);
			placesTaker.get(length).add(node);
			placesTaker.get(length).add(node);
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
		 * Generate Course
		 */
		
		ObjectNode root = new ObjectNode(mapper.getNodeFactory());
		ArrayNode courseArrayNode = null;
		for(int course=0; course<3; course++){
			 courseArrayNode = root.putArray("course"+course);
			for(int num=0; num<length; num++){
				courseArrayNode.add(placesTaker.get(num).get(course));
			}
		}
		
//		root.putArray("course1").add(placesTaker.get().get(0)).add(placeSecondStack.get(0)).add(placeThirdStack.get(0));
//		root.putArray("course2").add(placeFirstStack.get(1)).add(placeSecondStack.get(1)).add(placeThirdStack.get(1));
//		root.putArray("course3").add(placeFirstStack.get(2)).add(placeSecondStack.get(2)).add(placeThirdStack.get(2));
		placesTaker = null;
		
		return root.toString();
	}
	
	/***
	 * 
	 * @param place
	 * @return
	 * 
	 * place POJO를 JsonObject로 변환한다. 
	 */
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
		
		
		tmp.put("userRatings", place.getDouble("userRatings"));
		
		return tmp;
	}
	
	
	/***
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 * 
	 * 두 지점의 거리를 위도,경도를 이용하여 계산한다. 
	 */
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

