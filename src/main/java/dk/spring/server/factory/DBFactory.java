package dk.spring.server.factory;

import dk.spring.util.DatabaseConnector;

public class DBFactory {

	static DatabaseConnector connector = null;
	
	static{
		System.out.println("static constructor");
		connector = new DatabaseConnector(); 
		connector.connect();
	}


	public static DatabaseConnector getConnector() {
		if(connector != null)
			return connector;
		else {
			connector = new DatabaseConnector();
			return connector;
		}
			 
	}


	
	
	
	
	
	
	
}
