import java.io.IOException;

public class Main {

	public static void main(String[] args) throws Exception {
		String fileName = "Main";
		try {
			Translator s = new Translator(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
