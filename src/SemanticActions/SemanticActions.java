
package SemanticActions;

import java.util.ArrayList;

import LexicalAnalyzer.Token;
import SemanticActions.Symbol.SYMBOLTYPE;
import utils.SysLogger;

public class SemanticActions {
	private SymbolTable stHead = null;		// head of symbol table list;
	private SymbolTable stBak = null;// TODO this is has to fix		// only used to check global function declarations 
	private SymbolTable stCur = null;		// current symbol table;
	private static String floatMask = "1000";
	private ASMCode asmCode ;
	

	
	// symbol table manipulation functions
	SymbolTable create(Symbol curSymbol) {
		SymbolTable st = new SymbolTable();
		st.parent = curSymbol;
		if (curSymbol == null) {
			st.addrPrefix = "G";
		} else {
			st.addrPrefix = curSymbol.self.addrPrefix + "_" + curSymbol.tk.token;
		}
		return st;
	}

	// obsolete, see the action functions defined below 
	boolean search(SymbolTable st, String i, Symbol s) {
		return true;
	}
	// obsolete, see the action functions defined below 
	int insert(SymbolTable st, String i, Symbol s) {
		return 0;
	}
	// obsolete, see the action functions defined below 
	void delete(SymbolTable st) {
		// do nothing;
	}

	// print out information 
	void printSymbol(Symbol s) {
		if (s == null) {
			return;
		}
		String str = "Token: " + s.tk.token + "\nSymbol Type: " + s.symbolType.toString() +
			"\nDataType: " + s.dataType.token + "\nAddress: " + s.address + "\nSize: " + s.size + "\n"; 
		if (s.isArray) {
			str += "Dimensions: " + s.dimensions + "\nSizeOfDimensions: ";
			for (int i = 0; i < s.sizeOfDimension.size(); i++) {
				str += "[" + s.sizeOfDimension.get(i) + "]";
			}
			str += "\n";
		}
		if (s.ifAlreadyDefined) {
			str += "ifAlreadyDefined: true\n";
		}
		if (s.ifUnkownDataType) {
			str += "ifUnknownType: true\n";
		}
		 
		SysLogger.info(str);
	}
	void print(SymbolTable st) {
		if (st == null) {
			return;
		}
		String str = "--Begin Symbol Table -- Parent Symbol: ";
		if (st.parent != null && st.parent.tk != null) {
			str += st.parent.dataType.token + " " + st.parent.tk.token;
		} else {
			str += "null";
		}
		SysLogger.info(str);
		
		for (int i = 0; i < st.symbols.size(); i++) {
			Symbol s = st.symbols.get(i);
			printSymbol(s);
		}
		str = "--End--\n";
		SysLogger.info(str);
		
		for (int i = 0; i < st.symbols.size(); i++) {
			Symbol s = st.symbols.get(i);
			if (s.child != null) {
				print(s.child);
			}
		}
	}
	public void printAll() {
		print(stHead);
	}
	
	private void printErr(Symbol s, String msg) {
		String str = String.format("Semantic error   at line: %4d, col: %4d, Token:%16s, %s", 
				s.tk.line, s.tk.column, s.tk.token, msg);
		SysLogger.err(str);	
	}
	private void printErr(Symbol s, String token, String msg) {
		String str = String.format("Semantic error   at line: %4d, col: %4d, Token:%16s, %s", 
				s.tk.line, s.tk.column, token, msg);
		SysLogger.err(str);	
	}
	private void printWarn(Symbol s, String msg) {
		String str = String.format("Semantic warning at line: %4d, col: %4d, Token:%16s, %s", 
				s.tk.line, s.tk.column, s.tk.token, msg);
		SysLogger.err(str);	
	}
	private void printLog(Symbol s, String msg) {
		String str = String.format("Semantic error   at line: %4d, col: %4d, Token:%16s, %s", 
				s.tk.line, s.tk.column, s.tk.token, msg);
		SysLogger.log(str);	
	}
	

	// actions
	
	// create the first symbol table
	public int newProg() {
		stHead = create(null);
		stCur = stHead;
		return 0;
	}
	
