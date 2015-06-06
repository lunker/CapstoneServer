package dk.spring.server;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import dk.spring.util.NetWork;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		
		Logger log = Logger.getLogger(Application.class.getName());
		log.info("adf");
		log.error("adf");
		
//		NetWork nw = new NetWork();
//		nw.start();	
		
//		GoogleNetwork gn = new GoogleNetwork();
//		gn.start();
		
//		SetRating sr = new SetRating();
//		sr.start();
		
		
	}
	
}
