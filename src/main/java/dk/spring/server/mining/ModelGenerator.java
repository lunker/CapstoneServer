package dk.spring.server.mining;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class ModelGenerator {

	private static Recommender foodRcm = null;
	private static Recommender cafeRcm = null;
	private static Recommender restRcm = null;
	private static Recommender tourRcm = null;
	private static Recommender cultureRcm = null;
	
	
	public void start(){
		
		
	}
	
	public Recommender generate(){
		MYFileDataModel dataModel;
		try {
			dataModel = new MYFileDataModel(new File("data/intro2.csv"));
			
			DataModel model = dataModel.getDataModel();

			// dataModel의 인자를 getModel로 읽어들려 PearsonCorrelation에 의한 유사도 측정값음
			// similarity에 저장한다.
			UserSimilarity similarity = new PearsonCorrelationSimilarity(
					dataModel.getDataModel());
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(2,
					similarity, model);

			// 추천기 생성.
			Recommender recommender = new GenericUserBasedRecommender(model,
					neighborhood, similarity);

			// recommender.recommend 에서 앞에 파라미터가 유저 id, 뒤에 파라미터가 추천갯수
			
			// recommender를 이용해서 추천 받는다.. !  
			List<RecommendedItem> recommendations = recommender.recommend(1, 2);

			for (RecommendedItem recommendation : recommendations) {

				System.out.println(recommendation);

			}
			// itemId에 해당하는 장소명을 출력
			System.out.println(dataModel.toString());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}//end method
	
	
	
}
