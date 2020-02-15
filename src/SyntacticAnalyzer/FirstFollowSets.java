
package SyntacticAnalyzer;

import java.util.Hashtable;

import LexicalAnalyzer.StateMachineDriver;
import LexicalAnalyzer.Token;

public class FirstFollowSets {
	static String sets[][] = {
		{"prog",			"class program",	""},
		{"classDeclList", 	"class",			"program"},
		{"classDecl",		"class",			""},
		{"varFuncDeclList", "id integer real",	"}"},
		{"varFuncDeclListP","[ ; (",			""},
		{"funcDefList", 	"id integer real",	"} $"},
		{"funcDef",			"id integer real",	""},
		{"funcHead",		"id integer real",	""},
		{"funcBody",		"{",				""},
		{"varStateList", 	"id integer real if while read return write",	"}"},
		{"varStateListP",	"id [ . assignOp",	""},
		{"progBody",		"program",			""},
		{"statementList", 	"if while read return write id",	"}"},
		{"arraySizeList", 	"[",				"; ) ,"},
		{"statement",		"if while read return write id",	""},
		{"statBlock", 		"if while read return write id {",	"else ;"},
		{"expr",			"( id num not + -",	""},
		{"exprP", 			"relOp",			"; ) ,"},
		{"arithExpr",		"( id num not + -",	""},
		{"arithExprP", 		"addOp",			"; ) , relOp ]"},
		{"sign",			"+ -",				""},
		{"term",			"( id num not + -",	""},
		{"termP", 			"multOp",			"; ) , relOp ] addOp"},
		{"factor",			"+ - not num id (",	""},
		{"factorPP",		"[ . (",			"; ) , relOp ] addOp multOp"},
		{"factorP", 		".",				"; ) , relOp ] addOp multOp"},
		{"variable",		"id",				""},
		{"variableP", 		".",				"assignOp )"},
		{"indiceList", 		"[",				". assignOp ; ) , relOp ] addOp multOp"},
		{"indice",			"[",				""},
		{"arraySize",		"[",				""},
		{"type",			"id integer real",	""},
		{"fParams",	 		"id integer real",	")"},
		{"fParamsTailList", ",",				")"},
		{"aParams", 		"( id num not + -",	")"},
		{"aParamsTailList", ",",				")"},
		{"fParamsTail",		",",				""},
		{"aParamsTail",		",",				""},
		
		{"id",				"id",				""},
		{"num",				"num",				""},
		{"assignOp",		"assignOp",			""},
		{"relOp",			"relOp",			""},
		{"addOp",			"addOp",			""},
		{"multOp",			"multOp",			""},
};

	// use hash table storing the First and Follow sets
	static Hashtable<String, String> htFirst = new Hashtable<String, String>(60);
	static Hashtable<String, String> htFollow = new Hashtable<String, String>(60);
	
	public static int init() {		
		for (int i = 0; i < sets.length; i++) {
			for (int j = 0; j < 3; j++) {
				htFirst.put(sets[i][0], sets[i][1]);
				htFollow.put(sets[i][0], sets[i][2]);
			}
		}
		
		return 0;
	}
	
	// if the token is in the sets for grammar: symbol
	public static String isSet(String symbol, Token tk, Hashtable<String, String> ht) {
		String val = ht.get(symbol);
		
		if (val == null) {
			return "";		// terminal symbol
		}
		
		String set[] = val.split(" ");
		
		if (tk == null) {
			for (int i = 0; i < set.length; i++) {
				if (set[i].equals("$")) {
					return null;		// OK. Reaches the end of file
				}
			}
			return val;		// reaches the end of file and not match the grammar
		}
		for (int i = 0; i < set.length; i++) {
			if (set[i].equals("id")) {
				if (tk.type == StateMachineDriver.TOKEN_TYPE_ID) {
					return null;
				}
			} else if (set[i].equals("num")) {
				if (tk.type == StateMachineDriver.TOKEN_TYPE_INT 
						|| tk.type == StateMachineDriver.TOKEN_TYPE_FLOAT) {
					return null;
				}
			} else if (set[i].equals("assignOp")) {
				if (tk.type == StateMachineDriver.TOKEN_TYPE_OP_ASS) {
					return null;
				}
			} else if (set[i].equals("addOp")) {
				if (tk.type == StateMachineDriver.TOKEN_TYPE_OP_ADD) {
					return null;
				}
			} else if (set[i].equals("relOp")) {
				if (tk.type == StateMachineDriver.TOKEN_TYPE_OP_REL) {
					return null;
				}
			} else if (set[i].equals("multOp")) {
				if (tk.type == StateMachineDriver.TOKEN_TYPE_OP_MUL) {
					return null;
				}
			} else if (set[i].equals(tk.token)) {
				return null;
			}
		}
		return val;
	}
	
	public static String isFirst(String symbol, Token tk) {
		return isSet(symbol, tk, htFirst);
	}
	
	public static String isFollow(String symbol, Token tk) {
		return isSet(symbol, tk, htFollow);
	}
	
	public static String getFFSets(String first, String follow) {
		String fi = htFirst.get(first);
		String fo = htFollow.get(follow);
		String ret = null;
		
		if (fi != null) {
			ret = fi + " ";
		}
		if (fo != null) {
			if (ret != null) {
				ret += fo;
			} else
				ret = fo;		
		}
		
		return ret;
	}
}
