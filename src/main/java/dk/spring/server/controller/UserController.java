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


/***
 * 
 * 
 * @author Lee Dong Kyoo
 *
 * 사용자 정보와 관련된 요청을 처리한다. 
 */
@RestController
public class UserController {

	private DatabaseConnector connector = DBFactory.getConnector();
	private ObjectMapper mapper = MapperFactory.getMapper();
	private Logger logger = Logger.getLogger(UserController.class);
	
	
	/***
	 * 
	 * @param user
	 * @return userid  || "0"
	 * 
	 * 회원가입 
	 */
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public String signUp(@RequestBody UserModel user){
//		System.out.println("signup!");
//		System.out.println("[POST][signup]" + user.getEmail());
		String result = "";
		
		result = connector.saveUser(user);
		
		if(result.equals("0"))
			logger.info("[SIGN_UP] FAIL");
		else
			logger.info("[SIGN_UP] SUCCESS");
		
		return result;
	}
	
	/***
	 * 
	 * @param email
	 * @param password
	 * @return "1" -> success, "0"-> fail
	 * 
	 * 로그인 
	 */
	@RequestMapping(value="/loginn")
	public String login(@RequestParam(value="email", defaultValue="123", required=false) String email,
			@RequestParam(value="password", defaultValue="1", required=false) String password){
		
		String result = "";
		result = connector.login(email, password);
		
		if(result.equals("0"))
			logger.info("[LOGIN] SUCCESS");
		else
			logger.info("[LOGIN] FAIL");
		return result;
	}
	
	
	/***
	 * 
	 * @param model
	 * @return "1" -> success, "0" -> fail
	 * 
	 * 사용자 선호 카테고리 수정 
	 */
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






