package dk.spring.server.mining;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class ServerClock extends Thread{

	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	private int currentHour = 0;
	private int SECOND = 1000;
	private int MINUTE = 10000;
	private CSVGenerator csvGenerator = null;
	private Logger logger = Logger.getLogger(ServerClock.class);
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
		System.out.println(format.format(Calendar.getInstance().getTime()));
		String time = format.format(Calendar.getInstance().getTime());
		
		String[] times = time.split(":");
		
		currentHour = Integer.parseInt(times[0]);
		csvGenerator = new CSVGenerator();
		while(true){
			try {
				csvGenerator.start();
//				System.out.println("[SERVER_CLOCK] sleep . . .");
				logger.info("[SERVER_CLOCK] sleep . . .");
				Thread.sleep(60*MINUTE);
				
				time = format.format(Calendar.getInstance().getTime());
				
//				System.out.println("[SERVER_CLOCK] " + time + " generate csv");
				logger.info("[SERVER_CLOCK] " + time + " generate csv");
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

		
		
		
	}
}
