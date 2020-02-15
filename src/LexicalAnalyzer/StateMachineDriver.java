

package LexicalAnalyzer;

import utils.SysLogger;

public class StateMachineDriver {
	private static final int ROW_SIZE = 50;
	private static final int COL_SIZE = 255;
	
	public static final int INIT_STATE = 3;	
	
	public static final int C = 0;		// normal state, continue running state machine
	public static final int V = 1;		// final state, stop and exit state machine
	
	// return value of state
	public static final int E = -1;		// fatal error. state machine goes to wrong state.
	public static final int N = -2;		// no back up
	public static final int B = -3;		// back up
	public static final int EC = -4;	// error character
	public static final int ES = -5;	// state machine goes to error state

	public static final int TOKEN_TYPE_UNKNOWN = 0;
	public static final int TOKEN_TYPE_KEYWORD = 1; 
	public static final int TOKEN_TYPE_ID = 2;
	public static final int TOKEN_TYPE_INT = 3;
	public static final int TOKEN_TYPE_FLOAT = 4;
	public static final int TOKEN_TYPE_OPERATOR = 5;
	public static final int TOKEN_TYPE_PUNCTUATOR = 6;
	public static final int TOKEN_TYPE_COMMENT = 7;
	
	public static final int TOKEN_TYPE_OP_ASS = 8;
	public static final int TOKEN_TYPE_OP_ADD = 9;
	public static final int TOKEN_TYPE_OP_REL = 10;
	public static final int TOKEN_TYPE_OP_MUL = 11;
		
	public static String[] TOKEN_STR_TYPE = {
		"Unknown",
		"Keyword",
		"Identifier",
		"Integer",
		"Float",
		"Operator",
		"Punctuator",
		"Comment",
		"AssignOp",
		"AddOp",
		"RelOp",
		"MultOp"
	};
	

	
	private static int stateInputTable[][] = {					// state table for displaying
			// first column defines if the row is final state row: 
			// 0. not a final state row. 1. final state. 2. error state.
			// final state row: 0. no back up, 1. back up
			
			// please refer to the design doc for more information about how this table is generated.
			
			// first row defines the input chars
			{0, ' ', 'a', 'A', '0', '1', '_', '.', '<', '=', '>', '/', '*', ';', ',', '+', '-', '(', ')', '{', '}', '[', ']' },
			
			// second row defines the state for all unknown characters
			{EC,  },
			
			// state machine goes to error state
			{ES, },
			
			// Identifier: [a-z][A-Z] ([a-z][A-Z] | [0-9] | _)*
			{C,  3,   4,   4,   12,  15,  1,   6,   17,  21,  23,  25,  32,  31,  31,  32,  32,  31,  31,  31,  31,  31,  31},
			{C,  5,   4,   4,   4,   4,   4,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5 },
			// final state for identifier
	/*5*/	{V,  B,   E,   E,   E,   E,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B, },
			
			// Fraction & .: . | .0 | .[0-9] [1-9]*
			{C,  7,   7,   7,   8,   2,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7},
			// final state for .
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			{C,  11,  11,  11,  10,  9,   11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11},
/*.11to2*/	{C,  11,  11,  11,  10,  9,   11,  2,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11},
	/*10*/	{C,  2,   2,   2,   10,  9,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2},
			// final state for fraction
			{V,  B,   B,   B,   E,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
		  //{0, ' ', 'a', 'A', '0', '1', '_', '.', '<', '=', '>', '/', '*', ';', ',', '+', '-', '(', ')', '{', '}', '[', ']' },
			// Fraction & 0: 0 | 0.0 | 0.[0-9] [1-9]*
			{C,  13,  13,  13,  13,  13,  13,  14,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13,  13},
			// final state for 0
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			{C,  2,   2,   2,   8,   9,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2},
			
			// [1-9][0-9]* | [1-9][0-9]*.0 | [1-9][0-9]*.[0-9] [1-9]*
	/*15*/	{C,  16,  16,  16,  15,  15,  16,  14,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16},
			// final state for number
			{V,  B,   B,   B,   E,   E,   B,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			
			// operator: <= | <> | <
			{C,  18,  18,  18,  18,  18,  18,  18,  18,  19,  20,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18},
			// final state for <
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   E,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			// final state for <= | == | >=
			{V,  E,   E,   E,   E,   E,   E,   E,   E,   N,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E},
			// final state for <>
	/*20*/	{V,  E,   E,   E,   E,   E,   E,   E,   E,   E,   N,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E,   E},

//			{0, ' ', 'a', 'A', '0', '1', '_', '.', '<', '=', '>', '/', '*', ';', ',', '+', '-', '(', ')', '{', '}', '[', ']' },

			// operator: = | ==
			{C,  22,  22,  22,  22,  22,  22,  22,  22,  19,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22},
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			
			// operator: > | >=
			{C,  24,  24,  24,  24,  24,  24,  24,  24,  19,  24,  24,  24,  24,  24,  24,  24,  24,  24,  24,  24,  24,  24},
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			
			
			//{C,  3,   4,   4,   12,  15,  1,   6,   17,  21,  23,  25,  29,  31,  31,  32,  32,  31,  31,  31,  31,  31,  31},
			// operator & comment: / | // | /*
	/*25*/	{C,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26,  27,  28,  26,  26,  26,  26,  26,  26,  26,  26,  26,  26},
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   E,   E,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B},
			{V,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27},
			{C,  28,  28,  28,  28,  28,  28,  28,  28,  28,  29,  28,  29,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28},
			
			// comment:  */
			{C,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  30,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28},
	/*30*/	{V,  E,   E,   E,   E,   E,   E,   E,   E,   E,   E,   N,   E,    E,   E,   E,   E,   E,   E,   E,   E,   E,   E},

			{V,  B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   N,   N,   B,   B,   N,   N,   N,   N,   N,   N},
			{V,  B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   N,   B,   B,   N,   N,   B,   B,   B,   B,   B,   B},
			
		};
		
	private static int stateTable[][] = new int[ROW_SIZE][COL_SIZE];		// the table used to look up state
	
	public static int init() {
		int row = stateTable.length;
		int col = stateTable[0].length;

		// set initial value to all the states of stateTable
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				stateTable[i][j] = 1;		// invalid character
			}
		}

