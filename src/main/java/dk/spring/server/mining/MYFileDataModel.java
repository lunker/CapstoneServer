package dk.spring.server.mining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;


public class MYFileDataModel {
	final String INTER_FILE = "./input.csv";
	FileDataModel dataModel = null;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	
	Hashtable<String,Integer> hashtable;
	
	int itemID = 0;
	
	public MYFileDataModel(File dataFile) throws IOException {
		
		String str = null;
		//csv파일을  장소카테고리(String), 장소에 따른 고유ID(Integer) 형태로 저장할 Hashtable 생성.
		hashtable = new Hashtable<String , Integer>();	
		
		
		bufferedReader = new BufferedReader(new FileReader(dataFile));   
		bufferedWriter = new BufferedWriter(new FileWriter(new File(INTER_FILE)));
		
		// csv 파일을 "," 로 구분하여 각셀을 String arr에 저장
		// arr[0] = UserId,  arr[1] = ItemId, arr[2]= User가 매긴 Item 에 대한 rating
		while((str = bufferedReader.readLine()) != null){
			String arr[] = str.split(",");
			if(str.length() < 3){
				bufferedWriter.write("\n");
				continue;
			}
			if(hashtable.get(arr[1]) == null){
				hashtable.put(arr[1], itemID);	
				arr[1] = ""+itemID;
				itemID++;
			}else{
				arr[1] = "" + hashtable.get(arr[1]);
			}
			bufferedWriter.write("\n"+arr[0]+","+arr[1]+","+arr[2]);
		}
		bufferedWriter.flush();
		bufferedWriter.close();
		bufferedReader.close();
		dataModel = new FileDataModel(new File(INTER_FILE));
	}
	
	public FileDataModel getDataModel() {
		return dataModel;
	}
	public String toString(){
		String str = "";
		Set<String> keys = hashtable.keySet();
		for(String k : keys){
			str += k+":"+hashtable.get(k)+"\n";
		}
		return str;
	}
}
