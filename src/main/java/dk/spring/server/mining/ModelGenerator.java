package dk.spring.server.mining;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class ModelGenerator {

	 static Recommender foodRcm = null;
	 static Recommender cafeRcm = null;
	 static Recommender restRcm = null;
	 static Recommender tourRcm = null;
	 static Recommender cultureRcm = null;

	static String PATH = "/home/lunker/csv";

	static String CSV_FOOD = "csv_food.csv";
	static String CSV_CAFE = "csv_cafe.csv";
	static String CSV_REST = "csv_rest.csv";
	static String CSV_TOUR = "csv_tour.csv";
	static String CSV_CULTURE = "csv_culture.csv";

	
	static{
		
		for (int i = 0; i < 5; i++) {
			System.out.println("[MODEL_GENERATOR] initializing . . .");

			switch(i){
				case 0: cafeRcm = generate(PATH+"/"+CSV_CAFE); break;
				case 1: cultureRcm = generate(PATH+"/"+CSV_CULTURE);break;
				case 2: foodRcm = generate(PATH+"/"+CSV_FOOD);break;
				case 3: restRcm = generate(PATH+"/"+CSV_REST);break;
				case 4: tourRcm = generate(PATH+"/"+CSV_TOUR);break;
			
			}
		}// end for 
	}
	
	public static Recommender getFoodRcm() {
		return foodRcm;
	}

	public static Recommender getCafeRcm() {
		return cafeRcm;
	}

	public static Recommender getRestRcm() {
		return restRcm;
	}

	public static Recommender getTourRcm() {
		return tourRcm;
	}

	public static Recommender getCultureRcm() {
		return cultureRcm;
	}

	public void start() {
		for (int i = 0; i < 5; i++) {
			
			switch(i){
			
			case 0: cafeRcm = generate(PATH+"/"+CSV_CAFE); break;
			case 1: cultureRcm = generate(PATH+"/"+CSV_CULTURE);break;
			case 2: foodRcm = generate(PATH+"/"+CSV_FOOD);break;
			case 3: restRcm = generate(PATH+"/"+CSV_REST);break;
			case 4: tourRcm = generate(PATH+"/"+CSV_TOUR);break;
			
			}
		}// end for 
	}// end method

	static Recommender generate(String path) {
		FileDataModel dataModel;

		try {
			dataModel = new FileDataModel(new File(path));
//			DataModel model = dataModel.getDataModel();
			// dataModel의 인자를 getModel로 읽어들려 PearsonCorrelation에 의한 유사도 측정값음
			// similarity에 저장한다.
			UserSimilarity similarity = new PearsonCorrelationSimilarity(
					dataModel);
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(2,
					similarity, dataModel);

			// 추천기 생성.
			Recommender recommender = new GenericUserBasedRecommender(dataModel,
					neighborhood, similarity);
			return recommender;
			// recommender.recommend 에서 앞에 파라미터가 유저 id, 뒤에 파라미터가 추천갯수
			
		} catch(IllegalArgumentException ae){
			
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (Exception e){
			return null;
		}

	}// end method

}