	// check if the ID has been defined
	public boolean ifReDefined(Symbol s) {
		if (s.symbolType == SYMBOLTYPE.CLASS) {
			// within the same scope, if there is another class with same name
			for (int i = 0; i < s.self.symbols.size(); i++) {
				Symbol tmp = s.self.symbols.get(i);
				if (tmp.symbolType == SYMBOLTYPE.CLASS && tmp.tk.token.equals(s.tk.token)) {//TODO we may have a var and class with the same name
					return true;
				}
			}
		} else if (s.symbolType == SYMBOLTYPE.VARIABLE) {
			// within the same scope, if there is another variable with same name
			for (int i = 0; i < s.self.symbols.size(); i++) {
				Symbol tmp = s.self.symbols.get(i);
				if (tmp.symbolType == SYMBOLTYPE.VARIABLE && tmp.tk.token.equals(s.tk.token)) {
					return true;
				}
			}
		} 
		
		return false;
	}
	
	// add a new class
	public int newClass(Symbol s) {
		SysLogger.log("newClass: " + s.tk.token);
		s.symbolType = SYMBOLTYPE.CLASS;
		s.self = stCur;
		s.address = s.self.addrPrefix + "_C_" + s.tk.token + "_" + s.tk.line;
		
		// check if the identification has been defined before adding to list
		if (ifReDefined(s)) {
			printErr(s, "Class redefinition.");
			//printErr(s, "Multiply declared identifier.");
			//return -1;
			s.ifAlreadyDefined = true;		// still add into the list
		}
		s.child = create(s);
		stCur.symbols.add(s);		
		stCur = s.child;
		
		//asmCode.asmData(s.address, "dw", "0", "", ""); 
		return 0;
	}
	
	public int setClassSize(Symbol s) {
		for (int i = 0; i < stCur.symbols.size(); i++) {
			Symbol tmp = stCur.symbols.get(i);
			if (tmp.symbolType == SYMBOLTYPE.VARIABLE) {
				if (tmp.isArray) {
					s.size += getArrayTotalSize(tmp);
				} else {
					s.size += tmp.size;
				}
			}
		}
		return 0;
	}

	public  int getArrayTotalSize(Symbol s) {//TODO does not make sense at all!!!!
		int arrSize = 1;
		
		for (int j = 0; j < s.sizeOfDimension.size(); j++) {
			arrSize *= s.sizeOfDimension.get(j);
		}
		return arrSize * s.size;
	}
	
	// exit a class or function definition
	public int exitCurSymbolTable() {
		if (stCur.parent == null || stCur.parent.self == null) {
			SysLogger.err("Cannot exit a symbol table whose parent is null.");
			return -1;
		}
		SysLogger.log("exitCurSymbolTable: " + stCur.parent.tk.token);
		stCur = stCur.parent.self;
		return 0;
	}
	
	// check if the data type is defined
	private boolean ifDataTypeDefined(Token tk) {
		if (stBak == null) {//TODO What (we can search through head) 
			return false;
		}
		if (tk.token.equals("real") || tk.token.equals("integer")) {//TODO this should be first 
			return true;
		}
		for (int i = 0; i < stBak.symbols.size(); i++) {//TODO this means if a var or a func can have the type of its class or or class decarler after the func or var
			Symbol tmp = stBak.symbols.get(i);
			if (tmp.symbolType == SYMBOLTYPE.CLASS && tmp.tk.token.equals(tk.token)) {
				return true;
			}
		}
		return false;
	}
	public boolean ifDataTypeDefined(Symbol s) {
		if (ifDataTypeDefined(s.tk)) {
			return true;
		}
		printErr(s, "Undeclared type.");
		return false;
	}
	
	// add a new function
	public int newFunction(Symbol s) {
		SysLogger.log("newFunction: " + s.tk.token);
		s.symbolType = SYMBOLTYPE.FUNCTION;
		s.self = stCur;
		s.address = s.self.addrPrefix + "_F_" + s.tk.token + "_" + s.tk.line;
		s.child = create(s);
		
		if (!ifDataTypeDefined(s.dataType)) {//TODO this does not work 
			s.ifUnkownDataType = true;
		}
		stCur.symbols.add(s);
		//TO stCur should point to the function table sthead to the gloable table and stbac to class that the function memebr of 
		stCur = s.child;
		if (!s.ifAlreadyDefined && !s.ifUnkownDataType) {
			asmCode.asmFuncDefinition(s);//TODO should we wait until all defintion
		}
		s.size = 4;//TODO what the fuck  
		return 0;
	}
	
