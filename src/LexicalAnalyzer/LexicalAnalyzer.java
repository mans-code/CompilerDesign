
package LexicalAnalyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.State;

import utils.SysLogger;

public class LexicalAnalyzer {
	public static final int ID_MAX_LEN = 255;
	public static final int TAB_LEN = 4;
	
	private String strFile = null;
	private BufferedReader in = null;
	private char curChar = 0;
	private int curPos = 0;
	private int curLine = 1;
	private int curLinePos = 0;
	private int lastLinePos = 0;		// the position of last line
	private String curToken = null;
	
	public int init(String filename) {
		if (filename == null) {
			SysLogger.err("LexicalAnalyzer. No such file.");
			return -1;
		}
		
		// set the file to be analyzed.
		strFile = filename;
		
		// open the file to be ready to read
		if (openFile() == -1) {
			return -1;			
		}
		curChar = nextChar();
		
		return 0;
	}
	
	private int openFile() {        
		try {
			in = new BufferedReader(new FileReader(strFile));

		} catch (FileNotFoundException e) {
			SysLogger.err("File not found: " + strFile);
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			SysLogger.err(e.getMessage());
			return -1;
		}
		
		return 0;
	}
	
	private char nextChar() {
		int ret = -1;
		
		try {
			ret = in.read();
			
			if (ret == -1) {
				// end of file stream
				return 0;
			}
	    } catch (IOException e) {
			e.printStackTrace();
			SysLogger.err(e.getMessage());
			return 0;
		}
		if (ret == '\t') {
			curPos += TAB_LEN;
			curLinePos += TAB_LEN;
		} else {
			curPos++;
			curLinePos++;
		}
		if (ret == '\r' || ret == '\n') {
			if (ret == '\n') {
				curLine++;
			}
			lastLinePos = curLinePos;
			curLinePos = 0;
		}

        return (char)ret;
	}
	
	private void addChar(char ch) {
		if (curToken == null) {
			curToken = "" + (char) curChar;
		} else {
			curToken += (char) curChar;
		}
	}
	
	private void dump() {
		SysLogger.log("--------------------");
		SysLogger.log("strFile = " + strFile);
		SysLogger.log("curLine = " + curLine + ", " + curLinePos + ", curPos = " + curPos);
		SysLogger.log("curToken = " + curToken);
		SysLogger.log("curChar = " + curChar + ", " + (int)curChar);
		SysLogger.log("--------------------\n");
	}
	
	private int fatalerrorHandler() {
		dump();
		return 0;
	}
	
	private int checkTokenValidation(Token tk) {
		if (tk.token == null) {
			return -1;
		}
		
		if (tk.type == StateMachineDriver.TOKEN_TYPE_ID) {
			if (tk.token.length() > ID_MAX_LEN) {
				String err = "Line: " +  tk.line + ", Column: " + tk.column;
				SysLogger.err(err + ". Identifier length > " + ID_MAX_LEN + ". " + tk.token);
				tk.token = null;
				return -1;
			}
			
			// check the type
			if (StateMachineDriver.ifKeyword(tk.token)) {
				tk.type = StateMachineDriver.TOKEN_TYPE_KEYWORD;
			} else if (StateMachineDriver.ifOperator(tk.token)) {
				tk.type = StateMachineDriver.TOKEN_TYPE_OPERATOR;
			}
		}
		
		if (tk.type == StateMachineDriver.TOKEN_TYPE_INT) {
			try {
				Integer.parseInt(tk.token);
			} catch (NumberFormatException e) {				
				//e.printStackTrace();
				String err = "Line: " +  tk.line + ", Column: " + tk.column;
				SysLogger.err(err + ". Integer is too big. " + tk.token);
				tk.token = null;
				return -1;
			}
		}
		
		if (tk.type == StateMachineDriver.TOKEN_TYPE_FLOAT) {
			try {
				Float.parseFloat(tk.token);
			} catch (NumberFormatException e) {				
				//e.printStackTrace();
				String err = "Line: " +  tk.line + ", Column: " + tk.column;
				SysLogger.err(err + ". Float is too big. " + tk.token);
				tk.token = null;
				return -1;
			}
		}
		
		
		return 0;
	}

