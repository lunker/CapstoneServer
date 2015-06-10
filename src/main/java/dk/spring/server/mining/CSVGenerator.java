package dk.spring.server.mining;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoCursor;

import dk.spring.server.factory.DBFactory;
import dk.spring.util.DatabaseConnector;


public class CSVGenerator {

	
	private String PATH="/home/lunker/csv";
	
	private String CSV_FOOD = "csv_food.csv";
	private String CSV_CAFE = "csv_cafe.csv";
	private String CSV_REST = "csv_rest.csv";
	private String CSV_TOUR = "csv_tour.csv";
	private String CSV_CULTURE = "csv_culture.csv";
	
	
	private File fFood = null;
	private File fCafe = null;
	private File fRest = null;
	private File fTour = null;
	private File fCulture = null;
	
	private FileOutputStream fosFood = null;
	private FileOutputStream fosCafe = null;
	private FileOutputStream fosRest = null;
	private FileOutputStream fosTour = null;
	private FileOutputStream fosCulture = null;
	
	
	private DatabaseConnector connector = DBFactory.getConnector();
	private Logger logger = Logger.getLogger(CSVGenerator.class.getName());
	
	public void start(){
		logger.info("[CSV_GENERATOR] start generate");
		fileOpen();
		MongoCursor<Document> allReviews = connector.getMyCollection("review").find().iterator();
		Document review = null;
		String content = "";
		
		int userid = 0;
		long placeid = 0;
		double ratings = 0.0;
		
		while(allReviews.hasNext()){
			
			review = allReviews.next();
//			content+=review.getString("userid")+","+review.getString("placeid")+","+review.get
			
			userid = Integer.parseInt(review.getString("userid").substring(1));
			placeid = Long.parseLong(review.getString("placeid"));
			ratings = review.getDouble("ratings");
			content = userid+","+placeid+","+ratings+"\n";
			
			// 식당 
			if(review.getString("code").equals("FD6")){
				try {
					fosFood.write(content.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 카페 
			else if(review.getString("code").equals("CE7")){
				try {
					fosCafe.write(content.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 숙박 
			else if(review.getString("code").equals("AD5")){
				try {
					fosRest.write(content.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 관광지 
			else if(review.getString("code").equals("AT4")){
				try {
					fosTour.write(content.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 문
			else{
				try {
					fosCulture.write(content.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			content="";
			
		}// end while
	}// end method 
	
	public void fileOpen(){
		logger.info("[CSV_GENERATOR] file open");
		try {
			
			fFood = new File(PATH+"/"+CSV_FOOD);
			fCafe = new File(PATH+"/"+CSV_CAFE);
			fCulture = new File(PATH+"/"+CSV_CULTURE);
			fRest = new File(PATH+"/"+CSV_REST);
			fTour = new File(PATH+"/"+CSV_TOUR);
			
			try {
				if(!fFood.exists())
					fFood.createNewFile();
				if(!fCafe.exists())
					fCafe.createNewFile();
				if(!fCulture.exists())
					fCulture.createNewFile();
				if(!fRest.exists())
					fRest.createNewFile();
				if(!fTour.exists())
					fTour.createNewFile();
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fosFood = new FileOutputStream(fFood);
			fosCafe = new FileOutputStream(fCafe);
			fosCulture = new FileOutputStream(fCulture);
			fosRest = new FileOutputStream(fRest);
			fosTour = new FileOutputStream(fTour);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// end method 
	
}