	// check if the function is redefined
	// s is the last parameter of this function or the function identification
	public boolean ifFuncRedefined(Symbol s) {
		boolean ret = false;		// no redefinition
		
		// within the parent scope, if there is another function with same name, type and parameters
		if (s.symbolType == SYMBOLTYPE.PARAMETER) {
			Symbol sp = s.self.parent;		// get this function symbol
			
			// same name and type
			for (int i = 0; i < sp.self.symbols.size() - 1; i++) {// -1 means to look before this function
				if (ret) {
					break;
				}
				
				Symbol tmp = sp.self.symbols.get(i);
				if (tmp.symbolType == SYMBOLTYPE.FUNCTION && tmp.tk.token.equals(sp.tk.token)
						&& tmp.dataType.token.equals(sp.dataType.token)) {
					
					// check all parameters
					int paramCnt = tmp.child.symbols.size();
					ret = true;
					for (int j = 0; j < paramCnt; j++) {
						Symbol p = tmp.child.symbols.get(j);
						
						if (p.symbolType == SYMBOLTYPE.PARAMETER) {							
							if (sp.child.symbols.size() > j) {
								if (sp.child.symbols.get(j).symbolType != SYMBOLTYPE.PARAMETER) {
									ret = false;
									break;
								}
								if (!sp.child.symbols.get(j).dataType.token.equals(p.dataType.token)) {
									ret = false;
									break;
								}
							} else {
								ret = false;
								break;
							}
						} else {
							// the other function has at least one more parameter
							if (sp.child.symbols.size() > j) {
								if (sp.child.symbols.get(j).symbolType == SYMBOLTYPE.PARAMETER) {
									ret = false;
								}
							}
							break;
						}
					}
					// the other function has at least one more parameter
					if (sp.child.symbols.size() > paramCnt) { // TODO it can be delelted (no we shouldn't it is for worse case senerior )
						if (sp.child.symbols.get(paramCnt).symbolType == SYMBOLTYPE.PARAMETER) {
							ret = false;
						}
					}
				}
			}
		} else if (s.symbolType == SYMBOLTYPE.FUNCTION) {//TODO why do we need this 
			for (int i = 0; i < s.self.symbols.size() - 1; i++) {
				Symbol tmp = s.self.symbols.get(i);
				
				if (tmp.symbolType == SYMBOLTYPE.FUNCTION && tmp.tk.token.equals(s.tk.token) 
						&& tmp.dataType.token.equals(s.dataType.token)) {
					ret = true;
					break;
				}
			}
			
		} else {
			printErr(s, "Action error while checking function redifinition.");
		}
		
		if (ret) {
			String token = "";
			
			if (s.symbolType == SYMBOLTYPE.PARAMETER) {
				token = s.self.parent.tk.token;
				s.self.parent.ifAlreadyDefined = true;
			} else {
				token = s.tk.token;
				s.ifAlreadyDefined = true;
			}
			printErr(s, token, "Function redefinition.");
			//printErr(s, token, "Multiply declared identifier.");
		}
		asmCode.asmPopFunctionParams(s);
		return ret;
	}
	
