import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;


public class FileParser {
	
	private HashMap<String, HashMap<String, String>> userRating;
	private HashMap<String, String> mIDLookup;
	
	public FileParser(String ratingsPath, String moviesPath) {
		//userRating = {userID : {movieID:rating}}
		Scanner reader = null;
		try {
			reader = new Scanner(new File(ratingsPath));
			String[] line;
			HashMap<String, String> movieRating;
			userRating = new HashMap<String, HashMap<String, String>>();
			line = reader.nextLine().split(",");
			while (reader.hasNextLine()) {
				line = reader.nextLine().split(",");
				movieRating = new HashMap<String, String>();
				movieRating.put(line[1], line[2]);
				userRating.put(line[0], movieRating);
				
			}
			
			reader = new Scanner(new File(ratingsPath));
			mIDLookup = new HashMap<String, String>();
			line = reader.nextLine().split(",");
			while (reader.hasNextLine()) {
				line = reader.nextLine().split(",");
				mIDLookup.put(line[0], line[1]);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, String> allRatingsByUser(String uID) {
		HashMap<String, String> output = new HashMap<String, String>();
		for (String mID: userRating.get(uID).keySet()) {
			output.put(mIDLookup.get(mID), userRating.get(uID).get(mID));
		}
		return output;
	}
	
	public HashMap<String, String> allRatingsByMovie(String mID){
		HashMap<String, String> output = new HashMap<String, String>();
		for (String uID: userRating.keySet()) {
			if (userRating.get(uID).get(mID) != null) {
				output.put(uID, userRating.get(uID).get(mID));
			}
		}
		return output;
	}
	
	public String userMovieRating(String uID, String movieName) {
		return userRating.get(uID).get((mIDLookup).get(movieName));
	}
	
	private String average(Collection<String> ratings) {
		int sum = 0;
		for (String r: ratings) {
			sum += Integer.parseInt(r);
		}
		return String.valueOf(sum/ratings.size());
	}
	
	public String avgUserRating(String uID) {
		HashMap<String, String> ratedMovies = userRating.get(uID);
		if (ratedMovies != null) {
			Collection<String> ratings = ratedMovies.values();
			return average(ratings);
		}
		return null;
	}
	
	public String avgMovieRating(String movie) {
		ArrayList<String> mRatings = new ArrayList<String>();
		String mID = mIDLookup.get(movie);
		if (mID != null) {
			for (String uID: userRating.keySet()) {
				if (userRating.get(uID).get(mID) != null) {
					mRatings.add(userRating.get(uID).get(mID));
				}
			}
			
			return average(mRatings);
			
		}
		return null;
	}
	
	private String getMID(String mName) {
		for (String id: mIDLookup.keySet()) {
			if (mIDLookup.get(id).equals(mName)){
				return id;
			}
		}
		return null;
	}
	
	private int genMID() {
		int mID = 611;
		while (true) {
			for (String id: mIDLookup.keySet()) {
				if (Integer.valueOf(id) == mID) {
					mID++;
					continue;
				}
			}
			return mID;
		}
	}
	
	public void setUserRating(String uID, String mName, String rating) {
		
		String mID = getMID(mName);
		
		if (mID == null) {
			mID = String.valueOf(genMID());
			mIDLookup.put(mID, mName);
		}
		
		if (userRating.get(uID) != null) {
			userRating.get(uID).put(mID, rating);
		} else {
			HashMap<String, String> mIDRating = new HashMap<String, String>();
			mIDRating.put(mID, rating);
			userRating.put(uID, mIDRating);
		}
	}
	
}
