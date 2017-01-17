package example;

import java.util.Comparator;

public class RestaurantTitleComparator implements Comparator<Restaurant> {

	public static final RestaurantTitleComparator DEFAULT = new RestaurantTitleComparator();
	
	@Override
	public int compare(Restaurant r1, Restaurant r2) {
		return r1.getTitle().compareTo(r2.getTitle());
	}

}