	// add a new variable for the function
	public int newVarible(Symbol s) {
		SysLogger.log("addVar: " + s.tk.token);
		if (s.symbolType != SYMBOLTYPE.PARAMETER) {
			s.symbolType = SYMBOLTYPE.VARIABLE;
		}
		s.self = stCur;
		s.address = s.self.addrPrefix + "_V_" + s.tk.token + "_" + s.tk.line;
		
		// check if the identification has been defined before adding to list
		if (ifReDefined(s)) {
			printErr(s, "Variable redefinition.");
			//printErr(s, "Multiply declared identifier.");
			s.ifAlreadyDefined = true;
		}
		if (!ifDataTypeDefined(s.dataType)) {
			s.ifUnkownDataType = true;
		}else{
			s.ifUnkownDataType = false;// it does not matter because we only generate err in the second round
			}
		
		// set the size
		if (s.dataType.token.equals("integer") || s.dataType.token.equals("real")) {//TODO has to be fix for real num
			s.size = 4;  
		} else {
			boolean findit = false;
			for (int i = 0; i < stHead.symbols.size(); i++) {
				Symbol tmp = stHead.symbols.get(i);
				if (tmp.symbolType == SYMBOLTYPE.CLASS && tmp.tk.token.equals(s.dataType.token)) {
					s.size = tmp.size;
					findit = true;
					break;
				}
			}
			if (!findit && stBak != null) {
				for (int i = 0; i < stBak.symbols.size(); i++) {
					Symbol tmp = stBak.symbols.get(i);
					if (tmp.symbolType == SYMBOLTYPE.CLASS && tmp.tk.token.equals(s.dataType.token)) {
						s.size = tmp.size;
						break;
					}
				}
			}
		}
	
		// generate ASM code
		if (!s.ifAlreadyDefined && !s.ifUnkownDataType) {
			asmCode.asmVarDefinition(s);
		}
		
		stCur.symbols.add(s);
		return 0;
	}

	// check if the variable has been defined
	// get the data type of s as well, if it is a function, wait until all the 
	// parameters of the function has been parsed.
	public boolean ifVarDefined(Symbol s) {
		// first, check within local scope
		for (int i = 0; i < stCur.symbols.size(); i++) {
			Symbol tmp = stCur.symbols.get(i);
			if (tmp.symbolType == SYMBOLTYPE.VARIABLE && tmp.tk.token.equals(s.tk.token)) {
				copySymbolInfo(s, tmp);				
				return true;
			}
		}
		// if it is parameter
		for (int i = 0; i < stCur.symbols.size(); i++) {
			Symbol tmp = stCur.symbols.get(i);
			if (tmp.symbolType == SYMBOLTYPE.PARAMETER && tmp.tk.token.equals(s.tk.token)) {
				copySymbolInfo(s, tmp);
				return true;
			}
		}
		
		// then, if parent is a member function, including global variables
		if (stCur.parent != null) {
			if (stCur.parent.symbolType != SYMBOLTYPE.FUNCTION) {
				printErr(s, "Action error while checking variable definition.");
				return false;
			}
			// if it is a member of class (function or variable which is previous defined)
			for (int i = 0; i < stCur.parent.self.symbols.size(); i++) {		// allow the function itself
				Symbol tmp = stCur.parent.self.symbols.get(i);
				if (tmp.symbolType == SYMBOLTYPE.VARIABLE && tmp.tk.token.equals(s.tk.token)) {
					// s might be a function.
					copySymbolInfo(s, tmp);
					//s.dataType = (Token) tmp.dataType.clone();		
					s.self = stCur.parent.self;
					return true;
				}
				if (tmp.symbolType == SYMBOLTYPE.FUNCTION && tmp.tk.token.equals(s.tk.token)) {
					// check all the parameters later
					s.dataType = (Token) tmp.dataType.clone();
					s.self = stCur.parent.self;
					//varFuncParams.add(s);
					return true;
				}
			}
		}

		// if it is a global function
		if (stBak == null) {
			return false;
		}
		for (int i = 0; i < stBak.symbols.size(); i++) {
			Symbol tmp = stBak.symbols.get(i);
			if (tmp.symbolType == SYMBOLTYPE.FUNCTION && tmp.tk.token.equals(s.tk.token)) {
				// check all the parameters later
				s.dataType = (Token) tmp.dataType.clone();				
				//varFuncParams.add(s);
				return true;
			}
		}

		// class member function defined after this function 
		SymbolTable curParentSelf = null; 
		for (int i = 0; i < stBak.symbols.size(); i++) {
			Symbol t = stBak.symbols.get(i);
			if(stCur.parent != null && stCur.parent.self != null){ //TODO create table for main 
			if (stCur.parent.self.parent != null 
					&& t.address.equals(stCur.parent.self.parent.address)) {
				curParentSelf = t.child;
				break;
			}
			}
		}
		if (curParentSelf != null) {
			for (int i = 0; i < curParentSelf.symbols.size(); i++) {
				Symbol tmp = curParentSelf.symbols.get(i);
				if (tmp.symbolType == SYMBOLTYPE.FUNCTION && tmp.tk.token.equals(s.tk.token)) {
					// check all the parameters later
					s.dataType = (Token) tmp.dataType.clone();
					s.self = curParentSelf;
					return true;
				}
			}
		}

		printErr(s, "Undeclared identifier.");
		return false;
	}

