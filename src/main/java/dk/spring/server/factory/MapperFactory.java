package dk.spring.server.factory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapperFactory {
	
	static ObjectMapper mapper = null;
	
	static{
		mapper = new ObjectMapper();
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}


	
	
	
}
