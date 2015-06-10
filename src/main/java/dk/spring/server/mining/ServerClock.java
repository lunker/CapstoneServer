package dk.spring.server.mining;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

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
				
//				System.out.println("[SERVER_CLOCK] sleep . . .");
				logger.info("[SERVER_CLOCK] sleep . . .");
				Thread.sleep(1*MINUTE);// 2분에 한번씩 
				
//				time = format.format(Calendar.getInstance().getTime());
				
//				System.out.println("[SERVER_CLOCK] " + time + " generate csv");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		
		
	}
}