	private void copySymbolInfo(Symbol d, Symbol s) {
		d.dataType = (Token) s.dataType.clone();
		d.address = s.address;
		d.size = s.size;
		d.dimensions = s.dimensions;
		d.isArray = s.isArray;
		d.sizeOfDimension = s.sizeOfDimension;
		d.symbolType = s.symbolType;
	}
	
	// check if the token is a class member
	public boolean ifClassMember(Symbol s, Symbol m) {
		if (stBak == null) {
			return false;
		}
		//System.out.println("--" + s.dataType.token + ", " + m.tk.token);
		// first, if its type is a class
		for (int i = 0; i < stBak.symbols.size(); i++) {
			Symbol tmp = stBak.symbols.get(i);
			
			if (tmp.symbolType == SYMBOLTYPE.CLASS && tmp.tk.token.equals(s.dataType.token)) {
				// check if it is a member
				int offset = 0;
				
				for (int j = 0; j < tmp.child.symbols.size(); j++) {
					Symbol p = tmp.child.symbols.get(j);
					
					// s: the instance of the class
					// p: the member of the class
					// m: current symbol
					// tmp: the class symbol
					if (p.tk.token.equals(m.tk.token)) {
						if (m.address == null) {
							m.address = s.address;			// for ASM code
						}
						m.dataType = (Token) p.dataType.clone();
						m.size = p.size;
						m.dimensions = p.dimensions;
						m.isArray = p.isArray;
						m.sizeOfDimension = p.sizeOfDimension;
						
						m.className = tmp.tk.token;		// used to check the function params later.
						m.offset = offset;
						// parameter with class type
						if (s.symbolType == SYMBOLTYPE.PARAMETER) {
							m.ifPassedByAddress = true;
						}
						return true;
					}
					if (p.symbolType == SYMBOLTYPE.VARIABLE) {
						if (p.isArray) {
							offset += getArrayTotalSize(p);
						} else {
							offset += p.size;
						}
					}
				}
				printErr(s, "'" + m.tk.token + "' is not a member of '" + tmp.tk.token + "'");
				return false;
			}
		}
		printErr(m, "left of '." + m.tk.token + "' must have class type");
		return false;
	}

	// using a array
	private ArrayList<Symbol> arrIndexList = new ArrayList<Symbol>();
	
	// check if the type of express is a valid type for the index of array
	public boolean ifValidIndexType(Symbol s) {//TODO what if it is arithexpr
		if (!s.dataType.token.equals("integer")) {
			printErr(s, "Invalid array index type: " + s.dataType.token);
			return false;
		}
		arrIndexList.add(s);
		return true;
	}
	
	// get the element address of the array
	public int calcArrayAddr(Symbol s) {//TODO what if id.id = 
		int dimensions = arrIndexList.size();
		
		// check the number of dimensions
		if (s.dimensions != dimensions) {
			printErr(s, "Invalid array dimensions: " + dimensions);
			arrIndexList.clear();
			return -1;
		}
		
		// calculate the element address
		if (dimensions == 0) {
			// 
		} else {
			
			//asmCode("", "add", "r11", "r11", "r0", "% calc array index");
			for (int i = 0; i < arrIndexList.size(); i++) {
				int size = 1;		// total size of sub dimensions
				
				for (int j = i + 1; j < s.sizeOfDimension.size(); j++) {
					size *= s.sizeOfDimension.get(j);
				}
				size *= s.size;
				
				asmCode.asmCode("", "addi", "r1", "r0", "" + size, "% array offset index: " + arrIndexList.get(i).tk.token);
				asmCode.asmLW(arrIndexList.get(i), "r2", "");
				asmCode.asmCode("", "mul", "r1", "r1", "r2");
				asmCode.asmCode("", "add", "r11", "r11", "r1");
			}
			//asmCode("", "muli", "r11", "r11", "" + s.size);
		}
		arrIndexList.clear();
		return 0;
	}
	private int asmClassMemberOffset(int offset) {
//		asmCode.asmCode("", "addi", "r11", "r11", "" + offset, "% class member offset: " + offset);
		return 0;
	}

