package dk.spring.util;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import dk.spring.server.Constant;
import dk.spring.server.factory.DBFactory;

public class SetRating extends Thread {

	private DatabaseConnector connector = null;

	@Override
	public void run() {
		// TODO Auto-generated method stub

		connector = DBFactory.getConnector();

		String collection = "";
		for (int i = 0; i < 5; i++) {

			if (i == 0) {
				// food 
				collection = connector.codeToCollection(Constant.DAUM_CODE_CAFE);
			} 
			else if (i == 1) {
//				collection = connector.codeToCollection(Constant.DAUM_CODE_CULTURE);
			} 
			else if (i == 2) {
				collection = connector.codeToCollection(Constant.DAUM_CODE_FOOD);
			} 
			else if (i == 3) {
				collection = connector.codeToCollection(Constant.DAUM_CODE_REST);
			} 
			else if (i == 4) {
				collection = connector.codeToCollection(Constant.DAUM_CODE_TOUR);
			}

			
			MongoCollection<Document> allRows = connector.getMyCollection(collection);
			
			MongoCursor<Document> cursor = allRows.find().iterator();
			Document doc = null;
			Document update = null;
			while(cursor.hasNext()){
				
				doc = cursor.next();
				
				// initialize count
					update = new Document();
					update.append("$set", new Document("count",1));
					
					allRows.findOneAndUpdate(new Document("id",doc.getString("id")), update);
				
				// initialize ratings
				
					
					// 어떤 장소들은 rating으로 입력..ㅠㅠㅠㅠ 
					try{
						if(doc.getDouble("ratings") != 2.5){
							
							update = new Document();
							update.append("$set", new Document("ratings", 2.5));
							allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update );
						}
					} catch(ClassCastException cce){
						cce.printStackTrace();
						
						update = new Document();
						update.append("$set", new Document("ratings", 2.5));
						allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update );
						
					} catch (Exception e) {
						update = new Document();
						update.append("$set", new Document("ratings", 2.5));
						allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update );
						
					}finally{
						;
					}
				
//					allRows.d
				
				
			}
			
		}// end for

	}
}
