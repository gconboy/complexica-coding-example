package example;

public class Restaurant {

	private double Latitude;
	private double Longitude;
	private String Title;
	private String Address;
	
	public Restaurant() {}

	@Override public String toString() {
	    return String.format("%s", Title);	
	}
	
	public double getLatitude() {
		return Latitude;
	}

	public void setLatitude(double latitude) {
		Latitude = latitude;
	}

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}
}