	// compare date types
	public Symbol compDateType(Symbol a, Symbol b) {
		if (a.dataType.token.equals(b.dataType.token)) {
			// generate asm code
			//asmCode("% " + a.tk.token + "=" + b.tk.token);			
			asmCode.asmLW(b, "r1", "% " + a.tk.token + "=" + b.tk.token);
			asmCode.asmSW(a);
			return a;
		}
		if (a.dataType.token.equals("integer") && b.dataType.token.equals("real")) {
			// convert a -> real
			printWarn(a, "Warning: Convert from 'real' to 'integer'");
			asmCode.asmLW(b, "r1", "% " + a.tk.token + "=" + b.tk.token);
			//asmCode("", "sr", "r1", "8", "", "% real -> integer: " + b.tk.token);
			asmCode.asmCode("", "divi", "r1", "r1", floatMask, "% real -> integer: " + b.tk.token);
			asmCode.asmSW(a);
			return a;
		}
		if (a.dataType.token.equals("real") && b.dataType.token.equals("integer")) {
			// convert b -> real
			printWarn(a, "Warning: Convert from 'integer' to 'real'");
			asmCode.asmLW(b, "r1", "% " + a.tk.token + "=" + b.tk.token);
			//asmCode("", "sl", "r1", "8", "", "% integer -> real: " + b.tk.token);
			asmCode.asmCode("", "muli", "r1", "r1", floatMask, "% integer -> real: " + b.tk.token);
			asmCode.asmSW(a);
			return a;
		}
//		Symbol err = new Symbol();
//		err.tk = (Token)b.tk.clone();
//		err.dataType = (Token)b.dataType.clone();
//		err.dataType.token = "err";
		printErr(a, "Cannot convert from '" + b.dataType.token + "' to '" + a.dataType.token + "'.");
		return b;
	}

	
	public Symbol compDateTypeNum(Symbol a, Symbol b, Token tkOp) {
		//System.out.println(a.tk.token + ", " + b.tk.token);
		if (a.dataType.token.equals("integer") && b.dataType.token.equals("real")) {
			// convert a -> real
			printWarn(a, "Warning: Convert from 'integer' to 'real'");
			asmCode.asmGenMathExpr(b, a, tkOp, 1);
			
			if(b.writeExpr == null){ //TODO if type is not num
				a.writeExpr =  a.tk.token + tkOp.token + b.tk.token;
				
			}
			else{
				if ( b.writeExpr != null){
					a.writeExpr = a.tk.token + tkOp.token + b.writeExpr;
				
				}
				
			}
			
			
			return b;
		}
		if (b.dataType.token.equals("integer") && a.dataType.token.equals("real")) {
			// convert b -> real
			printWarn(b, "Warning: Convert from 'integer' to 'real'");
			asmCode.asmGenMathExpr(a, b, tkOp, 2);
			
			if(b.writeExpr == null){ //TODO if type is not num
				a.writeExpr =  a.tk.token + tkOp.token + b.tk.token;
				
			}
			else{
				if ( b.writeExpr != null){
					a.writeExpr = a.tk.token + tkOp.token + b.writeExpr;
					
				}
				
			}
			
			
			return a;
		}
		if (a.dataType.token.equals("integer") && b.dataType.token.equals("integer")) {
			asmCode.asmGenMathExpr(a, b, tkOp, 0);

			if(b.writeExpr == null){ //TODO if type is not num
				a.writeExpr =  a.tk.token + tkOp.token + b.tk.token;
				
			}
			else{
				if ( b.writeExpr != null){
					a.writeExpr = a.tk.token + tkOp.token + b.writeExpr;
					
				}
				
			}
		
			return a;
		}
		if (a.dataType.token.equals("real") && b.dataType.token.equals("real")) {
			asmCode.asmGenMathExpr(a, b, tkOp, 3);
			
			if(b.writeExpr == null){ //TODO if type is not num
				a.writeExpr =  a.tk.token + tkOp.token + b.tk.token;
				
			}
			else{
				if ( b.writeExpr != null){
					a.writeExpr = a.tk.token + tkOp.token + b.writeExpr;
					
				}
				
			}
			
			
			return a;
		}
		printErr(a, "and token: " + b.tk.token + ", Type should be integer or real.");
		return b;
	}

