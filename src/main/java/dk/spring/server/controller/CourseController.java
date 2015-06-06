package dk.spring.server.controller;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import dk.spring.server.factory.DBFactory;
import dk.spring.server.factory.MapperFactory;
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
	@RequestMapping(value="/savecourse", method=RequestMethod.POST)
	public String saveCourse(
			@RequestParam(value="userid", defaultValue="1", required=false)String userid, 
			@RequestParam(value="firstplaceid", defaultValue="1", required=false)String firstPlaceId, 
			@RequestParam(value="secondplaceid", defaultValue="1", required=false)String secondPlaceId, 
			@RequestParam(value="thirdplaceid", defaultValue="1", required=false)String thirdPlaceId){
		
		connector.saveCourse(userid, firstPlaceId, secondPlaceId, thirdPlaceId);
		
		return "";
	}
	*/
	
	@RequestMapping(value="/loadCourse", method=RequestMethod.GET)
	public String loadCourse(
			@RequestParam(value="asdf")String tmp
			){
		return "";
		
	}
	
	@RequestMapping(value="/savecourse2", method=RequestMethod.POST)
	public String saveCourse(
//			@RequestParam(value="userid", defaultValue="1", required=false) String userId,
//			@RequestParam(value="placeids", defaultValue="1", required=false) String placeIds
			@RequestBody CourseModel course
			){
		
		System.out.println("userid:"+ course.getUserId() + "placeids" + course.getPlaceIds());
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
		
		// generate course information
//		CourseModel course = new CourseModel();
		course.setCourseId("mc"+(totalCourses+1));
		
		Document doc = new Document();
		doc.append("userid", course.getUserId());
		doc.append("placeids", course.getPlaceIds());
		doc.append("courseid", course.getCourseId());
		
		try{
			connector.getMyCollection(course.getUserId()).insertOne(doc);
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
			@RequestParam(value="placeCodeFirst", defaultValue="1", required=false)String placeCodeFirst, 
			@RequestParam(value="placeCodeSecond", defaultValue="1", required=false)String placeCodeSecond, 
			@RequestParam(value="placeCodeThird", defaultValue="1", required=false)String placeCodeThird) {

//		System.out.println("[COURSE_RECOMMEND] in course recommend");
		logger.info("[COURSE_RECOMMEND] in course recommend");
		System.out.println("lat,lng:"+latitude+ ","+longitude);
		
		
		ArrayList<ObjectNode> placeFirstStack = new ArrayList<ObjectNode>();
		ArrayList<ObjectNode> placeSecondStack = new ArrayList<ObjectNode>();
		ArrayList<ObjectNode> placeThirdStack = new ArrayList<ObjectNode>();
		
		// 평점순으로 정렬된 place list
		MongoCursor<Document> firstPlaceList = 
				connector.getMyCollection(connector.codeToCollection(placeCodeFirst)).
				find().sort(Sorts.descending("ratings")).iterator();

		// firstPlaceList.
		while (firstPlaceList.hasNext()) {

			Document place = firstPlaceList.next();
			
			try{
				if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
						Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 1000){
					System.out.println("first bb");
					if(placeFirstStack.size()<=3){
						placeFirstStack.add(makeObjectNode(place));
					}
					else
						break;
				}
			} catch(Exception e){
				System.out.println("error in first place");
				continue;
			}
		}
		
		
		MongoCursor<Document> secondPlaceList = connector.getMyCollection(connector.codeToCollection(placeCodeSecond)).find().sort(Sorts.descending("ratings")).iterator();
		
		while (secondPlaceList.hasNext()) {
//			System.out.println("iter second ");
			Document place = secondPlaceList.next();
			
			try{
				if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
						Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 1000){
					System.out.println("sec bb");
					if(placeSecondStack.size()<=3)
						placeSecondStack.add(makeObjectNode(place));
					else
						break;
				}
			} catch(Exception e){
				System.out.println("error in second place");
				continue;
			}
			
		}		
		
		MongoCursor<Document> thirdPlaceList = connector.getMyCollection(connector.codeToCollection(placeCodeThird)).find().sort(Sorts.descending("ratings")).iterator();

		while (thirdPlaceList.hasNext()) {
			Document place = thirdPlaceList.next();
			
			try{
				if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
						Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 2500){
					System.out.println("thr bb");
					
					if(placeThirdStack.size()<=3)
						placeThirdStack.add(makeObjectNode(place));
					else
						break;
				}
			} catch(Exception e){
				System.out.println("error in thrd place");
				continue;
			}

			
		}
		
		if(placeThirdStack.size()<=3){
			thirdPlaceList = connector.getMyCollection(connector.codeToCollection(placeCodeThird)).find().sort(Sorts.descending("ratings")).iterator();
			while (thirdPlaceList.hasNext()) {
				Document place = thirdPlaceList.next();
				
				try{
					if(distFrom(Float.parseFloat(latitude), Float.parseFloat(longitude), 
							Float.parseFloat(place.getString("latitude")), Float.parseFloat(place.getString("longitude"))) <= 4500){
						System.out.println("thr bb");
						
						if(placeThirdStack.size()<=3)
							placeThirdStack.add(makeObjectNode(place));
						else
							break;
					}
				} catch(Exception e){
					System.out.println("error in third place");
					continue;
				}

				
			}
		}

		ObjectNode root = new ObjectNode(mapper.getNodeFactory());
//		courses.put
		/*
		course1.putArray("asdf").add(course2)
		course1.put("first", placeFirstStack.get(0));
		course1.put("second", placeSecondStack.get(0));
		course1.put("third", placeThirdStack.get(0));
		root.put("course1", course1.toString());
		
		
		course2.put("first", placeFirstStack.get(1));
		course2.put("second", placeSecondStack.get(1));
		course2.put("third", placeThirdStack.get(1));
		root.put("course2", course1.toString());
		
		course3.put("first", placeFirstStack.get(2));
		course3.put("second", placeSecondStack.get(2));
		course3.put("third", placeThirdStack.get(2));
		root.put("course3", course1.toString());
		*/
		
		
		root.putArray("course1").add(placeFirstStack.get(0)).add(placeSecondStack.get(0)).add(placeThirdStack.get(0));
		root.putArray("course2").add(placeFirstStack.get(1)).add(placeSecondStack.get(1)).add(placeThirdStack.get(1));
		root.putArray("course3").add(placeFirstStack.get(2)).add(placeSecondStack.get(2)).add(placeThirdStack.get(2));
		
		return root.toString();
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

