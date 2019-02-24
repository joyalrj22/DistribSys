import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class FileParser {
	
	private HashMap<String, HashMap<String, String>> userRating;
	
	public FileParser(String ratingsPath, String moviesPath) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(ratingsPath));
			String[] line;
			userRating = new HashMap<String, HashMap<String, String>>();
			while ((line = reader.readNext()) != null) {
				HashMap<String, String> movieRating = new HashMap<String, String>();
				movieRating.put(line[1], line[2]);
				userRating.put(line[0], movieRating);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