	// store function parameters of the variable
	public ArrayList<Symbol> varFuncParams = null;	
	public ArrayList<String> varFuncParamsAttr = new ArrayList<String>();

	private boolean compParams(SymbolTable st) {
		int cnt = 0;
		for (int i = 0; i < st.symbols.size(); i++) {
			Symbol s = st.symbols.get(i);
			if (s.symbolType == SYMBOLTYPE.PARAMETER) {
				cnt++;
				if (varFuncParams.size() < cnt) {
					return false;
				}
				Symbol p = varFuncParams.get(i);
				if (!s.dataType.token.equals(p.dataType.token)) {
					if (!((s.dataType.token.equals("real") && p.dataType.token.equals("integer"))
							|| (s.dataType.token.equals("integer") && p.dataType.token.equals("real")))) {
						return false;
					}
				}
			}
		}
		if (varFuncParams.size() != cnt) {
			return false;
		}
		
		// print warning message
		cnt = 0;
		varFuncParamsAttr.clear();
		for (int i = 0; i < st.symbols.size(); i++) {
			Symbol s = st.symbols.get(i);
			if (s.symbolType == SYMBOLTYPE.PARAMETER) {
				cnt++;
				Symbol p = varFuncParams.get(i);
				varFuncParamsAttr.add(s.dataType.token);
				if (s.dataType.token.equals("real") && p.dataType.token.equals("integer")) {
					printWarn(p, "Warning: Convert parameter " + cnt + " from 'integer' to 'real'");
				}
				if (s.dataType.token.equals("integer") && p.dataType.token.equals("real")) {
					printWarn(p, "Warning: Convert parameter " + cnt + " from 'real' to 'integer'");
				}
			}
		}		
		return true;
	}
	// check all parameters for a function variable 
	public boolean ifValidFuncParamType(Symbol var) {
		if (var.symbolType == SYMBOLTYPE.CHKMEMBER) {
			// statement likes: a = class.func(a, b);
			// find the function from the class scope
			if (stBak == null) {
				return false;
			}
			for (int i = 0; i < stBak.symbols.size(); i++) {
				Symbol s = stBak.symbols.get(i);
				if (s.symbolType == SYMBOLTYPE.CLASS && s.tk.token.equals(var.className)) {
					for (int j = 0; j < s.child.symbols.size(); j++) {
						Symbol t = s.child.symbols.get(j);
						if (t.symbolType == SYMBOLTYPE.FUNCTION && t.tk.token.equals(var.tk.token)) {
							if (compParams(t.child)) {
								var.dataType = (Token) t.dataType.clone();
								var.address = t.address;
								var.size = t.size;
								var.symbolType = SYMBOLTYPE.FUNCTION;
								printLog(t, "ifValidFuncParamType OK");
								printLog(var, "ifValidFuncParamType OK");
								return true;
							}
						}
					}
				}
			}
		} else  {
			// statement likes: a = func(a, b);
			// first try to find the function within the scope which var is placed in.
			if (var.self != null) {
				for (int i = 0; i < var.self.symbols.size(); i++) {
					Symbol s = var.self.symbols.get(i);
					if (s.symbolType == SYMBOLTYPE.FUNCTION && s.tk.token.equals(var.tk.token)) {
						if (compParams(s.child)) {
							// correct the data type
							var.dataType = (Token) s.dataType.clone();
							var.address = s.address;
							var.size = s.size;
							var.symbolType = SYMBOLTYPE.FUNCTION;
							printLog(s, "ifValidFuncParamType OK");
							printLog(var, "ifValidFuncParamType OK");
							return true;
						}
					}
				}
			}
			
			// then try to find the function from Global functions
			if (stBak == null) {
				return false;
			}
			for (int i = 0; i < stBak.symbols.size(); i++) {
				Symbol s = stBak.symbols.get(i);
				if (s.symbolType == SYMBOLTYPE.FUNCTION && s.tk.token.equals(var.tk.token)) {
					if (compParams(s.child)) {
						var.dataType = (Token) s.dataType.clone();
						var.address = s.address;
						var.size = s.size;
						var.symbolType = SYMBOLTYPE.FUNCTION;
						printLog(s, "ifValidFuncParamType OK");
						printLog(var, "ifValidFuncParamType OK");
						return true;
					}
				}
			}
		}
		
		printErr(var, "Undeclared identifier: function parameters mismatch.");		
		return false;
	}
	
	
	public void cirdependencies(Symbol curVar , Symbol curClass ){
	
		if (stBak == null){
			return  ;
		}
		for (int i = 0 ; i < stBak.symbols.size(); i ++){
			Symbol temp  = stBak.symbols.get(i);
			if (temp.symbolType == SYMBOLTYPE.CLASS && curVar.dataType.token.equals(temp.tk.token)){
				for (int j = 0 ; j< temp.child.symbols.size(); j ++){
					Symbol temvar =  temp.child.symbols.get(j);
					if(temvar.symbolType == SYMBOLTYPE.VARIABLE){
						if(temvar.dataType.token != "integer" && temvar.dataType.token != "real" ){
							if(temvar.dataType.token.equals(curClass.tk.token)){
							printErr ( curVar , "circular class dependencies");
						}
					}
				}
				
			}
		}
	}
		
		
}
	
