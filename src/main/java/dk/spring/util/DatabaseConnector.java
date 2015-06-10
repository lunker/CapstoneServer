package dk.spring.util;


import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import dk.spring.server.Constant;
import dk.spring.server.model.ReviewModel;
import dk.spring.server.model.UserModel;

public class DatabaseConnector {
	
	private static final Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);
	
	private final String DATABASE = "superdb";
	private final int PORT = 27017;
	private final String HOST = "localhost";
	
	public volatile MongoClient mongoClient = null;
	private MongoDatabase database = null;
	
	public MongoDatabase getDatabase() {
		return database;
	}

	public void setDatabase(MongoDatabase database) {
		this.database = database;
	}
	
	public void connect(){

		MongoDatabase tmp = null;
		
		mongoClient = new MongoClient( HOST , PORT);
		tmp = mongoClient.getDatabase(DATABASE);
		if(tmp == null){
//			mongoClient.
			System.out.println("fail to connect MongoDB");
		}
		else{
			System.out.println("connect the DB");
			setDatabase(tmp);
		}

	}// end method

	public MongoCollection<Document> getMyCollection(String collectionName){
		
		MongoCursor<String> cursor = database.listCollectionNames().iterator();
		while(cursor.hasNext()){
			if(cursor.next().equals(collectionName)){
				System.out.println("find collection ( " + collectionName + ")");
				return database.getCollection(collectionName);
			}
		}
		System.out.println("fail to find collection");
			
		return null;
	}
	
	public void savePlace(JsonNode place, String code){
	
		Document placeDoc = new Document();
		placeDoc.append("phone", place.get("phone").asText());
		placeDoc.append("imageUrl", place.get("imageUrl").asText());
//		placeDoc.append("direction", place.get("direction").asText());
//		placeDoc.append("zipcode", place.get("zipcode").asText());
		placeDoc.append("placeUrl", place.get("placeUrl").asText());
		placeDoc.append("id", place.get("id").asText());
		placeDoc.append("title", place.get("title").asText());
		placeDoc.append("category", place.get("category").asText());
//		placeDoc.append("distance", place.get("distance").asText());
		placeDoc.append("address", place.get("address").asText());
		placeDoc.append("longitude", place.get("longitude").asText());
		placeDoc.append("latitude", place.get("latitude").asText());
		placeDoc.append("addressBCode", place.get("addressBCode").asText());
		placeDoc.append("code", code);
		placeDoc.append("count", 1);
		placeDoc.append("ratings", 2.5);
		
		String collection = "";
		collection = codeToCollection(code);
		
		MongoCollection<Document> collec = null;
		collec = getMyCollection(collection);
		
		if( collec != null){
			getMyCollection(collection.toString()).insertOne(placeDoc);
		}
		else{
			database.createCollection(collection.toString());
		}
	}// end method
	
	public boolean savePlaceRate(String placeId, String rating, String code){
	
		Document filter = new Document();
		filter.append("id", placeId);
		
		Document ratingField = new Document();
		ratingField.append("rating", rating);
		
		Document update = new Document();
		update.append("$set", ratingField);
		
		
		getMyCollection(code).findOneAndUpdate(filter, update);
		
		return true;
	}
	
	public String getPlace(String code, String latitude, String longitude){
		
		String collection = "";
		collection = codeToCollection(code);
		
		
		Document filter = new Document();
		filter.append("latitude", latitude);
		filter.append("longitude", longitude);
		filter.append("code", code);
		
		Document result = null;
		result = getMyCollection(collection).find(filter).iterator().next();
		
		if(result == null){
			return null;
		}
		else{
			return result.toJson();
		}
	}
	
	public Document getPlaceById(String code, String placeid){
		
		Document place = null;
		
		place = getMyCollection(codeToCollection(code)).find(new Document("id", placeid)).first();
		
		return place;
		
	}

	public boolean isSaved(String id, String code){
		
		String collection = "";
		collection = codeToCollection(code);
		
		if(getMyCollection(collection) ==null){
			database.createCollection(collection);
		}
		
		Document filter = new Document();
		filter.append("id", id);
		
		Document place = null;
		place = getMyCollection(collection).find(filter).first();
		
		// not saved
		if(place == null){
			return false;
		}
		else
			return true;
	}
	
	public String codeToCollection(String code){
		String collection = "";
		if(code.equals(Constant.DAUM_CODE_FOOD)){
			collection = "foodplace";
		}
		else if(code.equals(Constant.DAUM_CODE_CAFE)){
			collection = "cafeplace";
		}
		else if(code.equals(Constant.DAUM_CODE_TOUR)){
			collection = "tourplace";
		}
		else if(code.equals(Constant.DAUM_CODE_REST)){
			collection = "restplace";
		}
		else if(code.equals(Constant.DAUM_CODE_CULTURE))	{
			collection = "cultureplace";
		}
		else{
			System.out.println("faile to map code to collection");
		}
		
		return collection;
	}

	public boolean isRegisteredUser(String email){
		
		Document filter = new Document();
		filter.append("email", email);
		
		Document target = null;
		target = getMyCollection("user").find(filter).first();
		
		if(target == null){
			
			System.out.println("not registered");
			return false;
		}
		else{
			System.out.println("registered");
			return true;
		}
	}
	
	public String saveUser(UserModel user){
		
		if(!isRegisteredUser(user.getEmail())){
			long totalUserNum = getMyCollection("user").count();
			String userId = "c"+(totalUserNum+1);
			
			database.createCollection(userId);
			Document userData = new Document();
			
			userData.append("id", userId);
			userData.append("email", user.getEmail());
			userData.append("password", user.getPassword());
			userData.append("gender", user.getGender());
			userData.append("prefercategory", user.getPreferCategory());
			
			getMyCollection(userId).insertOne(userData);
			getMyCollection("user").insertOne(userData);
			
			return userId;
		}	
		else 
			return "0";
	}
	
	public String login(String email, String password){
		
		Document filter = new Document();
		filter.append("email", email);
		
		Document result = null;
		result = getMyCollection("user").find(filter).first();
		
		if(result!=null){
			if(result.getString("password").equals(password)){
				System.out.println("Find the user");
				return result.getString("id");
			}
			else{
				System.out.println("user data is not matched");
				return "0";
			}
		}
		else{
			System.out.println("user is not registered");
			return "0";
		}	
	}
	
	public void saveCourse(String userid, String firstPlaceId, String secondPlaceId, String thirdPlaceId){
		
//		Document filter = new Document();
//		filter.append("id", userid);
		
		Document doc = new Document();
		doc.append("firstplace", firstPlaceId );
		doc.append("secondplace", secondPlaceId);
		doc.append("thirdplace", thirdPlaceId );
		
		
		getMyCollection(userid).insertOne(doc);
		
	}
	// Save Review
	// Review : 
	
	public String saveReview(ReviewModel review){
		
		/*
		 * 이전에 사용자가 평가를 했었는지 봐야함. 
		 * 평가는 남겼으면 이전 평가를 지우고 
		 * 새로운 평가를 남겨야 .
		 */
		
		logger.info("[SAVE_REVIEW] ");
		
		Document doc = getMyCollection(review.getUserId()).find(new Document("placeid", review.getPlaceId())).first();
		
		// 이미 평가를 한 경우 
		if(doc!=null){
			System.out.println("[SAVE_REVIEW] already reviewed ");
			// 1. userid collection 수정
			// 2. review collection 수정 
			// 3. 장소정보 collection 수정 
			
			// (1)
			
			// get user before ratings
			double beforeRatings = getMyCollection(review.getUserId()).find(new Document("placeid", review.getPlaceId())).first().getDouble("ratings");
			Document filter = new Document();
			filter.append("placeid", review.getPlaceId());
			
			
			// update user ratings 
			Document update = new Document();
			update.append("ratings", Double.parseDouble(review.getRating()));
			update.append("date", review.getDate());
			getMyCollection(review.getUserId()).findOneAndUpdate(filter, new Document("$set",update));
			
			
			
			
			// (2)
			// review collection에 업데이트 
			getMyCollection("review").findOneAndUpdate(
					// filter
					new Document("placeid",review.getPlaceId()).append("userid", review.getUserId()),
					// content
					new Document("$set", update));
			
			// (3)
			// update place collection
			Document reviewDoc = new Document();
			
			reviewDoc.append("ratings", Double.parseDouble(review.getRating()));
			reviewDoc.append("date", review.getDate());
			reviewDoc.append("userid", review.getUserId());
			reviewDoc.append("placeid", review.getPlaceId());
//			reviewDoc.append(key, value);
					
			getMyCollection(codeToCollection(review.getCode())).findOneAndUpdate(
					
					// filter
					new Document("id",review.getPlaceId()).append("review.userid", review.getUserId()),
					// update 
//					new Document("$elemMatch", new Document("review", new Document("userid",review.getUserId())).
					new Document("$set", new Document("review.$.ratings", Double.parseDouble(review.getRating())).append("review.$.date", review.getDate()))
					);
			
			
			Document targetPlace = getMyCollection(codeToCollection(review.getCode())).find(new Document("id", review.getPlaceId())).first();
			
			double ratings = targetPlace.getDouble("ratings");
			int count = targetPlace.getInteger("count");
			
			
			double newRatings = (((ratings * count) - beforeRatings) + Double.parseDouble(review.getRating()) ) /count;
			getMyCollection(codeToCollection(review.getCode())).
			findOneAndUpdate( new Document("id", review.getPlaceId()), new Document("$set", new Document("ratings", newRatings)));
			
			return "1";
		
		}
		// 아직 평가를 하지 않은 경우 
		else{
			/*
			 * 이전에 평가를 안한 경우.
			 */
			
			System.out.println("[SAVE_REVIEW] reviewed not yet");
			Document reviewDoc = new Document();
			
			//error 
			long totalReviewNum = getMyCollection("review").count();
			String reviewId = "cr" + (totalReviewNum+1);
			
			// generate review
			// with new id 
			reviewDoc.append("id", reviewId);
			reviewDoc.append("date", review.getDate());
			reviewDoc.append("userid", review.getUserId());
			reviewDoc.append("placeid", review.getPlaceId());
			reviewDoc.append("code", review.getCode());
			reviewDoc.append("ratings", Double.parseDouble(review.getRating()));
			
			
			getMyCollection("review").insertOne(reviewDoc);
			// update 로 바꿔야함 
			/////////////////////////////////////////////
			
			
			
			reviewDoc.remove("id");
			reviewDoc.append("reviewid", reviewId);
			getMyCollection(review.getUserId()).insertOne(reviewDoc);

			/*
			 * 장소 정보에 평점 갱신 
			 */
			
			MongoCollection<Document> collection = getMyCollection(codeToCollection(review.getCode()));
			Document find = new Document();
			Document update = new Document();
			find.append("id", review.getPlaceId());
			
			// get target place
			// update new Ratings
			
			Document result = getMyCollection(codeToCollection(review.getCode())).find(find).first();
			int currentCount = result.getInteger("count", 1);
			double currentRatings = result.getDouble("ratings");
			
			double newRatings = ((currentRatings*currentCount) + Double.parseDouble(review.getRating())) / (currentCount+1);
			
			update.append("$inc", new Document("count",1));
			update.append("$set", new Document("ratings", newRatings));
			collection.findOneAndUpdate(find, update);
			
			/*
			 * 해당 장소에 리뷰 정보 추가 
			 */
			
			reviewDoc.remove("code");
			update.append("$push", new Document("review", reviewDoc));
			System.out.println(review.getCode());
			
			System.out.println( "colleciton" + collection.toString());
			collection.findOneAndUpdate(new Document("id",review.getPlaceId()), update);
			
			return "1";
		}
		
		
		
		
	}	
	
	public String getReview(String placeId){
		
		Document filter = new Document();
		filter.append("placeid", placeId);
		
		Document result = null;
		result = getMyCollection("review").find(filter).first();
		
		if(result==null){
			System.out.println("no Review in place");
			return "";
		}
		else{
			System.out.println("get review");
			return result.toString();
		}
	}
}



























