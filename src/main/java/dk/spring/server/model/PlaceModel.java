package dk.spring.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlaceModel {

	/*
	 * "phone":"02-749-5507",
	 * "newAddress":"서울 용산구 이촌로65가길 72",
	 * "imageUrl":"",
	 * "direction":"동",
	 * "zipcode":"",
	 * "placeUrl":"http://place.map.daum.net/8056170",
	 * "id":"8056170",
	 * "title":"스마일이촌떡볶이",
	 * "category":"음식점 > 분식 > 떡볶이",
	 * "distance":"152",
	 * "address":"서울 용산구 이촌동 301-18 동인상가 102호",
	 * "longitude":"126.97497308501626",
	 * "latitude":"37.52087070880758",
	 * "addressBCode":"1117012900"
	 */
	
	private String phone;//
	private String newAddress;//
	private String imageUrl;//
	
	@JsonIgnore
	private String direction;//
	@JsonIgnore
	private String zipcode;//
	private String placeUrl;//
	private String id;//
	private String title;//
	private String category;//
	@JsonIgnore
	private String distance;//
	private String address;//
	private String longitude;//
	private String latitude;//
	private String addressBCode;//
	
	// Max :5.0
	// Default : 2.5 
	@JsonIgnore
	private String rating;
	
	@JsonIgnore
	private int count;
	private String code;
	@JsonIgnore
	private String[] reviewedUser;
	
	
	public PlaceModel(){
		
	}
	
	@JsonCreator
	public PlaceModel(String phone, String newAddress, String imageUrl,
			String direction, String zipcode, String placeUrl, String id,
			String title, String category, String distance, String address,
			String longitude, String latitude, String addressBCode, String code) {
		
		super();
		this.phone = phone;
		this.newAddress = newAddress;
		this.imageUrl = imageUrl;
		this.direction = direction;
		this.zipcode = zipcode;
		this.placeUrl = placeUrl;
		this.id = id;
		this.title = title;
		this.category = category;
		this.distance = distance;
		this.address = address;
		this.longitude = longitude;
		this.latitude = latitude;
		this.addressBCode = addressBCode;
		this.code = code;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getNewAddress() {
		return newAddress;
	}
	public void setNewAddress(String newAddress) {
		this.newAddress = newAddress;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getPlaceUrl() {
		return placeUrl;
	}
	public void setPlaceUrl(String placeUrl) {
		this.placeUrl = placeUrl;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getAddressBCode() {
		return addressBCode;
	}
	public void setAddressBCode(String addressBCode) {
		this.addressBCode = addressBCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
	
	
}