		// convert the display table to state table by setting state to the right index
		// the column index of stateTable is the first row of stateInputTable
		row = stateInputTable.length;
		for (int i = 1; i < row; i++) {
			col = stateInputTable[i].length;
			for (int j = 0; j < col; j++) {
				stateTable[i][stateInputTable[0][j]] = stateInputTable[i][j];

				// auto add states for consecutive chars 
				char ch = (char) stateInputTable[0][j];
				
				if (ch == 'a') {
					for (int k = 'a'; k <= 'z'; k++) {
						stateTable[i][k] = stateInputTable[i][j];
					}
				}
				if (ch == 'A') {
					for (int k = 'A'; k <= 'Z'; k++) {
						stateTable[i][k] = stateInputTable[i][j];
					}
				}
				if (ch == '1') {
					for (int k = '1'; k <= '9'; k++) {
						stateTable[i][k] = stateInputTable[i][j];
					}
				}
				if (ch == ' ') {
					stateTable[i]['\t'] = stateInputTable[i][j];
					
					// CRLF is treated as space
					stateTable[i]['\r'] = stateInputTable[i][j];
					
					stateTable[i]['\n'] = stateInputTable[i][j];
					if(i== 27 && j == 1){
						stateTable[i]['\n'] = 13;
						stateTable[i]['\r'] = 13;
					}
				}
			}
		}
		
		//dump();
		
		return 0;
	}

	private static void dump() {
		// test
		System.out.print(stateTable[1][' '] + " ");
		System.out.print(stateTable[1]['\t'] + " ");
		System.out.print(stateTable[1]['a'] + " ");
		System.out.print(stateTable[1]['d'] + " ");
		System.out.print(stateTable[1]['A'] + " ");
		System.out.print(stateTable[1]['D'] + " ");
		System.out.print(stateTable[1]['0'] + " ");
		System.out.print(stateTable[1]['1'] + " ");
		System.out.print(stateTable[1]['9'] + " ");
		System.out.print(stateTable[1]['_'] + " \n");
		System.out.print(stateTable[2]['0'] + " ");
		System.out.print(stateTable[2]['1'] + " ");
		System.out.print(stateTable[2]['a'] + " ");
		System.out.print(stateTable[2]['_'] + " \n");
		System.out.print(stateTable[3]['0'] + " ");
		System.out.print(stateTable[3]['1'] + " ");
		System.out.print(stateTable[3]['a'] + " ");
		System.out.print(stateTable[3]['_'] + " \n");
	}
	
	public static int nextState(int curState, char ch, Token token) {
		if (curState < 1 || curState >= ROW_SIZE || ch <= 0 || ch >= COL_SIZE) {
			SysLogger.err("Unkown state or ch: " + curState + ", " + ch);
			return E;
		}
		
		
		int st = stateTable[curState][ch];
		
		if (st < 1 || st >= ROW_SIZE) {
			SysLogger.err("Unkown state: " + curState);
			return E;
		}

		if (stateTable[st][0] == V) {
			if(!(token.type == TOKEN_TYPE_COMMENT && st == 13) ){
				token.type = getType(st);	
			}
			
			return stateTable[st][ch];
		}
		if (stateTable[st][0] == EC) {
			token.type = getType(curState);
			return EC;
		}
		if (stateTable[st][0] == ES) {
			token.type = getType(curState);
			return ES;
		}
		
		return stateTable[curState][ch];
	}
	
	public static int getType(int curState) {
		int ret = TOKEN_TYPE_UNKNOWN;
		
		if (curState >= 4 && curState <= 5) {
			ret = TOKEN_TYPE_ID;
		}
		if (curState >= 12 && curState <= 16 && curState != 14) {
			ret = TOKEN_TYPE_INT;
		}
		if (curState == 31) {
			ret = TOKEN_TYPE_PUNCTUATOR;
		}
		if (curState >= 8 && curState <= 11 || curState == 14) {
			ret = TOKEN_TYPE_FLOAT;
		}
		if ((curState >= 6 && curState <= 7) || (curState >= 17 && curState <= 24) 
				|| curState == 26 || curState ==32) {
			ret = TOKEN_TYPE_OPERATOR;
		}
		if ((curState >= 27 && curState <= 28 ) || curState == 30 ) {
			ret = TOKEN_TYPE_COMMENT;
		}
		return ret;
	}
	
	public static boolean ifKeyword(String token) {
		if (token.equals("if")) {
			return true;
		}
		if (token.equals("then")) {
			return true;
		}
		if (token.equals("else")) {
			return true;
		}
		if (token.equals("while")) {
			return true;
		}
		if (token.equals("do")) {
			return true;
		}
		if (token.equals("class")) {
			return true;
		}
		if (token.equals("integer")) {
			return true;
		}
		if (token.equals("real")) {
			return true;
		}
		if (token.equals("read")) {
			return true;
		}
		if (token.equals("write")) {
			return true;
		}
		if (token.equals("return")) {
			return true;
		}
		if (token.equals("program")) {
			return true;
		}
		return false;
	}
	
	public static boolean ifOperator(String token) {
		if (token.equals("and")) {
			return true;
		}
		if (token.equals("not")) {
			return true;
		}
		if (token.equals("or")) {
			return true;
		}
		return false;
	}

}
