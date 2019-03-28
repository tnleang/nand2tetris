import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;



public class Parser {
	private static final int A_COM = 0;
	private static final int C_COM = 1;
	private static final int L_COM = 2;
	
	private Map<String,Integer> symbols = new HashMap<String,Integer>();
	private Map<String,String> comps = new HashMap<String,String>();
	private Map<String,String> dests = new HashMap<String,String>();
	private Map<String,String> jumps = new HashMap<String,String>();
	private ArrayList<String> instructions = new ArrayList<String>();
	private BufferedReader buffer;
	private BufferedWriter bufferWrite;
	private int addressCount;
	
	public Parser (String fileName) throws IOException {
		try 
		{
			bufferWrite = new BufferedWriter (new FileWriter(fileName + ".hack"));
			buffer = new BufferedReader(new FileReader( fileName + ".asm" ));
			initlize();
			parsing();
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parsing () {
		String line;
		instructions.clear();
		addressCount = 16; //address starts at 16
		try {
			line = buffer.readLine();
			
			while ( line != null )
			{
				line = getRidOfWhiteSpace(line);
				line = normalizingLine (line);
				if (line.length() != 0)
				{
					changeLabelToAddress(line);	
					if (typeOfCommand(line.charAt(0)) != L_COM)
					{
						instructions.add(line);
					}	
				}
				line = buffer.readLine();
				
			}
			for (int i=0; i < instructions.size(); i++)
			{
				String s;
				if (typeOfCommand(instructions.get(i).charAt(0)) == A_COM)
				{
					s = changeSymbolsToAddress(instructions.get(i));
					s = convertToMachine(s);
				}
				else
				{
					s = convertToMachine(instructions.get(i));
				}
				bufferWrite.write(s + "\n");
			}
			
			closeFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	//get rid of comments
	private String normalizingLine (String line) {
		line = getRidOfWhiteSpace(line);
		String[] split = line.split("/");
		return split[0];
	}
	
	// assuming there is a line
	private String getRidOfWhiteSpace(String line) {
		line = line.replaceAll(" ", "");
		return line;	
	}
	
	// assuming there is a line
	// only valid with @
	private String changeSymbolsToAddress (String line) {
		String newLine;
		if (typeOfCommand(line.charAt(0)) != A_COM //if not @ then it is not symbol
			|| Character.isDigit(line.charAt(1)))  //or if it is value not symbol
			return line;
					// if the symbol does not exist, add it in
		if ( !symbols.containsKey(line.substring(1, line.length())) )
		{
			symbols.put(line.substring(1, line.length()), addressCount++);
		}
		//mapping the symbols with value
		int value = symbols.get(line.substring(1, line.length()));
		newLine = Character.toString(line.charAt(0)) + value;
		return newLine;
	}
	
	private void closeFile() {
		try {
			if (buffer != null)
				buffer.close();
			if (bufferWrite != null)
				bufferWrite.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean changeLabelToAddress (String line) {
		if (typeOfCommand(line.charAt(0)) != L_COM) //if not @ then it is not symbol
			return false;
		if (!symbols.containsKey(line.substring(1, line.length() - 1))) 
		{													//current size is current line
			symbols.put(line.substring(1, line.length() - 1), instructions.size()); 
			return true;
		}
		return false;
	}
	
	private String convertToMachine (String line) {
		String machineLine;
		if (typeOfCommand(line.charAt(0)) == A_COM)
		{	
			machineLine = intToBinary(Integer.parseInt(line.substring(1, line.length()), 10),16);
		}	
		else  
		{
			machineLine = "111";
			String[] inst = null;
			String dest = "000", comp, jump = "000"; //default value
			if (line.contains("="))
			{
				inst = line.split("=");
				dest = dests.get(inst[0]);
				line = inst[1];
			}
		    if (line.contains(";"))
			{
				inst = line.split(";");
				comp = comps.get(inst[0]);
				jump = jumps.get(inst[1]);
			}
			else //there is no jump
			{
				comp = comps.get(inst[1]);
				
			}
			machineLine = "111" + comp + dest + jump;
		}
		return machineLine;
	}
	
	private int typeOfCommand(char c) {
		if(c == '@')
			return A_COM;
		else if (c == '(')
			return L_COM;
		else
			return C_COM;
	}
	
	private String intToBinary (int n, int numOfBits) {
		String binary = "";
		for(int i = 0; i < numOfBits; ++i, n/=2) 
		{
		    switch (n % 2) 
		    {
		         case 0:
		            binary = "0" + binary;
		         break;
		         case 1:
		            binary = "1" + binary;
		         break;
		    }
	   }
	   return binary;
	}

	private void initlize () {
		//symbols
	    symbols.put("R0", 0);
	    symbols.put("R1", 1);
	    symbols.put("R2", 2);
	    symbols.put("R3", 3);
	    symbols.put("R4", 4);
	    symbols.put("R5", 5);
	    symbols.put("R6", 6);
	    symbols.put("R7", 7);
	    symbols.put("R8", 8);
	    symbols.put("R9", 9);
	    symbols.put("R10", 10);
	    symbols.put("R11", 11);
	    symbols.put("R12", 12);
	    symbols.put("R13", 13);
	    symbols.put("R14", 14);
	    symbols.put("R15", 15);
	    symbols.put("SCREEN", 16384);
	    symbols.put("KBD", 24576);
	    symbols.put("SP", 0);
	    symbols.put("LCL", 1);
	    symbols.put("ARG", 2);
	    symbols.put("THIS", 3);
	    symbols.put("THAT", 4);
	    
	    //comps
	    comps.put("0", "0101010");
	    comps.put("1", "0111111");
	    comps.put("-1", "0111010");
	    comps.put("D", "0001100");
	    comps.put("A", "0110000");
	    comps.put("M", "1110000");
	    comps.put("!D", "0001101");
	    comps.put("!A", "0110001");
	    comps.put("!M", "1110001");
	    comps.put("-D", "0001111");
	    comps.put("-A", "0110011");
	    comps.put("-M", "1110011");
	    comps.put("D+1", "0011111");
	    comps.put("A+1", "0110111");
	    comps.put("M+1", "1110111");
	    comps.put("D-1", "0001110");
	    comps.put("A-1", "0110010");
	    comps.put("M-1", "1110010");
	    comps.put("D+A", "0000010");
	    comps.put("D+M", "1000010");
	    comps.put("D-A", "0010011");
	    comps.put("D-M", "1010011");
	    comps.put("A-D", "0000111");
	    comps.put("M-D", "1000111");
	    comps.put("D&A", "0000000");
	    comps.put("D&M", "1000000");
	    comps.put("D|A", "0010101");
	    comps.put("D|M", "1010101");
	    
	    //destination registers
	    dests.put(null, "000");
	    dests.put("M", "001");
	    dests.put("D", "010");
	    dests.put("MD", "011");
	    dests.put("A", "100");
	    dests.put("AM", "101");
	    dests.put("AD", "110");
	    dests.put("AMD", "111");
	    
	    //jump
	    jumps.put(null, "000");
	    jumps.put("JGT", "001");
	    jumps.put("JEQ", "010");
	    jumps.put("JGE", "011");
	    jumps.put("JLT", "100");
	    jumps.put("JNE", "101");
	    jumps.put("JLE", "110");
	    jumps.put("JMP", "111"); 
	}

}


