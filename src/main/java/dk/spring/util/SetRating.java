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
				collection = connector.codeToCollection(Constant.DAUM_CODE_CULTURE);
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
				
					try{
						// ratings가 없을경
						if(doc.getDouble("ratings")==null){
							
							System.out.println("[UPDATE DB] ratings");
							update = new Document();
							update.append("$set", new Document("ratings", 2.5));
							
							allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update);
						}
						
						// count가 없을경
						if(doc.getInteger("count")==null){
							System.out.println("[UPDATE DB] count");
							
							
							update = new Document();
							update.append("$set", new Document("count",1));
							
							allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update);
						}
						
					} catch(ClassCastException cce){
						cce.printStackTrace();
						
//						update = new Document();
//						update.append("$set", new Document("ratings", 2.5));
//						allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update );
						
					} catch (Exception e) {
						e.printStackTrace();
//						update = new Document();
//						update.append("$set", new Document("ratings", 2.5));
//						allRows.findOneAndUpdate(new Document("id", doc.getString("id")), update );
						
					}finally{
						;
					}
			}
			
		}// end for

	}
}
