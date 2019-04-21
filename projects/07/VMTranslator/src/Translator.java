import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Translator {
	private Map<String,String> commands = new HashMap<String,String>();
	private int count;
	private BufferedReader buffer;
	private BufferedWriter bufferWrite;
	private String functionName;
	private String currFileName;
	
	public Translator (String fileName) throws Exception {
		try 
		{
			bufferWrite = new BufferedWriter (new FileWriter(fileName + ".asm"));
			buffer = new BufferedReader(new FileReader( fileName + ".vm" ));
			parsing();
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parsing() throws Exception {
		int i = 0;
		String line;
		initialize();
		String init = "@256\n" + "D=A\n" + "@SP\n" +
			      "M=D\n" + call("Sys.init", "0");
		bufferWrite.write(init);
		try {
			line = buffer.readLine();
			while ( line != null )
			{
				System.out.println("At line " + ++i + ": " + line);;
				line = normalizingLine(line);
				if (line.length() != 0)
				{				
					line = nextCommand(line);
					bufferWrite.write(line);
				}
				line = buffer.readLine();
			}
			
			closeFile();
		}
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// assuming there is a line
	private String getRidOfWhiteSpace(String line) {
		line = line.replaceAll(" ", "");
		return line;	
	}
	
	//get rid of comments
	private String normalizingLine (String line) {
		line = line.replaceAll("//.*", "").trim();
		return line;
	}
	
	private String nextCommand(String line) throws Exception {
		System.out.println(line); //for debugging
		line = line.trim();
		if ( line == null)
			throw new Exception("The line should not be empty at this point!!");
		switch (line) {
			case "add": return commands.get(line);
			case "sub": return commands.get(line);
			case "neg": return commands.get(line);
			case "and": return commands.get(line);
		    case "or":  return commands.get(line);
		    case "not": return commands.get(line);
		    case "eq":  return equal();
		    case "gt":  return greaterThan();
	      	case "lt":  return lessThan();
	      	case "return":return commands.get(line);
		}
		String[] words = line.split(" ");
		if (words.length == 0) {
			throw new Exception("The line should not be empty at this point!!!");
		}
		switch (words[0]) { // check for the command (first word)
		case "push": 		return push(words[1], words[2]);
        case "pop": 		return pop(words[1], words[2]);
        case "label": 		return "(" + functionName + "$" + words[1] + ")\n";
        case "goto": 		return _goto(words[1]);
        case "if-goto": 	return if_goto(words[1]);
        case "function": { 
        		functionName = words[1];
        					return function(words[1], words[2]); 
        		}
        case "call": 		return call(words[1], words[2]);
        default: throw new Exception(words[0] + " should not exists");
		}
	}
	
	private String greaterThan() {
		String i = Integer.toString(++count);
		String command = "@SP\n" +
						"AM=M-1\n" +
						"D=M\n" +
						"A=A-1\n" +
						"D=M-D\n" +
						"@GT.true." + i + "\n" +
						"\nD;JGT\n" +
						"@SP\n" +
						"A=M-1\n" +
						"M=0\n" +
						"@GT.after." + i + "\n" +
						"0;JMP\n" +
						"(GT.true." + i + ")\n" +
						"@SP\n" +
						"A=M-1\n" +
						"M=-1\n" +
						"(GT.after." + i + ")\n";
		return command;
	}
	
	private String lessThan() {
		String i = Integer.toString(++count);
		String command = "@SP\n" +
						"AM=M-1\n" +
						"D=M\n" +
						"A=A-1\n" +
						"D=M-D\n" +
						"@LT.true." + i + "\n" +
						"D;JLT\n" +
						"@SP\n" +
						"A=M-1\n" +
						"M=0\n" +
						"@LT.after." + i + "\n" +
						"0;JMP\n" +
						"(LT.true." + i + ")\n" +
						"@SP\n" +
						"A=M-1\n" +
						"M=-1\n" +
						"(LT.after." + i + ")\n";
		return command;
	}
	
	private String equal() {
		String i = Integer.toString(++count);
		String command = "@SP\n" +
				"AM=M-1\n" +
				"D=M\n" +
				"A=A-1\n" +
				"D=M-D\n" +
				"@EQ.true." + i + "\n" +
				"D;JEQ\n" +
				"@SP\n" +
				"A=M-1\n" +
				"M=0\n" +
				"@EQ.after." + i + "\n" +
				"0;JMP\n" +
				"(EQ.true." + i + ")\n" +
				"@SP\n" +
				"A=M-1\n" +
				"M=-1\n" +
				"(EQ.after." + i + ")\n";
		return command;
	}
	
	private String _goto(String label) {
	    String s = "@" + functionName + "$" + label + "\n" +
	    			"0;JMP\n";
	    return s;
	}
	
	private String if_goto(String label) {
	    String s = "@SP\n" +
	    			"AM=M-1\n" +
	    			"D=M\n" +
	    			"@" + functionName + "$" + label + "\n" +
	    			"D;JNE\n";
	    return s;
	}
	
	private String pop(String base, String x) throws Exception {
		//System.out.println("Pop: " + base + x);
		switch (base) {
		case "local" :
			return "@LCL\n" + "D=M\n" + "@" + x 
					+ "\n" + "D=D+A\n" + commands.get("pop");
		case "argument" :
			return "@ARG\n" + "D=M\n" + "@" + x 
					+ "\n" + "D=D+A\n" + commands.get("pop");
		case "this" :
			return "@THIS\n" + "D=M\n" + "@" + x 
					+ "\n" + "D=D+A\n" + commands.get("pop");
		case "that" :
			return "@THAT\n" + "D=M\n" + "@" + x 
					+ "\n" + "D=D+A\n" + commands.get("pop");
		case "pointer" : {
			if (x.equals("0"))
		          return "@THIS\n" + "D=A\n" + commands.get("pop");
				else
		          return "@THAT\n" + "D=A\n" + commands.get("pop");
			}
		case "static" :
			return "@" + currFileName + "." + x + "\n" +
			"D=A\n" + commands.get("pop");
		case "temp" :
			return "@R5\n" + "D=A\n" + "@" + x + "\n" +
			"D=D+A\n" + commands.get("pop");
		default: throw new Exception("bad command! " + base + " " + x );
			
		}	
	}
	
	private String push(String base, String x) throws Exception {
		//System.out.println("Push: " + base + x);
		switch (base) {
		case "local" :
			return "@LCL\n" + "D=M\n" + "@" + x 
					+ "\n" + "A=D+A\n" + "D=M\n" + commands.get("push");
		case "argument" :
			return "@ARG\n" + "D=M\n" + "@" + x 
					+ "\n" + "A=D+A\n" + "D=M\n" + commands.get("push");	
		case "this" :
			return "@THIS\n" + "D=M\n" + "@" + x 
					+ "\n" + "A=D+A\n" + "D=M\n" + commands.get("push");
		case "that" :
			return "@THAT\n" + "D=M\n" + "@" + x 
					+ "\n" + "A=D+A\n" + "D=M\n" + commands.get("push");
		case "pointer" : {
			if (x.equals("0"))
		          return "@THIS\n" + "D=M\n" + commands.get("push");
				else
		          return "@THAT\n" + "D=M\n" + commands.get("push");
			}
		case "constant" :
			return "@" + x + "\n" + "D=A\n" + commands.get("push");
		case "static" :
			return "@" + currFileName + "." + x + "\n" +
				"D=M\n" + commands.get("push");
		case "temp" :
			return "@R5\n" + "D=A\n" + "@" + x + "\n" +
	        	"A=D+A\n" + "D=M\n" + commands.get("push");
		default: throw new Exception("bad command!" +" "+ base + " " + x);
		}
	}
	
	private String call(String f, String n) {
		String i = count();
		String returnAddress = String.valueOf(System.currentTimeMillis());
//		String s = // Push return address
//				"@" + returnAddress +"\n" + "D=A\n" + "@SP\n" + "A=M\n" +
//				"M=D" + "@SP\n" + "M=M+1\n" +
//				// Push LCL
//				"@LCL\n" + "D=M\n" + "@SP\n" + "A=M\n" +
//				"M=D" + "@SP\n" + "M=M+1\n" +
//				// Push ARG
//				"@ARG\n" + "D=M\n" + "@SP\n" + "A=M\n" +
//				"M=D" + "@SP\n" + "M=M+1\n" +
//				// Push THIS
//				"@THIS\n" + "D=M\n" + "@SP\n" + "A=M\n" +
//				"M=D" + "@SP\n" + "M=M+1\n" +
//				// Push THAT
//				"@THAT\n" + "D=M\n" + "@SP\n" + "A=M\n" +
//				"M=D" + "@SP\n" + "M=M+1\n" +
//				// ARG = SP - n - 5
//				"@SP\n" + "D=M\n" + "@" + n + "D=D-A\n" + "@5\n" +
//				"D=D-A\n" + "@ARG\n" + "M=D\n" +
//				// LCL = SP
//				"@SP\n" + "D=M\n" + "@LCL\n" + "M=D\n" +
//				// goto
//				"@SP\n" + "M=M+1\n" +  // decrement st ptr
//				"@SP\n" + "A=M\n" + "D=M\n" + // pop
//				"@" + functionName + "\n" + "D;JNE\n" +
//				"(" + returnAddress + ")\n";
	    return 
	      // SP -> R13
	      "@SP\n" + "D=M\n" + "@R13\n" + "M=D\n" +
	      // @RET -> *SP
	      "@RET." + i + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" +
	      // SP++
	      "@SP\n" + "M=M+1\n" +
	      // LCL -> *SP
	      "@LCL\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" +
	      // SP++
	      "@SP\n" + "M=M+1\n" +
	      // ARG -> *SP
	      "@ARG\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" +
	      // SP++
	      "@SP\n" + "M=M+1\n" +
	      // THIS -> *SP
	      "@THIS\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" +
	      // SP++
	      "@SP\n" + "M=M+1\n" +
	      // THAT -> *SP
	      "@THAT\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" +
	      // SP++
	      "@SP\n" + "M=M+1\n" +
	      // R13 - n -> ARG
	      "@R13\n" + "D=M\n" + "@" + n + "\n" + 
	      "D=D-A\n" + "@ARG\n" + "M=D\n" +
	      // SP -> LCL
	      "@SP\n" + "D=M\n" + "@LCL\n" + "M=D\n" +
	      "@" + f + "\n" + "0;JMP\n" + "(RET." + i + ")\n";
	}
	
	private String function(String f, String num) {
	    String s = "(" + f + ")\n";
	    int depth = Integer.parseInt(num);
	    System.out.println("Num Arg: " + depth);
	    for (int i = 0; i < depth; i++) {
	      s += "@SP\n" + "A=M\n" + "M=0\n"
	    		  + "@SP\n" + "M=M+1\n";
	    }
	    //s += "D=A\n" + "@SP\n" + "M=D\n";
	    return s;
	}

	private String count() {
		count = count + 1;
		return Integer.toString(count);
	}
	
	
	private void initialize () {
		count = 0;
		//commands map
		commands.put("add", "@SP\nAM=M-1\nD=M\nA=A-1\nM=D+M\n"); //add
		commands.put("sub", "@SP\nAM=M-1\nD=M\nA=A-1\nM=M-D\n"); //sub
		commands.put("neg", "@SP\nA=M-1\nM=-M\n"); //neg
		commands.put("and", "@SP\nAM=M-1\nD=M\nA=A-1\nM=D&M\n"); //and
		commands.put("or", "@SP\nAM=M-1\nD=M\nA=A-1\nM=D|M\n"); //or
		commands.put("not", "@SP\nA=M-1\nM=!M\n"); //not
		commands.put("push", "@SP\nA=M\nM=D\n@SP\nM=M+1\n"); // push
		commands.put("pop", "@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n"); //pop
		commands.put("return",    
				// Frame == R13
				"@LCL\n" + "D=M\n" + "@R13\n" + "M=D\n" +
				// put return temp
				"@R13\n" + "D=M\n" + "@5\n" + "D=D-A\n" + "A=D\n" + "D=M\n" + "@R14\n" + "M=D\n" +
				// copy return value to D
				"@SP\n" + "A=M\n" + "A=A-1\n" + "D=M\n" +
				"@ARG\n" + "A=M\n" + "M=D\n" +
				// Restore stack pt to caller
				// SP = ARG + 1
				"@ARG\n" + "D=M\n" + "D=D+1\n" + "@SP\n" + "M=D\n" +
				// THAT
				"@R13\n" + "D=M\n" + "@1\n" + "D=D-A\n" + "A=D\n" + "D=M\n" + "@THAT\n" + "M=D\n" +
				// THIS
				"@R13\n" + "D=M\n" + "@2\n" + "D=D-A\n" + "A=D\n" + "D=M\n" + "@THIS\n" + "M=D\n" +
				// ARG
				"@R13\n" + "D=M\n" + "@3\n" + "D=D-A\n" + "A=D\n" + "D=M\n" + "@ARG\n" + "M=D\n" +
				// LCL
				"@R13\n" + "D=M\n" + "@4\n" + "D=D-A\n" + "A=D\n" + "D=M\n" + "@LCL\n" + "M=D\n" +
				// goto RET
				"@R14\n" + "A=M\n" + "0;JMP\n");
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

}
