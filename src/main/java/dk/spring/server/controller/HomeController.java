package dk.spring.server.controller;


import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dk.spring.server.factory.DBFactory;
import dk.spring.util.DatabaseConnector;

/**
 * Handles requests for the application home page.
 */


@RestController
public class HomeController {
	
	private static final Logger logger = Logger.getLogger(HomeController.class);
	private DatabaseConnector connector = DBFactory.getConnector();
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
//		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	@RequestMapping("/test")
	public String test( @RequestParam(value="name", required=false, defaultValue="Hi")String name){
		System.out.println("ododododod");
		return "asdf";
	}
	
	
}








