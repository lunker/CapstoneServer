package dk.spring.server.mining;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;


/**
 * @author Lee Dong Kyoo 
 * 
 * Collaborative Filtering을 위하여 주기적으로 
 * CSV파일과 Collaborative Filtering Recommender를 업데이트한다. 
 */
public class ServerClock extends Thread{

	private SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd','HH:mm:ss");
	private int currentHour = 0;
	private int SECOND = 1000; // 1초 
	private int MINUTE = 60000;// 1분 
	private CSVGenerator csvGenerator = null;
	private ModelGenerator modelGenerator = null;
	
	private Logger logger = Logger.getLogger(ServerClock.class);
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		csvGenerator = new CSVGenerator();
		modelGenerator = new ModelGenerator();
		while(true){
			try {
				
				logger.info("[SERVER_CLOCK] " + format.format(Calendar.getInstance().getTime()) + " UPDATE RECOMMENDER");
				
				csvGenerator.start();
				modelGenerator.start();
				
				logger.info("[SERVER_CLOCK] sleep . . .");
				Thread.sleep(8*MINUTE);// 5초에 한번씩 
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		
		
	}
}