	public int findOffset(Token s, Token m) {
		if (stBak == null) {
			return 0;
		}
		//System.out.println("--" + s.dataType.token + ", " + m.tk.token);
		// first, if its type is a class
		for (int i = 0; i < stBak.symbols.size(); i++) {
			Symbol tmp = stBak.symbols.get(i);
			
			if (tmp.symbolType == SYMBOLTYPE.CLASS && tmp.tk.token.equals(s.token)) {
				// check if it is a member
				int offset = 0;
				
				for (int j = 0; j < tmp.child.symbols.size(); j++) {
					Symbol p = tmp.child.symbols.get(j);
					
					// s: the instance of the class
					// p: the member of the class
					// m: current symbol
					// tmp: the class symbol
					if (p.tk.token.equals(m.token)) {

						return offset;
					}
					if (p.symbolType == SYMBOLTYPE.VARIABLE) {
						if (p.isArray) {
							offset += getArrayTotalSize(p);
						} else {
							offset += p.size;
						}
					}
				}
	
			}
		}
		
		return 0;
	}

	//-----------------------------------------------------------------------------
	
	public SymbolTable getStBak() {
		return stBak;
	}
	public void setStBak(SymbolTable stBak) {
		this.stBak = stBak;
	}
	public SymbolTable getStCur() {
		return stCur;
	}
	public void setStCur(SymbolTable stCur) {
		this.stCur = stCur;
	}

	public void setAsmCode(ASMCode asmCode) {
		this.asmCode = asmCode;
	}
	public ArrayList<Symbol> getVarFuncParams() {
		return varFuncParams;
	}
	public void setVarFuncParams(ArrayList<Symbol> varFuncParams) {
		this.varFuncParams = varFuncParams;
	}
	public ArrayList<String> getVarFuncParamsAttr() {
		return varFuncParamsAttr;
	}
	public void setVarFuncParamsAttr(ArrayList<String> varFuncParamsAttr) {
		this.varFuncParamsAttr = varFuncParamsAttr;
	}
	public SymbolTable getStHead() {
		return stHead;
	}
	public void setStHead(SymbolTable stHead) {
		this.stHead = stHead;
	}
}
