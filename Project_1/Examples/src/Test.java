import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Test {
	public static void main(String... args) throws IOException {
		File myFile = new File("/home/kennemat/Documents/CIS 457/test");
		System.out.println(myFile.getAbsolutePath());
		BufferedReader br = new BufferedReader(new FileReader(myFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          System.out.println("Line: " + line);
        }
        br.close();
	}
}
