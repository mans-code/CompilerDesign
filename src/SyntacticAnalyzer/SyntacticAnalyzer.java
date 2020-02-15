package SyntacticAnalyzer;

import java.util.ArrayList;

import utils.SysLogger;
import LexicalAnalyzer.LexicalAnalyzer;
import LexicalAnalyzer.StateMachineDriver;
import LexicalAnalyzer.Token;
import SemanticActions.ASMCode;
import SemanticActions.SemanticActions;
import SemanticActions.Symbol;
import SemanticActions.Symbol.SYMBOLTYPE;
import SemanticActions.SymbolTable;

public class SyntacticAnalyzer {
	private LexicalAnalyzer lexScanner = null;
	private Token curToken = null;
	private Token preToken = new Token();
	private Token nextToken = null;
	
	
	private SemanticActions smActions;
	private ASMCode asmCode;
	private Symbol curVar = null;
	Symbol curClass = null ; 
	
	public int init(LexicalAnalyzer la) {
		if (la == null) {
			SysLogger.err("SyntacticAnalyzer init. LexicalAnalyzer = null");
			return -1;
		}
		lexScanner = la;
		
		// initialize First & Follow sets
		if (FirstFollowSets.init() != 0) {
			return -1;
		}
		
		smActions = new SemanticActions();
		asmCode = new ASMCode();
		smActions.setAsmCode(asmCode);
		asmCode.setSemAct(smActions);
		
		return 0;
	}

	// main function
	public boolean parse() {
		curToken = lexScanner.nextToken();
		
		//no comment 
		while(true){
			if(curToken == null){break;}
			if(curToken.type == 7){
				curToken = lexScanner.nextToken();
			}else {break;}
		}
		
		if (curToken == null) {
			SysLogger.err("The input file seems empty.");
			return false;
		}
		
		//if (startSymbol() && match(SYNTAX_END_SIGN)) {
		if (startSymbol()) {
			if (curToken == null) {		// end of file				
				asmCode.asmGenDataAndCode();
				return true;
			}
		}
		
		//err("END.");
		return false;
	}
	public boolean parseEx(SymbolTable st) {
		if (st == null) {
			return parse();
		}
		
		// copy function declarations
		smActions.setStBak((SymbolTable) st.clone());
		return parse();
	}
	
	private int getNextToken() {
		preToken = curToken;
		if (nextToken != null) {
			curToken = (Token) nextToken.clone();
			nextToken = null;
		} else {
			curToken = lexScanner.nextToken();
			while(true){
				if(curToken == null){break;}
				if(curToken.type == 7){
			
				curToken = lexScanner.nextToken();
				}else {break;}
			}
			
			
		}
		
		return 0;
	}
	
	private void err(String msg) {
		String str = "";
		
		if (curToken != null) {
			str = String.format("Syntax error at line: %4d, col: %4d. Token:%12s.  ", curToken.line, 
					curToken.column, curToken.token);
		} else {
			str = String.format("Syntax error at line: %4d, col: %4d. Token:%12s.  ", preToken.line, 
					preToken.column + preToken.token.length(), preToken.token);
		}
		SysLogger.err(str + msg);
	}
	
	// if next token match the given token
	private boolean matchEx(String tk, SYMBOLTYPE type) {
		SysLogger.log("match: " + tk);

		if (tk.equals("id")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_ID) {
				// store the symbol temporarily
				Symbol mb = new Symbol();
				if (type == SYMBOLTYPE.CHKMEMBER) {
					mb.tk = (Token) curToken.clone();
					mb.ifPassedByAddress = curVar.ifPassedByAddress;
				} else {
					curVar = new Symbol();
					curVar.tk = (Token) curToken.clone();
					curVar.dataType = (Token) preToken.clone();
					curVar.symbolType = type;
				}
				
				if (type == SYMBOLTYPE.CHKVAR) {
					smActions.ifVarDefined(curVar);
//					asmCode.asmResetOffset();
				} else if (type == SYMBOLTYPE.CHKTYPE) {
					smActions.ifDataTypeDefined(curVar);
				} else if (type == SYMBOLTYPE.CHKMEMBER) {
					smActions.ifClassMember(curVar, mb);
					
					curVar = (Symbol)mb.clone();
					curVar.tk = (Token) curToken.clone();
					if (mb.dataType != null) {
						curVar.dataType = (Token) mb.dataType.clone();
					} else {
						curVar.dataType = (Token) preToken.clone();
					}
					curVar.symbolType = type;
					//curVar.address = mb.address;
					//curVar.ifPushOffset = mb.ifPushOffset;
				} 
				getNextToken();
				return true;
			}
		} else if (tk.equals("int")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_INT && !curToken.token.equals("0")) {
				if (type == SYMBOLTYPE.ARRAYSIZE) {
					curVar.isArray = true;
					curVar.sizeOfDimension.add(Integer.parseInt(curToken.token));
					curVar.dimensions++;
				}
				getNextToken();
				return true;
			} 
		} else if (tk.equals("num")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_INT
					|| curToken.type == StateMachineDriver.TOKEN_TYPE_FLOAT) {
				getNextToken();
				return true;
			} 
		} else if (tk.equals("addOp")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_OP_ADD) {
				getNextToken();
				return true;
			} 
		} else if (tk.equals("multOp")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_OP_MUL) {
				getNextToken();
				return true;
			} 
		} else if (tk.equals("relOp")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_OP_REL) {
				getNextToken();
				return true;
			} 
		} else if (tk.equals("assignOp")) {
			if (curToken.type == StateMachineDriver.TOKEN_TYPE_OP_ASS) {
				getNextToken();
				return true;
			} 
		} else if (curToken != null && curToken.token.equals(tk)) {
			if (type == SYMBOLTYPE.CLASS) {
				smActions.newClass(curVar);
			} else if (type == SYMBOLTYPE.FUNCTION) {
				smActions.newFunction(curVar);
			} else if (type == SYMBOLTYPE.UNKNOWN_EXITTABLE) {
				smActions.exitCurSymbolTable();
				
			}else if(type == SYMBOLTYPE.CIREFE){
				if( curVar.dataType.token != "integer" && 
						curVar.dataType.token != "real"  && curClass != null ){
				                           // per   class 
				smActions.cirdependencies(curVar , curClass);
				
				}
			}
			
			getNextToken();
			return true;
		}

		err("Expected the token: " + tk);

		// try to insert this new token
		err("Inserts a new token: " + tk);
		
		// do not need to call getNextToken()

