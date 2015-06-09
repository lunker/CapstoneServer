package dk.spring.server.controller;

import org.apache.catalina.connector.Request;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.spring.server.factory.DBFactory;
import dk.spring.server.factory.MapperFactory;
import dk.spring.server.model.SaveCategoryModel;
import dk.spring.server.model.UserModel;
import dk.spring.util.DatabaseConnector;


@RestController
public class UserController {

	private DatabaseConnector connector = DBFactory.getConnector();
	private ObjectMapper mapper = MapperFactory.getMapper();
	private Logger logger = Logger.getLogger(UserController.class);
	
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public String signUp(@RequestBody UserModel user){
		System.out.println("signup!");
		System.out.println("[POST][signup]" + user.getEmail());
		
		return connector.saveUser(user);
	}
	
	@RequestMapping(value="/loginn")
	public String login(@RequestParam(value="email", defaultValue="123", required=false) String email,
			@RequestParam(value="password", defaultValue="1", required=false) String password){
		
//		System.out.println("asdfasdf");
		logger.info("in login!");
		System.out.println(email);
		System.out.println(password);
		/*
		ObjectNode root = new ObjectNode(mapper.getNodeFactory());
		JsonNode preferNode = null;
		try {
			 preferNode = mapper.readTree(preferStr);
			 if(preferNode.isArray()){
				 for(int i=0; i<preferNode.size(); i++){
					 JsonNode preferCategory = preferNode.get(i);
				 }
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		String result = "";
		result = connector.login(email, password);
		
		return result;
	}
	
	@RequestMapping(value="/saveCategory", method=RequestMethod.POST)
	public String saveCategory(
			@RequestBody SaveCategoryModel model
			){
		
		if(connector.getMyCollection(model.getUserid())
		.findOneAndUpdate(
				new Document("id", model.getUserid()),
				new Document("$set", new Document("prefercategory", model.getPrefercategory()))
				
				)!=null)
			return "1";
		
		return "0";
	}
	
	
	
		
}