	// get next valid token from the stream.
	public Token nextTokenEx() {
		int curState = StateMachineDriver.INIT_STATE;
		Token tk = new Token();
		boolean bExit = false;
		
		tk.error = false;
		while (!bExit) {
			if (curChar == 0) {
				// before exit, adding a '\n' to the end of file.
				bExit = true;
				curChar = '\n';
				curLinePos++;
				if( (curState  == 28 || curState == 29 )) {
					curToken = curToken.replace("\r\n", " ");
					String err = "Line: " +  curLine + ", column: " + curLinePos;
					
					SysLogger.err(err + "Multi-Line comment without */"+ "." );
					
				}
				//return null;		// exit;
			}

			if (SysLogger.bLexicalAnalyzer) {
				SysLogger.log("Line(" + curLine + "," + curLinePos + ")curstate: " + curState 
						+ ", " + curChar + ", " + (int)curChar);
			}
			curState = StateMachineDriver.nextState(curState, curChar, tk);
			
			if (SysLogger.bLexicalAnalyzer) {
				SysLogger.log("next state: " + curState);
			}

			// final state without backing up
			if (curState == StateMachineDriver.N) {
				addChar((char) curChar);
				if (!bExit) {
					curChar = nextChar();
				}
				break;
			}
			// back up
			if (curState == StateMachineDriver.B) {
				break;
			}

			// error
			if (curState == StateMachineDriver.E) {
				SysLogger.err("Fatal error. State machine goes to wrong state. ");	
				fatalerrorHandler();
				tk = null;
				return tk;
			}
			// find an invalid character
			if (curState == StateMachineDriver.EC) {
				String err = "Line: " +  curLine + ", column: " + curLinePos;
				
				SysLogger.err(err + ". Unknown character " + curChar + " (ASCII: " + (int)curChar + ").");
				
				// skip to next char
				if (!bExit) {
					curChar = nextChar();
				}
				
				// still output the part of token has been analyzed
				if (curToken != null) {
					tk.error = true;
					break;
				}
				
				curState = StateMachineDriver.INIT_STATE;
				continue;
			}
			// find a valid but unexpected character
			if (curState == StateMachineDriver.ES) {
				String err = "Line: " +  curLine + ", column: " + curLinePos;
				
				SysLogger.err(err + ". Unexpected character " + curChar + " (ASCII: " 
					+ (int)curChar + "), when analyzing " + curToken);
				curState = StateMachineDriver.INIT_STATE;
			
	
				if (curToken != null) {
					tk.error = true;
					break;
				}
			}
			
			if (curState != StateMachineDriver.INIT_STATE) {
				addChar((char) curChar);
			}
			if (!bExit) {
				curChar = nextChar();
			}
		}
		

		if (curToken == null) {
			if (!bExit) {
				SysLogger.err("Fatal error. curToken == null");		// last line
			}
			return null;
		}

		
			tk.token = curToken.replace("\r\n", " ");
			curToken = null;
			tk.file = strFile;
			tk.line = curChar == '\n' ? curLine - 1 : curLine;
			
			if(tk.type == 7){
				int a = (curLinePos == 0 ? lastLinePos : curLinePos) - tk.token.length();
				if(a < 0){
					tk.line = 1 ;
					tk.token = tk.token.replace("\r\n", " ");
					tk.column = 1 ;
				}else{
					tk.column = (curLinePos == 0 ? lastLinePos : curLinePos) - tk.token.length();
				}
			}else{
				tk.column = (curLinePos == 0 ? lastLinePos : curLinePos) - tk.token.length();
			}
			if (tk.error) {
				tk.column--;		// nextChar was called in errorHandler()			
			}
	
			if (checkTokenValidation(tk) == -1) {
				//return tk;		// exit only when tk.token = null;
			}		

			return tk;
	}
	
	// modifications according to the requirements of Ass2.
	public Token nextToken() {
		Token tk = nextTokenEx();
		
		if (tk == null || tk.token == null) {
			return tk;
		}
		
		if (tk.type == StateMachineDriver.TOKEN_TYPE_OPERATOR) {
			if (tk.token.equals("=")) {
				tk.type = StateMachineDriver.TOKEN_TYPE_OP_ASS;
			} else if (tk.token.equals("+") || tk.token.equals("-") 
					|| tk.token.equals("or")) {
				tk.type = StateMachineDriver.TOKEN_TYPE_OP_ADD;
			} else if (tk.token.equals("*") || tk.token.equals("/") 
					|| tk.token.equals("and")) {
				tk.type = StateMachineDriver.TOKEN_TYPE_OP_MUL;
			} else if (tk.token.equals("==") || tk.token.equals("<>") 
					|| tk.token.equals("<") || tk.token.equals(">")
					|| tk.token.equals("<=") || tk.token.equals(">=")) {
				tk.type = StateMachineDriver.TOKEN_TYPE_OP_REL;
			}
//		} else if (tk.type == StateMachineDriver.TOKEN_TYPE_INT) {
//			if (tk.token.equals("0")) {
//				tk.type = StateMachineDriver.TOKEN_TYPE_FLOAT;	// Just for Ass2.
//			}
		}
		
		return tk;
	}
	
	public void printToken(Token tk) {
		if (tk == null || (tk != null && tk.token == null)) {
			return;
		}
		
		String msg = String.format("Line: %4d,\tCol: %3d,\tType: %11s", tk.line, tk.column, 
			StateMachineDriver.TOKEN_STR_TYPE[tk.type]);
//		msg = "Line: " + tk.line + ", Column: " + tk.column + ", Type: " 
//			+ StateMachineDriver.TOKEN_STR_TYPE[tk.type];
		
		if (tk.error) {
			msg += ", Invoke Error Recovery";
		}
		if(tk.type == 7){
			tk.token.replace("\n", " ");
		}
		msg += ",\tLexeme/Value: " + tk.token;
		
		SysLogger.info(msg);
	}
	
	public int getAllTokens() {
		Token tk = nextToken();
		
		while (tk != null) {	
			printToken(tk);
			tk = nextToken();
		}
		return 0;
	}
	
}
