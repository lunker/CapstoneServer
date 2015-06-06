package dk.spring.server.mining;

import java.io.File;
import java.util.List;

import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class UserRecomm {

	public static void main(String[] args) throws Exception {

		// data폴더안에 있는 intro2.csv파일을 읽어들여 새로운 객체로 저장한다.
		MYFileDataModel dataModel = new MYFileDataModel(new File(
				"data/intro2.csv"));
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
	}
}