//		if (nextToken == null) {
//			nextToken = new Token();
//			
//			nextToken = (Token) curToken.clone();
//		}		 
//		// else, keep the original token, because curToken is a inserted token at this time.
//		curToken.error = true;
//		curToken.type = StateMachineDriver.TOKEN_TYPE_UNKNOWN;
//		curToken.token = tk;
//		err("Inserts a new token: " + tk);
		
		return true;		
	}
	
	private boolean match(String tk) {
		return matchEx(tk, SYMBOLTYPE.UNKNOWN);
	}

	// check if the next token is one of the tokens in the FIRST set for grammar 'symbol'
	private boolean isFirst(String symbol) {
		String set = FirstFollowSets.isFirst(symbol, curToken);
		
		if (set == "") {
			if (curToken != null && symbol.equals(curToken.token)) {
				return true;
			}
			return false;
		} else if (set != null) {
			return false;
		}
		return true;
	}
	
	// check if the next token is one of the tokens in the FOLLOW set for grammar 'symbol'
	private boolean isFollow(String symbol) {
		String set = FirstFollowSets.isFollow(symbol, curToken);
		
		if (set == null) {
			return true;
		}

		return false;
	}
	
	// if there is a invalid token, skips it
	// if the invalid token is a newline, insert ';'
	private boolean skipErrors(String first, String follow) {
		SysLogger.log("skipErrors: " + first + ", " + follow);
		
		// if there is an error
		if ((!first.equals("") && isFirst(first)) 
				|| (!follow.equals("") && isFollow(follow))) {
			return true;
		}
		
		// print the error message	
		String expectedTokens = FirstFollowSets.getFFSets(first, follow);
		if (expectedTokens == null && !first.equals("")) {
			expectedTokens = first;
		}
		err("Expected tokens: " + expectedTokens);
		
		// skip the tokens until find a valid one
		String lst[] = expectedTokens.split(" ");
		do {
			String newTk = null;

			if (curToken != null && preToken != null && curToken.line > preToken.line) {
				// if encounters a newline token
				// try to insert ')' or ';' if they are in the expected tokens list.
				for (int i = 0; i < lst.length; i++) {
					if (lst[i].equals(")")) {
						newTk = ")";
						break; // break the for loop 
					} else if (lst[i].equals(";")) {
						newTk = ";";
						break;//break the for loop 
					}
					
				}
				
				if (newTk != null) {
					if (nextToken == null) {
						nextToken = new Token();
						
						nextToken = (Token) curToken.clone();
					}
					// else, keep the original token, because curToken is a inserted token at this time.
					curToken.error = true;
					curToken.type = StateMachineDriver.TOKEN_TYPE_UNKNOWN; 
					curToken.token = newTk;
					err("Inserts a new token: " + newTk);
					break;
				}
			}
			if (newTk == null) {
				if (curToken != null && nextToken == null) {
					// do not need to skip the token we inserted.
					err("Skips a token: " + curToken.token);
				}
				getNextToken();
				if (curToken != null && curToken.token != null) {
					if (curToken.token.equals("class")) {
						err("Finding the keyword 'class' while skiping invalid tokens. Discards previous grammars.");
						prog();
						curToken = null;		// exit;
						break;
					} else if (curToken.token.equals("program")) {
						err("Finding the keyword 'program' while skiping invalid tokens. Discards previous grammars.");
						progBody();
						curToken = null;		// exit;
						break;
					}
				}
			}
			
		} while (curToken != null && !(isFirst(first) || isFollow(follow)));

		return false;
	}

	private void printGrammar(String msg) {
		String str = "";
		
		if (preToken != null) {
			str = String.format("Line: %4s, Col: %4s, Token: %12s, \t", 
					preToken.line, preToken.column, preToken.token); 
		}
		
		SysLogger.der(str + "Grammar: " + msg);
	}
	
	private void copySymbol(Symbol d, Symbol s) {
		d.tk = (Token)s.tk.clone();
		d.dataType = (Token)s.dataType.clone();
		d.address = s.address;
		d.symbolType = s.symbolType;
		d.size = s.size;
		d.dimensions = s.dimensions;
		d.isArray = s.isArray;
		d.sizeOfDimension = s.sizeOfDimension;
		//d.className = s.className;
		d.ifPassedByAddress = s.ifPassedByAddress;
		d.writeExpr = s.writeExpr;
		d.offset = s.offset;
		d.className = s.className;
		d.addresToPass = s.addresToPass;
	}
	
	/* Grammar definition:
   prog             -> classDeclList progBody 
   classDeclList    -> classDecl classDeclList 
                     | EPSILON 
   classDecl        -> class id { varFuncDeclList } ; 
   varFuncDeclList  -> type id varFuncDeclListP 
                     | EPSILON 
   varFuncDeclListP -> ( fParams ) funcBody ; funcDefList 
                     | arraySizeList ; varFuncDeclList 
   funcDefList      -> funcDef funcDefList 
                     | EPSILON 
   funcDef          -> funcHead funcBody ; 
   funcHead         -> type id ( fParams ) 
   funcBody         -> { varStateList } 
   varStateList     -> integer id arraySizeList ; varStateList 
                     | real id arraySizeList ; varStateList 
                     | id varStateListP 
                     | EPSILON 
   varStateListP    -> indiceList variableP assignOp expr ; statementList 
                     | id arraySizeList ; varStateList 
   progBody         -> program funcBody ; funcDefList 
   statementList    -> statement statementList 
                     | EPSILON 
   arraySizeList    -> arraySize arraySizeList 
                     | EPSILON 
   statement        -> if ( expr ) then statBlock else statBlock ; 
                     | while ( expr ) do statBlock ; 
                     | read ( variable ) ; 
                     | return ( expr ) ; 
                     | write ( expr ) ; 
                     | variable assignOp expr ; 
   statBlock        -> { statementList } | statement 
                     | EPSILON 
   expr             -> arithExpr exprP 
   exprP            -> relOp arithExpr | EPSILON 
   arithExpr        -> term arithExprP 
   arithExprP       -> addOp term arithExprP 
                     | EPSILON 
   sign             -> + | - 
   term             -> factor termP 
   termP            -> multOp factor termP 
                     | EPSILON 
   factor           -> ( expr ) | id factorPP | num | not factor 
                     | sign factor 
   factorPP         -> ( aParams ) | indiceList factorP 
   factorP          -> . id factorPP | EPSILON 
   variable         -> id indiceList variableP 
   variableP        -> . id indiceList variableP 
                     | EPSILON 
   indiceList       -> indice indiceList | EPSILON 
   indice           -> [ arithExpr ] 
   arraySize        -> [ int ] 
   type             -> id | integer | real 
   fParams          -> type id arraySizeList fParamsTailList 
                     | EPSILON 
   fParamsTailList  -> fParamsTail fParamsTailList 
                     | EPSILON 
   aParams          -> expr aParamsTailList 
                     | EPSILON 
   aParamsTailList  -> aParamsTail aParamsTailList 
                     | EPSILON 
   fParamsTail      -> , type id arraySizeList 
   aParamsTail      -> , expr 

	*/	
	
	// entry point of the grammar
	private boolean startSymbol() {		
		asmCode.asmStartASM();
		return prog();
	}	

	private boolean prog() {
		smActions.newProg();
		//skipErrors("classDeclList", "");
		//if (isFirst("classDeclList")) {
			if (classDeclList() && progBody()) {
				printGrammar("prog             -> classDeclList progBody");
				return true;
			} else
				return false;
		//}
		//return false;		
	}
	private boolean classDeclList() {
		skipErrors("classDecl", "classDeclList");
		if (isFirst("classDecl")) {
			if (classDecl() && classDeclList()) {
				printGrammar("classDeclList    -> classDecl classDeclList");
				return true;
			} else
				return false;
		} else if (isFollow("classDeclList")) {
			printGrammar("classDeclList    -> EPSILON");
			return true;
		}
		return false;
	}	
	private boolean progBody() {
		skipErrors("program", "");
		if (isFirst("program")) {
			asmCode.asmStartProg();
			if (match("program") && funcBody() && match(";")) {
				asmCode.asmEndProg();
				if (funcDefList()) {
					printGrammar("progBody         -> program funcBody ; funcDefList");
					return true;
				}
			}
		}
		return false;		
	}

	private boolean classDecl() {
		skipErrors("class", "");
		if (isFirst("class")) {
			if (match("class") && match("id") && matchEx("{", SYMBOLTYPE.CLASS)) {
				Symbol tmp = curVar;// class id 
				curClass = curVar;
				if (varFuncDeclList() && match("}") && match(";")) {
					printGrammar("classDecl        -> class id { varFuncDeclList } ;");
					smActions.setClassSize(tmp);
					curClass = null ; // for circlur depen
					smActions.exitCurSymbolTable();
					return true;
				}
			}
		}
		return false;		
	}
	
	private boolean varFuncDeclList() {
		skipErrors("type", "varFuncDeclList");
		if (isFirst("type")) {
			if (type() && match("id")  && varFuncDeclListP()) {
				printGrammar("varFuncDeclList  -> type id varFuncDeclListP");
				return true;
			} else
				return false;
		} else if (isFollow("varFuncDeclList")) {
			printGrammar("varFuncDeclList  -> EPSILON");
			return true;
		}
		return false;
	}	
	
	private boolean type() {
		skipErrors("type", "");
		if (isFirst("integer")) {
			if (match("integer")) {
				printGrammar("type             -> integer");
				return true;
			} else
				return false;
		} else if (isFirst("real")) {
			if (match("real")) {
				printGrammar("type             -> real");
				return true;
			} else
				return false;
		} else if (isFirst("id")) {
			if (matchEx("id", SYMBOLTYPE.CHKTYPE)) {
				printGrammar("type             -> id");
				return true;
			} else
				return false;
		}
		return false;
	}

	private boolean varFuncDeclListP() {
		skipErrors("varFuncDeclListP", "");
		if (isFirst("(")) {
			if (matchEx("(", SYMBOLTYPE.FUNCTION) && fParams() && match(")")) {
				smActions.ifFuncRedefined(curVar);
				if (funcBody()) {
					asmCode.asmEndOfFuncDefinition();
					if (matchEx(";", SYMBOLTYPE.UNKNOWN_EXITTABLE) && funcDefList()) {
						printGrammar("varFuncDeclListP -> ( fParams ) funcBody ; funcDefList");
						return true;
					}
				}
			}
		} else if (isFirst("varFuncDeclListP")) {
			if (arraySizeList() && matchEx(";"  , SYMBOLTYPE.CIREFE) && varFuncDeclList()) {
				printGrammar("varStateList     -> arraySizeList ; varFuncDeclList");
				return true;
			} else
				return false;
		}
		return false;
	}
	
	private boolean funcBody() {
		skipErrors("{", "");
		if (isFirst("{")) {
			if (match("{") && varStateList() && match("}")) {
				printGrammar("funcBody         -> { varStateList }");
				return true;
			} else
				return false;
		}
		return false;		
	}
	private boolean funcDefList() {
		skipErrors("funcDef", "funcDefList");
		if (isFirst("funcDef")) {
			if (funcDef() && funcDefList()) {
				printGrammar("funcDefList      -> funcDef funcDefList");
				return true;
			} else
				return false;
		} else if (isFollow("funcDefList")) {
			printGrammar("funcDefList      -> EPSILON");
			return true;
		}
		return false;
	}	

	private boolean varStateList() {
		skipErrors("varStateList", "varStateList");
		Symbol expr = new Symbol();
		if (isFirst("integer")) {
			if (match("integer") && match("id") && arraySizeList() && match(";") && varStateList()) {
				printGrammar("varStateList     -> integer id arraySizeList ; varStateList");
				return true;
			} else
				return false;
		} else if (isFirst("real")) {
			if (match("real") && match("id") && arraySizeList() && match(";") && varStateList()) {
				printGrammar("varStateList     -> real id arraySizeList ; varStateList");
				return true;
			} else
				return false;
		} else if (isFirst("id")) {
			if (match("id") && varStateListP()) {
				printGrammar("varStateList     -> id varStateListP");
				return true;
			} else
				return false;
		} else if (isFirst("if")) {
			if ( match("if") && match("(") && expr(expr) && match(")") && match("then")) {
				String elseAddr = asmCode.asmOPIfThen(expr);
				if (statBlock() && match("else")) {
					String endifAddr = asmCode.asmOPIfElse(expr, elseAddr);
					if (statBlock() && match(";") && statementList()) {
						printGrammar("varStateList     -> if ( expr ) then statBlock else statBlock ; statementList");
						asmCode.asmOPIfEndif(expr, endifAddr);
						return true;
					}
				}
			}			
//			if (match("if") && match("(") && expr(expr) && match(")") && match("then") && statBlock() && match("else") && statBlock() && match(";") && statementList()) {
//				printGrammar("varStateList     -> if ( expr ) then statBlock else statBlock ; statementList");
//				return true;
//			} else
//				return false;
		} else if (isFirst("while")) {//TODO do the asm code for loop 
			if (match("while") && match("(") && expr(expr) && match(")") && match("do") && statBlock() && match(";") && statementList()) {
				printGrammar("varStateList     -> while ( expr ) do statBlock ; statementList");
				return true;
			} else
				return false;
		} else if (isFirst("read")) {
			if (match("read") && match("(") && variable() && match(")") && match(";")) {
				asmCode.asmRead(curVar);
				if (statementList()) {
					printGrammar("varStateList     -> read ( variable ) ; statementList");
					return true;
				}
			}
		} else if (isFirst("return")) {
			if (match("return") && match("(") && expr(expr) && match(")") && match(";")) {
				asmCode.asmFuncReturn(expr);
				if (statementList()) {
					printGrammar("varStateList     -> return ( expr ) ; statementList");
					return true;
				}
			}
		} else if (isFirst("write")) {
			if (match("write") && match("(") && expr(expr) && match(")") && match(";")) {
				asmCode.asmWrite(expr);
				if (statementList()) {
					printGrammar("varStateList     -> write ( expr ) ; statementList");
					return true;
				}
			}
		} else if (isFollow("varStateList")) {
			printGrammar("varStateList     -> EPSILON");
			return true;
		}
		return false;
	}	
	private boolean funcDef() {
		skipErrors("funcHead", "");
		if (isFirst("funcHead")) {
			if (funcHead() && funcBody() && match(";")) {
				printGrammar("funcDef          -> funcHead funcBody ;");
				asmCode.asmEndOfFuncDefinition();
				smActions.exitCurSymbolTable();
				return true;
			} else
				return false;
		}
		return false;		
	}

	private boolean varStateListP() {
		skipErrors("varStateListP", "");
		if (isFirst("id")) {
			smActions.ifDataTypeDefined(curVar);
			if (match("id") && arraySizeList() && match(";") && varStateList()) {
				printGrammar("varStateList     -> id arraySizeList ; varStateList");
				return true;
			} else
				return false;
		} else if (isFirst("varStateListP")) {
			Symbol expr = new Symbol();

			smActions.ifVarDefined(curVar);//curvar = type 
			//asmCode.asmResetOffset();
			if (indiceList() && variableP() && match("assignOp")) {
				Symbol tmp = (Symbol)curVar.clone();// what ever after the last .id id 
				
				copySymbol(tmp, curVar);
				asmCode.asmPushOffset(curVar);				
				if (expr(expr)) {
					smActions.compDateType(tmp, expr);
					if (match(";") && statementList()) {
						printGrammar("varStateListP    -> indiceList variableP assignOp expr ; statementList");
						return true;
					}
				}
			}
		}
		return false;
	}	
	private boolean funcHead() {
		skipErrors("type", "");
		if (isFirst("type")) {
			if (type() && match("id") && matchEx("(", SYMBOLTYPE.FUNCTION) && fParams() && match(")")) {
				printGrammar("funcHead         -> type id ( fParams )");
				smActions.ifFuncRedefined(curVar);
				return true;
			} else
				return false;
		} 
		return false;
	}
	private boolean fParams() {
		skipErrors("type", "fParams");
		if (isFirst("type")) {
			if (type() && matchEx("id", SYMBOLTYPE.PARAMETER) && arraySizeList()) {
				if (fParamsTailList()) {
					printGrammar("fParams          -> type id arraySizeList fParamsTailList");
					return true;
				}
			}
		} else if (isFollow("fParams")) {
			printGrammar("fParams          -> EPSILON");
			return true;
		}
		return false;
	}
	private boolean indiceList() {
		skipErrors("indice", "indiceList");
		if (isFirst("indice")) {
			if (indice() && indiceList()) {
				printGrammar("indiceList       -> indice indiceList");
				return true;
			} else
				return false;
		} else if (isFollow("indiceList")) {
			printGrammar("indiceList       -> EPSILON");
			smActions.calcArrayAddr(curVar);//TODO what if id.id// this not clear 
			return true;
		}
		return false;
	}
	private boolean variableP() {
		skipErrors(".", "variableP");
		if (isFirst(".")) {
			if (match(".") && matchEx("id", SYMBOLTYPE.CHKMEMBER) && indiceList() && variableP()) {
				printGrammar("variableP        -> . id indiceList variableP");
				return true;
			} else
				return false;
		} else if (isFollow("variableP")) {
			printGrammar("variableP        -> EPSILON");
			return true;
		}
		return false;
	}
	private boolean expr(Symbol expr) {
		skipErrors("arithExpr", "");
		if (isFirst("arithExpr")) {
			Symbol arithExpr = new Symbol();
			Symbol exprP = new Symbol();
			if (arithExpr(arithExpr) && exprP(arithExpr, exprP)) {
				printGrammar("expr             -> arithExpr exprP");
				//expr.dataType = (Token) exprP.dataType.clone();
				copySymbol(expr, exprP);
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean statementList() {
		skipErrors("statement", "statementList");
		if (isFirst("statement")) {
			if (statement() && statementList()) {
				printGrammar("statementList    -> statement statementList");
				return true;
			} else
				return false;
		} else if (isFollow("statementList")) {
			printGrammar("statementList    -> EPSILON");
			return true;
		}
		return false;
	}
	private boolean fParamsTailList() {
		skipErrors("fParamsTail", "fParamsTailList");
		if (isFirst("fParamsTail")) {
			Symbol tmpSb = (Symbol)curVar.clone();
			copySymbol(tmpSb, curVar);
			if (fParamsTail() && fParamsTailList()) {
				printGrammar("fParamsTailList  -> fParamsTail fParamsTailList");
				//smActions.asmPop(tmpSb);	// pop parameters in the reverse order
				return true;
			} else
				return false;
		} else if (isFollow("fParamsTailList")) {
			printGrammar("fParamsTailList  -> EPSILON");
			//smActions.asmPop(curVar);	// pop parameters in the reverse order
			return true;
		}
		return false;
	}
	private boolean indice() {
		skipErrors("[", "");
		if (isFirst("[")) {
			Symbol tmp = (Symbol) curVar.clone();// the array id 			
			copySymbol(tmp, curVar);
			Symbol arithExpr = new Symbol();
			asmCode.asmPushR("r11");
			if (match("[") && arithExpr(arithExpr) && match("]")) {
				printGrammar("indice           -> [ arithExpr ]");
				smActions.ifValidIndexType(arithExpr);
				curVar = (Symbol) tmp.clone(); 
				copySymbol(curVar, tmp);
				asmCode.asmPopR("r11");
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean arithExpr(Symbol arithExpr) {
		skipErrors("term", "");
		if (isFirst("term")) {
			Symbol term = new Symbol();
			Symbol arithExprP = new Symbol();
			if (term(term) && arithExprP(term, arithExprP)) {
				printGrammar("arithExpr        -> term arithExprP");
				//arithExpr.dataType = (Token) arithExprP.dataType.clone();
				copySymbol(arithExpr, arithExprP);
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean exprP(Symbol arithExpr, Symbol exprP) {
		skipErrors("relOp", "exprP");
		if (isFirst("relOp")) {
			Symbol arithExprP = new Symbol();
			Token tkOp = (Token)curToken.clone();
			asmCode.asmPushOffset(arithExpr);				
			if (match("relOp") && arithExpr(arithExprP)) {
				printGrammar("exprP            -> relOp arithExpr");
				Symbol tmp = smActions.compDateTypeNum(arithExpr, arithExprP, tkOp);
				copySymbol(exprP, tmp);
				//exprP.dataType = (Token).dataType.clone();
				return true;
			} else
				return false;
		} else if (isFollow("exprP")) {
			printGrammar("exprP            -> EPSILON");
			//exprP.dataType = (Token) arithExpr.dataType.clone();
			copySymbol(exprP, arithExpr);
			return true;
		}
		return false;
	}
	private boolean statement() {
		skipErrors("statement", "");
		Symbol expr = new Symbol();
		if (isFirst("if")) {
			if (match("if") && match("(") && expr(expr) && match(")") && match("then")) {
				String elseAddr = asmCode.asmOPIfThen(expr);
				if (statBlock() && match("else")) {
					String endifAddr = asmCode.asmOPIfElse(expr, elseAddr);
					if (statBlock() && match(";")) {
						printGrammar("statement        -> if ( expr ) then statBlock else statBlock ;");
						asmCode.asmOPIfEndif(expr, endifAddr);
						return true;
					}
				}
			} 
		} else if (isFirst("while")) {
			if (match("while")) {
				String gowhileAddr = asmCode.asmOPWhile(curToken);
				if (match("(") && expr(expr) && match(")") && match("do")) {
					String endwhileAddr = asmCode.asmOPWhileDo(expr);
					if (statBlock() && match(";")) {
						printGrammar("statement        -> while ( expr ) do statBlock ;");
						asmCode.asmOPIfEndWhile(gowhileAddr, endwhileAddr);
						return true;
					}
				}
			}
		} else if (isFirst("read")) {
			if (match("read") && match("(") && variable() && match(")") && match(";")) {
				printGrammar("statement        -> read ( variable ) ;");
				asmCode.asmRead(curVar);
				return true;
			} else
				return false;
		} else if (isFirst("return")) {
			if (match("return") && match("(") && expr(expr) && match(")") && match(";")) {
				printGrammar("statement        -> return ( expr ) ;");
				asmCode.asmFuncReturn(expr);
				return true;
			} else
				return false;
		} else if (isFirst("write")) {
			if (match("write") && match("(") && expr(expr) && match(")") && match(";")) {
				printGrammar("statement        -> write ( expr ) ;");
				asmCode.asmWrite(expr);
				return true;
			} else
				return false;
		} else if (isFirst("variable")) {
			if (variable() && match("assignOp")) {
				Symbol tmp = (Symbol) curVar.clone();
				copySymbol(tmp, curVar);
				asmCode.asmPushOffset(tmp);
				if (expr(expr) && match(";")) {
					printGrammar("statement        -> variable assignOp expr ;");
					smActions.compDateType(tmp, expr);
					return true;
				} else
					return false;
			} else
				return false;
		}
		return false;
	}
	private boolean fParamsTail() {
		skipErrors(",", "");
		if (isFirst(",")) {
			if (match(",") && type() && matchEx("id", SYMBOLTYPE.PARAMETER) && arraySizeList()) {
				printGrammar("fParamsTail      -> , type id arraySizeList");
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean term(Symbol term) {
		skipErrors("factor", "");
		if (isFirst("factor")) {
			Symbol factor = new Symbol();
			Symbol termP = new Symbol();
			if (factor(factor) && termP(factor, termP)) {
				printGrammar("term             -> factor termP");
				//term.dataType = (Token) termP.dataType.clone();
				copySymbol(term, termP);
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean arithExprP(Symbol term, Symbol arithExprP) {
		skipErrors("addOp", "arithExprP");
		if (isFirst("addOp")) {
			Symbol arithExprPP = new Symbol();
			Symbol termP = new Symbol();
			Token tkOp = (Token)curToken.clone();
			asmCode.asmPushOffset(term);
			if (match("addOp") && term(termP) && arithExprP(termP, arithExprPP)) {
				printGrammar("arithExprP       -> addOp term arithExprP");
				//arithExprP.dataType = (Token)smActions.compDateTypeNum(term, arithExprPP).dataType.clone();
				Symbol tmp = smActions.compDateTypeNum(term, arithExprPP, tkOp);
				copySymbol(arithExprP, tmp);
				return true;
			} else
				return false;
		} else if (isFollow("arithExprP")) {
			printGrammar("arithExprP       -> EPSILON");
			//arithExprP.dataType = (Token)term.dataType.clone();
			copySymbol(arithExprP, term);
			return true;
		}
		return false;
	}
	private boolean statBlock() {
		skipErrors("statBlock", "statBlock");
		if (isFirst("{")) {
			if (match("{") && statementList() && match("}")) {
				printGrammar("statBlock        -> { statementList }");
				return true;
			} else
				return false;
		} else if (isFirst("statBlock")) {
			if (statement()) {
				printGrammar("statBlock        -> statement");
				return true;
			} else
				return false;
		} else if (isFollow("statBlock")) {
			printGrammar("statBlock        -> EPSILON");
			return true;
		}
		return false;
	}
	private boolean variable() {
		skipErrors("id", "");
		if (isFirst("id")) {
			if (matchEx("id", SYMBOLTYPE.CHKVAR) && indiceList() && variableP()) {
				printGrammar("variable         -> id indiceList variableP");
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean termP(Symbol factor, Symbol termP) {
		skipErrors("multOp", "termP");
		if (isFirst("multOp")) {
			Symbol factorP = new Symbol();
			Symbol termPP = new Symbol();
			Token tkOp = (Token)curToken.clone();
			asmCode.asmPushOffset(factor);				
			if (match("multOp") && factor(factorP) && termP(factorP, termPP)) {
				printGrammar("termP            -> multOp factor termP");
				//termP.dataType = (Token)smActions.compDateTypeNum(factor, termPP).dataType.clone();
				Symbol tmp = smActions.compDateTypeNum(factor, termPP, tkOp);
				copySymbol(termP, tmp);
				return true;
			} else
				return false;
		} else if (isFollow("termP")) {
			printGrammar("termP            -> EPSILON");
			//termP.dataType = (Token)factor.dataType.clone();
			copySymbol(termP, factor);
			return true;
		}
		return false;
	}
	private boolean factor(Symbol factor) {
		skipErrors("factor", "");
		if (isFirst("(")) {
			Symbol expr = new Symbol();
			if (match("(") && expr(expr) && match(")")) {
				printGrammar("factor           -> ( expr )");
				//factor.dataType = (Token) expr.dataType.clone();
				copySymbol(factor, expr);
				return true;
			} else
				return false;
		} else if (isFirst("id")) {			
			Symbol factorPP = new Symbol();
			if (matchEx("id", SYMBOLTYPE.CHKVAR) && factorPP(curVar, factorPP)) {
				printGrammar("factor           -> id factorPP");
				//factor.dataType = (Token)factorPP.dataType.clone();
				copySymbol(factor, factorPP);
				return true;
			} else
				return false;
		} else if (isFirst("num")) {
			if (match("num")) {
				printGrammar("factor           -> num");
				
				
				
				factor.tk = (Token)preToken.clone();// pre because we just did match
				factor.dataType = (Token)preToken.clone();
				if (preToken.type == StateMachineDriver.TOKEN_TYPE_INT) {
					factor.dataType.token = "integer";
				} else if (preToken.type == StateMachineDriver.TOKEN_TYPE_FLOAT) {
					factor.dataType.token = "real";
//					Float val = Float.parseFloat(factor.tk.token) * (1 << 8);
//					factor.tk.token = "" + val.intValue();
				} 
				factor.symbolType = SYMBOLTYPE.NUMBER;
				return true;
			} else
				return false;
		} else if (isFirst("not")) {
			if (match("not") && factor(factor)) {
				printGrammar("factor           -> not factor");
				asmCode.asmOPNot(factor);
				return true;
			} else
				return false;
		} else if (isFirst("sign")) {
			Token tkSign = (Token)curToken.clone();
			if (sign() && factor(factor)) {
				printGrammar("factor           -> sign factor");
				if (tkSign.token.equals("-")) {
					asmCode.asmOPSign(factor);
				}
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean factorPP(Symbol s, Symbol factorPP) {// s is function id 
		skipErrors("factorPP", "factorPP");
		if (isFirst("(")) {
			ArrayList<Symbol> tmpParams = smActions.varFuncParams;
			smActions.varFuncParams = new ArrayList<Symbol>();
			Symbol tmp = (Symbol) curVar.clone();
			copySymbol(tmp,  curVar);			
			if (match("(") && aParams() && match(")")) {
				printGrammar("factorPP         -> ( aParams )");
				// check the parameters
				Symbol cls = (Symbol)tmp.clone();	// save class information
				smActions.ifValidFuncParamType(tmp);
				s.dataType = (Token)tmp.dataType.clone();		// reset data type of the function
				s.address = tmp.address;			// member information
				s.size = tmp.size;
				s.symbolType = tmp.symbolType;
//				s.dimensions = tmp.dimensions;
//				s.sizeOfDimension = tmp.sizeOfDimension;
				
				// generate asm code for function call
				asmCode.asmCallingFunc(tmp, cls);
				
				// restore the parameters.
				smActions.varFuncParams = tmpParams;
				//factorPP.dataType = (Token)s.dataType.clone();
				copySymbol(factorPP, s);
				return true;
			} else
				return false;
		} else if (isFirst("factorPP")) {
			Symbol factorP = new Symbol();
			if (indiceList() && factorP(s, factorP)) {
				printGrammar("factorPP         -> indiceList factorP");
				//factorPP.dataType = (Token)factorP.dataType.clone();
				copySymbol(factorPP, factorP);
				return true;
			} else
				return false;
		} else if (isFollow("factorPP")) {
			printGrammar("factorPP         -> EPSILON");
			//factorPP.dataType = (Token)s.dataType.clone();
			copySymbol(factorPP, s);
			return true;
		}
		return false;
	}
	private boolean factorP(Symbol s, Symbol factorP) {
		skipErrors(".", "factorP");
		if (isFirst(".")) {
			Symbol factorPP = new Symbol();
			if (match(".") && matchEx("id", SYMBOLTYPE.CHKMEMBER) && factorPP(curVar, factorPP)) {
				printGrammar("factorP          -> . id factorPP");
				//factorP.dataType = (Token)factorPP.dataType.clone();
				copySymbol(factorP, factorPP);
				return true;
			} else
				return false;
		} else if (isFollow("factorP")) {
			printGrammar("factorP          -> EPSILON");
			//factorP.dataType = (Token)s.dataType.clone();
			copySymbol(factorP, s);
			return true;
		}
		return false;
	}
	private boolean sign() {
		skipErrors("sign", "");
		if (isFirst("+")) {
			if (match("+")) {
				printGrammar("sign             -> +");
				return true;
			} else
				return false;
		} else if (isFirst("-")) {
			if (match("-")) {
				printGrammar("sign             -> -");
				return true;
			} else
				return false;
		}
		return false;
	}
	private boolean aParams() {
		skipErrors("expr", "aParams");
		if (isFirst("expr")) {
			Symbol expr = new Symbol();
			if (expr(expr)) {
				// store the parameter
				smActions.varFuncParams.add(expr);
				if (aParamsTailList()) {
					printGrammar("aParams          -> expr aParamsTailList");
					return true;
				}
			}
		} else if (isFollow("aParams")) {
			printGrammar("aParams          -> EPSILON");
			return true;
		}
		return false;
	}
	private boolean aParamsTailList() {
		skipErrors("aParamsTail", "aParamsTailList");
		if (isFirst("aParamsTail")) {
			if (aParamsTail() && aParamsTailList()) {
				printGrammar("aParamsTailList  -> aParamsTail aParamsTailList");
				return true;
			} else
				return false;
		} else if (isFollow("aParamsTailList")) {
			printGrammar("aParamsTailList  -> EPSILON");
			return true;
		}
		return false;
	}
	private boolean aParamsTail() {
		skipErrors(",", "");
		if (isFirst(",")) {
			Symbol expr = new Symbol();
			if (match(",") && expr(expr)) {
				printGrammar("aParamsTail      -> , expr");
				smActions.varFuncParams.add(expr);
				return true;
			} else
				return false;
		}
		return false;
	}

	/*

varDeclList -> varDecl varDeclList  | EPSILON
varDecl -> type id arraySizeList ; 
arraySizeList -> arraySize arraySizeList | EPSILON
arraySize -> [ int ] 
type -> integer | real | id

	 */
//	private boolean varDeclList() {
//		skipErrors("varDecl", "varDeclList");
//		if (isFirst("varDecl")) {
//			if (varDecl() && varDeclList()) {
//				printGrammar("varDeclList -> varDecl varDeclList");
//				return true;
//			} else
//				return false;
//		} else if (isFollow("varDeclList")) {
//			printGrammar("varDeclList -> EPSILON");
//			return true;
//		}
//		return false;
//	}
//	
//	private boolean varDecl() {
//		skipErrors("type", "");
//		if (isFirst("type")) {
//			if (type() && match("id") && arraySizeList() && match(";")) {
//				printGrammar("varDecl -> type id arraySizeList ;");
//				return true;
//			} else
//				return false;
//		}
//		return false;
//	}
	private boolean arraySizeList() {
		skipErrors("arraySize", "arraySizeList");
		if (isFirst("arraySize")) {
			if (arraySize() && arraySizeList()) {
				printGrammar("arraySizeList    -> arraySize arraySizeList");
				return true;
			} else
				return false;
		} else if (isFollow("arraySizeList")) {
			printGrammar("arraySizeList    -> EPSILON");
			smActions.newVarible(curVar);
			return true;
		}
		return false;
	}
	
	private boolean arraySize() {
		skipErrors("[", "");
		if (isFirst("[")) {
			if (match("[") && matchEx("int", SYMBOLTYPE.ARRAYSIZE) && match("]")) {
				printGrammar("arraySize        -> [ int ]");
				return true;
			} else
				return false;
		}
		return false;
	}
	

	public SemanticActions getSmActions() {
		return smActions;
	}

	public void setSmActions(SemanticActions smActions) {
		this.smActions = smActions;
	}
}
