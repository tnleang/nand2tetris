
import java.io.IOException;

public class main {

	public static void main(String[] args) {
		String fileName = "Pong";
		try {
			Parser p = new Parser(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
