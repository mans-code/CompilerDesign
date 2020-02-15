package SemanticActions;

import java.util.Random;

import LexicalAnalyzer.Token;
import SemanticActions.Symbol.SYMBOLTYPE;
import utils.SysLogger;

public class ASMCode {

	private StringBuffer secData ;		// to store data of the ASM code
	private StringBuffer secCode ;		// to store instructions of ASM code 
	private SemanticActions semAct;
	private StringBuffer secCodeEnd ;	
	
	public ASMCode(){
		secData = new StringBuffer();		
		secCode = new StringBuffer();
		secCodeEnd = new StringBuffer();
		
	}
	
	
	// print ASM code
	public void asmData(String op) {
		String cmd;
		cmd = String.format("%-25s %-7s ", "", op);
		secData.append(cmd + "\r\n");
		//System.out.println(cmd);
	}
	public void asmCode(String op) {
		String cmd;
		if (secCode.charAt(secCode.length() - 1) != '\n') {
			cmd = String.format(" %-7s ", op);
		} else {
			cmd = String.format("%-25s %-7s ", "", op);
		}
		secCode.append(cmd + "\r\n");
		//System.out.println(cmd);
	}
	public void asmDataLable(String lbl) {
		String cmd = String.format("%-25s", lbl);
		secData.append(cmd);
	}	
	public void asmCodeLable(String lbl) {
		String cmd = String.format("%-25s", lbl);
		secCode.append(cmd);
	}
	public String asmCmd(String lbl, String op, String arg1, String arg2, String arg3, boolean isCode) {
		String cmd;
		if (arg2.isEmpty()) {
			if (isCode && lbl.isEmpty() && secCode.charAt(secCode.length() - 1) != '\n') {
				cmd = String.format(" %-7s %s ", op, arg1);
			} else {
				cmd = String.format("%-25s %-7s %s ", lbl, op, arg1);
			}
		} else if (arg3.isEmpty()) {
			if (isCode && lbl.isEmpty() && secCode.charAt(secCode.length() - 1) != '\n') {
				cmd = String.format(" %-7s %s, %s ", op, arg1, arg2);
			} else {
				cmd = String.format("%-25s %-7s %s, %s ", lbl, op, arg1, arg2);
			}
		} else {
			if (isCode && lbl.isEmpty() && secCode.charAt(secCode.length() - 1) != '\n') {
				cmd = String.format(" %-7s %s, %s, %s ", op, arg1, arg2, arg3);
			} else {
				cmd = String.format("%-25s %-7s %s, %s, %s ", lbl, op, arg1, arg2, arg3);
			}
		}
		return cmd;
	}
	public void asmData(String lbl, String op, String arg1, String arg2, String arg3) {
		secData.append(asmCmd(lbl, op, arg1, arg2, arg3, false) + "\r\n");
	}
	public void asmCode(String lbl, String op, String arg1, String arg2, String arg3) {
		secCode.append(asmCmd(lbl, op, arg1, arg2, arg3, true) + "\r\n");
	}
	public void asmCode(String lbl, String op, String arg1, String arg2, String arg3, String arg4) {
		String cmd;
		if (arg2.isEmpty()) {
			if (lbl.isEmpty() && secCode.charAt(secCode.length() - 1) != '\n') {
				cmd = String.format(" %-7s %s \t\t\t%s", op, arg1, arg4);
			} else {
				cmd = String.format("%-25s %-7s %s \t\t\t%s", lbl, op, arg1, arg4);
			}
		} else if (arg3.isEmpty()) {
			if (lbl.isEmpty() && secCode.charAt(secCode.length() - 1) != '\n') {
				cmd = String.format(" %-7s %s, %s \t\t%s", op, arg1, arg2, arg4);
			} else {
				cmd = String.format("%-25s %-7s %s, %s \t\t%s", lbl, op, arg1, arg2, arg4);
			}
		} else {
			if (lbl.isEmpty() && secCode.charAt(secCode.length() - 1) != '\n') {
				cmd = String.format(" %-7s %s, %s, %s \t%s", op, arg1, arg2, arg3, arg4);
			} else {
				cmd = String.format("%-25s %-7s %s, %s, %s \t%s", lbl, op, arg1, arg2, arg3, arg4);
			}
		}
		secCode.append(cmd + "\r\n");
	}
	public void asmStartASM() {
		asmData("% Data Section");
		asmCodeLable(" ");
		asmCode("% Code Section");	
	}
	public void asmStartProg() {
		asmCode("");
		asmCode("entry");
		asmCode("","addi","r14","r0","topaddr", "% set r14 to topaddr");
	}
	public void asmEndProg() {
		asmCode("hlt");
	}
	public void asmGenDataAndCode() {
		SysLogger.asm(secData.toString());
		//SysLogger.asm("\n");
		SysLogger.asm(secCode.toString() + secCodeEnd.toString() );
	}
	public void asmVarDefinition(Symbol s) {//TODO what about free func and main func ???
		if (s.self.parent != null && s.self.parent.symbolType == SYMBOLTYPE.CLASS) {
			// class member, do not need to allocate address
			return;
		} 
		
		if (s.isArray) {
			asmData(s.address, "res", "" + semAct.getArrayTotalSize(s), "", "");
		} else {
			if (s.size > 4 && s.symbolType == SYMBOLTYPE.VARIABLE) {
				asmData(s.address, "res", "" + (s.size), "", "");
			} else {
				asmData(s.address, "dw", "0", "", "");
			}
		}
	}
	
	private String strFuncEntry = "_e";
	public void asmFuncDefinition(Symbol s) {
		asmData(s.address, "dw", "0", "", "");//TODO we do not need this 
		asmCode("% Function definition: " + s.tk.token);
		asmCodeLable(s.address + strFuncEntry);
	}
	
	public String createTempAddr(Symbol a) {
		String addr = a.tk.line + "_" + a.tk.column + "_" + (new Random()).nextInt(999999);
		return addr;
	}
	private String createTempAddr(Token tk) {
		String addr = tk.line + "_" + tk.column + "_" + (new Random()).nextInt(999999);
		return addr;
	}
	
	public int asmOPNot(Symbol s) {
		//asmCode("% not " + s.tk.token);		
		String addr = "expr_" + createTempAddr(s);
		asmLW(s, "r1", "% not " + s.tk.token);
		asmCode("", "not", "r3", "r1", "");
		asmData(addr, "dw", "0", "", "");
		asmCode("", "sw", addr + "(r0)", "r3", "");
		s.address = addr;
		if (s.symbolType == SYMBOLTYPE.NUMBER) {
			s.symbolType = SYMBOLTYPE.UNKNOWN;		// it is not a number anymore
		}
		return 0;
	}
	public int asmOPSign(Symbol s) {
		//asmCode("% - " + s.tk.token);		
		String addr = "expr_" + createTempAddr(s);
		asmCode("", "add", "r1", "r0", "r0", "\t\t\t% - " + s.tk.token);
		asmLW(s, "r2", "");	
		asmCode("", "sub", "r3", "r1", "r2");
		asmData(addr, "dw", "0", "", "");
		asmCode("", "sw", addr + "(r0)", "r3", "");
		s.address = addr;
		if (s.symbolType == SYMBOLTYPE.NUMBER) {
			s.symbolType = SYMBOLTYPE.UNKNOWN;		// it is not a number anymore
		}
		return 0;
	}
	
	public String asmOPIfThen(Symbol s) {
		//asmCode("% if " + s.tk.token + " then");		
		asmLW(s, "r1", "% if " + s.tk.token + " then");	
		String addr = "else_" + createTempAddr(s);
		asmCode("", "bz", "r1", addr, "");

		return addr;
	}

	public void asmLW(Symbol s, String r, String m) {
		// r12 is reserved for floating number.
		if (s.symbolType == SYMBOLTYPE.NUMBER) {
			String addr = "num_" + createTempAddr(s);

			if (s.dataType.token.equals("real")) {
				String[] lst = s.tk.token.split("\\.");
				if (lst[0].isEmpty()) {
					asmCode("", "addi", "r12", "r0", "0", "\t\t\t" + m);
				} else {
					asmData(addr, "dw", lst[0], "", "");
					asmCode("", "lw", "r12", addr + "(r0)", "", "\t\t\t" + m);
					//asmCode("", "addi", "r12", "r0", lst[0], "\t\t\t" + m);
					asmCode("", "muli", "r12", "r12", floatMask);
				}
				//asmCode("", "sl", "r12", "8", "");
				//asmCode("", "addi", r, "r12", lst[1]);
				Double f = Double.parseDouble("0." + lst[1]);
				int mask = Integer.parseInt(floatMask);

				asmCode("", "addi", r, "r12", "" + ((int)(f * mask)) % mask);
			} else {
				asmData(addr, "dw", s.tk.token, "", "");
				asmCode("", "lw", r, addr + "(r0)", "", "\t\t\t" + m);
				//asmCode("", "addi", r, "r0", s.tk.token, "\t\t\t" + m);
			}
			return;
		}
		
		// if it is a member variable, get start address from func address
		if (semAct.getStBak() == null) {
			return;
		}
		Symbol c = null, v = null;
		boolean findit = false;
		int offset = 0;
		
		if (s.symbolType != SYMBOLTYPE.FUNCTION) {
			for (int i = 0; i < semAct.getStBak().symbols.size(); i++) {
				c = semAct.getStBak().symbols.get(i);
				offset = 0;
				if (c.symbolType == SYMBOLTYPE.CLASS) {
					for (int j = 0; j < c.child.symbols.size(); j++) {
						v = c.child.symbols.get(j);
						if (v.address.equals(s.address)) {
							findit = true;
							break;
						}
						if (v.isArray) {
							offset += semAct.getArrayTotalSize(v);
						} else {
							offset += v.size;
						}
					}
					if (findit) {
						break;
					}
				}
			}
		}
		
		if (s.symbolType == SYMBOLTYPE.CHKMEMBER ||
				(s.symbolType != SYMBOLTYPE.UNKNOWN && s.isArray)) {
			if (s.ifPassedByAddress) {
				// get address of the class instance
				if (findit) {
					// get class instance address
					asmCode("", "lw", r, c.address + "(r0)", "", "% Get variable address. " + m);
					asmCode("", "addi", "r10", "r0", "" + offset, "% member variable offset");
					asmCode("", "add", r, r, "r10");
				} else {
					asmCode("", "lw", r, s.address + "(r0)", "", m + ". pass by address");
				}
				// get the address of the member
				asmCode("", "add", r, r, "r11");
				// get the value
				asmCode("", "lw", r, "0(" + r + ")", "");
			} else {
				if (findit) {
					asmCode("", "lw", r, c.address + "(r0)", "", "% Get variable address. " + m);
					asmCode("", "addi", "r10", "r0", "" + offset, "% member variable offset");
					asmCode("", "add", r, r, "r10");
					asmCode("", "add", r, r, "r11");
					asmCode("", "lw", r, "0(" + r + ")", "");
				} else {
//					System.out.println(s.tk.token  +  "     "  + s.dataType.token  + "    "  + s.className  + s.offset );
					asmCode("", "lw", r, s.address + "(r11)", "", m);
					asmCode("", "addi", "r11", "r0", "" + s.offset, "% reset offset: " + s.offset);
				}
			}
		} else {
			if (findit) {
				asmCode("", "lw", r, c.address + "(r0)", "", "% Get variable address. " + m);
				asmCode("", "addi", "r10", "r0", "" + offset, "% member variable offset");
				asmCode("", "add", r, r, "r10");
				asmCode("", "lw", r, "0(" + r + ")", "");
			} else {
				asmCode("", "lw", r, s.address + "(r0)", "", m);
			}
		}
	}
	public void asmSW(Symbol s) {
		// if it is a member variable, get start address from func address
		if (semAct.getStBak() == null) {
			return;
		}
		Symbol c = null, v = null;
		boolean findit = false;
		int offset = 0;
		
		if (s.symbolType != SYMBOLTYPE.FUNCTION) {
			for (int i = 0; i < semAct.getStBak().symbols.size(); i++) {
				c = semAct.getStBak().symbols.get(i);
				offset = 0;
				if (c.symbolType == SYMBOLTYPE.CLASS) {
					for (int j = 0; j < c.child.symbols.size(); j++) {
						v = c.child.symbols.get(j);
						if (v.address.equals(s.address)) {
							findit = true;
							break;
						}
						if (v.isArray) {
							offset += semAct.getArrayTotalSize(v);
						} else {
							offset += v.size;
						}
					}
					if (findit) {
						break;
					}
				}
			}
		}
		
		if (s.symbolType == SYMBOLTYPE.CHKMEMBER ||
				(s.symbolType != SYMBOLTYPE.UNKNOWN && s.isArray)) {
			asmPopR("r11");
			if (s.ifPassedByAddress) {
				// get address of the class instance			
				if (findit) {
					asmCode("", "lw", "r2", c.address + "(r0)", "", "% Get variable address ");
					asmCode("", "addi", "r10", "r0", "" + offset, "% member variable offset");
					asmCode("", "add", "r2", "r2", "r10");
				} else {
					asmCode("", "lw", "r2", s.address + "(r0)", "", "% pass by address");
				}
				// get the address of the member
				asmCode("", "add", "r2", "r2", "r11");
				// get the value
				asmCode("", "sw", "0(r2)", "r1", "");				
			} else {
				if (findit) {
					asmCode("", "lw", "r2", c.address + "(r0)", "", "% Get variable address ");
					asmCode("", "addi", "r10", "r0", "" + offset, "% member variable offset");
					asmCode("", "add", "r2", "r2", "r10");
					asmCode("", "add", "r2", "r2", "r11");
					asmCode("", "sw", "0(r2)", "r1", "");
				} else {
					asmCode("", "sw", s.address + "(r11)", "r1", "");
				}
			}
		} else {
			if (findit) {
				asmCode("", "lw", "r2", c.address + "(r0)", "", "% Get variable address ");
				asmCode("", "addi", "r10", "r0", "" + offset, "% member variable offset");
				asmCode("", "add", "r2", "r2", "r10");
				asmCode("", "sw", "0(r2)", "r1", "");
			} else {
				asmCode("", "sw", s.address + "(r0)", "r1", "");
			}
		}
	}
	
	public String asmOPIfElse(Symbol s, String elseAddr) {
		asmCode("% if ... else ");
		
		String addr = "endif_" + createTempAddr(s);
		asmCode("", "j", addr, "", "");
		//asmCode(elseAddr, "nop", "", "", "");
		asmCodeLable(elseAddr);
		return addr;
	}
	public void asmOPIfEndif(Symbol s, String endifAddr) {
		asmCode("% if ... endif ");		
		//asmCode(endifAddr, "nop", "", "", "");
		asmCodeLable(endifAddr);
	}
	public String asmOPWhile(Token tk) {
		String addr = "gowhile_" + createTempAddr(tk);
		asmCodeLable(addr);
		return addr;
	}
	public String asmOPWhileDo(Symbol s) {
		asmLW(s, "r1", "% while " + s.tk.token + " do");	
		String addr = "endwhile_" + createTempAddr(s);

		asmCode("", "bz", "r1", addr, "");
		return addr;
	}
	public void asmOPIfEndWhile(String goWhile, String endWhile) {
		asmCode("", "j", goWhile, "", "");
		asmCode("% while ... end ");		
		asmCodeLable(endWhile);
	}	
	
	// push data into stack and pop data from stack
	// r13, r14 are reserved for stack
	public int asmPush(Symbol s, int args) {
		asmLW(s, "r13", "% Push");
		asmCode("", "sw", "-"+args+"(r14)", "r13", "% Push parm");
		return 0;
	}
	public int asmPushAndShift(Symbol s, String shift) {
		asmLW(s, "r13", "% Push");
		//asmCode("", shift, "r13", "8", "", "% shift parameter");
		asmCode("", shift, "r13", "r13", floatMask, "% shift parameter");
		asmCode("", "subi", "r14", "r14", "4");
		asmCode("", "sw", "topaddr(r14)", "r13", "");
		return 0;
	}
	public int asmPushR(String r) {
//		asmCode("", "subi", "r14", "r14", "4", "\t\t% push " + r);
//		asmCode("", "sw", "topaddr(r14)", r, "");
		return 0;
	}

	public void asmPushOffset(Symbol s) {
		if (s.symbolType == SYMBOLTYPE.CHKMEMBER ||
				(s.symbolType != SYMBOLTYPE.UNKNOWN && s.isArray)) {
			//asmPushR("r11");
		}
	}
	public void asmPopOffset(Symbol s) {
		if (s.symbolType == SYMBOLTYPE.CHKMEMBER ||
				(s.symbolType != SYMBOLTYPE.UNKNOWN && s.isArray)) {
			asmPopR("r11");
		}
	}
//	public void asmResetOffset() {
//		asmCode("", "add", "r11", "r0", "r0", "% reset offset");
//	}
	public int asmPop(Symbol s , int args) {
		asmCode("", "lw" , "r13", "-"+args+"(r14)","% Pop args" );
		asmCode("", "sw", s.address + "(r0)", "r13", "");
//		asmCode("", "lw", "r13", "topaddr(r14)", "", "% Pop");
//		asmCode("", "addi", "r14", "r14", "4");
//		asmCode("", "sw", s.address + "(r0)", "r13", "");
		return 0;
	}
	public int asmPopR(String r) {
//		asmCode("", "lw", r, "topaddr(r14)", "", "% Pop " + r);
//		asmCode("", "addi", "r14", "r14", "4");
		return 0;
	}
	
	// function definition
	public int asmPopFunctionParams(Symbol func) {
		// pop the class instance address first
		if (semAct.getStCur().parent != null && semAct.getStCur().parent.self.parent != null) {
			// it is member function, get the class address first
			//asmPop(func.self.parent);		// TODO: temporarily store in the class address.
		}
		
		asmCode("", "sw", "-4(r14)", "r15", "% Save link");  // save the link for recursive call
		
		int args = 8;
		for (int i = 0; i < semAct.getStCur().symbols.size() ; i++) {
			Symbol s = semAct.getStCur().symbols.get(i);
			if (s.symbolType == SYMBOLTYPE.PARAMETER) {
				if ((s.dataType.token.equals("integer") || s.dataType.token.equals("real") && !s.isArray)) {
					asmPop(s , args);		// pass by value
					args +=4;
				} else {
					// get the address first
					asmPopR("r1");
					//asmCode("", "lw", "r1", "0(r2)", "", "% get value of address");      
					asmCode("", "sw", s.address + "(r0)", "r1", "");
				}
			}
		}
		asmCode("", "subi", "r14", "r14","32" ,"% Adjust SP");
		return 0;
	}
	public int asmEndOfFuncDefinition() {
		// go back to the calling PC
		// r15 is reserved for calling link.
		asmCode("", "addi", "r14", "r14","32" ,"% Adjust SP");
		asmCode("", "lw", "r15", "-4(r14)" ," % Restore link");
		asmCode("", "jr", "r15", "", "");
		return 0;
	}
	public int asmFuncReturn(Symbol s) {
		// check the types
		if (semAct.getStCur().parent != null && semAct.getStCur().parent.symbolType == SYMBOLTYPE.FUNCTION) {
			semAct.compDateType(semAct.getStCur().parent, s);  //TODO what 
			return 0;
		}
		SysLogger.err("asmFuncReturn: " + s.tk.token);
		return -1;
	}	
	
	// function calling
	public int asmCallingFunc(Symbol s, Symbol cls) {
		if (s.address == null) {		// stBak == null
			return -1;
		}

		
		// push all parameters into stack
		int args = 8 ;
		for (int i = 0; i < semAct.getVarFuncParams().size(); i++) {
			if (semAct.getVarFuncParams().size() == semAct.getVarFuncParamsAttr().size()) {//TODO delete
				if (semAct.getVarFuncParamsAttr().get(i).equals("integer") 
						&& semAct.getVarFuncParams().get(i).dataType.token.equals("real")) {
					//asmPushAndShift(varFuncParams.get(i), "sr");
					asmPushAndShift(semAct.getVarFuncParams().get(i), "divi");
					continue;
				}
				if (semAct.getVarFuncParamsAttr().get(i).equals("real") 
						&& semAct.getVarFuncParams().get(i).dataType.token.equals("integer")) {
					//asmPushAndShift(varFuncParams.get(i), "sl");
					asmPushAndShift(semAct.getVarFuncParams().get(i), "muli");
					continue;
				}
			}
			if ((semAct.getVarFuncParams().get(i).dataType.token.equals("integer")
					|| semAct.getVarFuncParams().get(i).dataType.token.equals("real")) && !semAct.getVarFuncParams().get(i).isArray  ) {
				// parameter passed by value
				asmPush(semAct.getVarFuncParams().get(i) , args);
				args +=4;
			} else {
				// passed by address
				
				asmCode("", "addi", "r13", "r0", semAct.getVarFuncParams().get(i).address, "% pass by address");
				asmCode("", "sw", "-"+args+"(r14)", "r13", "% Push object address as parm");
				args +=4;
			}
		}
		
		// push class instance address into stack
		if (cls.symbolType == SYMBOLTYPE.CHKMEMBER || 
				(semAct.getStCur().parent != null && semAct.getStCur().parent.self.parent != null && cls.self != null &&
				semAct.getStCur().parent.self.parent.address.equals(cls.self.parent.address))) {
			// get the offset of all member variables
			Symbol c = null, v = null;
			int offset = 0;
			
			if (semAct.getStBak() == null) {
				return 0;
			}
			for (int i = 0; i < semAct.getStBak().symbols.size(); i++) {
				c = semAct.getStBak().symbols.get(i);
				if (c.symbolType == SYMBOLTYPE.CLASS && c.tk.token.equals(cls.className)) {
					offset = 0;
					for (int j = 0; j < c.child.symbols.size(); j++) {
						v = c.child.symbols.get(j);
						if (v.symbolType == SYMBOLTYPE.VARIABLE) {
							if (v.isArray) {
								offset += semAct.getArrayTotalSize(v);
							} else {
								offset += v.size;
							}
						}
					}
				}
			}
			
			// if cls is a member of this function
			boolean ifLocalVar = false;
			for (int i = 0; i < semAct.getStCur().symbols.size(); i++) {
				Symbol m = semAct.getStCur().symbols.get(i);
				if (m.address.equals(cls.address)) {
					ifLocalVar = true;
					break;
				}
			}
			
			// if it is class function, calculate the real class instance address
			if (!ifLocalVar && semAct.getStCur().parent != null && semAct.getStCur().parent.self.parent != null 
					&& semAct.getStCur().parent.self.parent.symbolType == SYMBOLTYPE.CLASS) {
				int cls_offset = 0;
				for (int j = 0; j < semAct.getStCur().parent.self.symbols.size(); j++) {
					v = semAct.getStCur().parent.self.symbols.get(j);
					if (v.address.equals(cls.address)) {
						break;
					}
					if (v.symbolType == SYMBOLTYPE.VARIABLE) {
						if (v.isArray) {
							cls_offset += semAct.getArrayTotalSize(v);
						} else {
							cls_offset += v.size;
						}
					}
				}
				if (cls.self != null && semAct.getStCur().parent.self.parent.address.equals(cls.self.parent.address)) {
					// call class function which is located in the same class
					asmCode("", "lw", "r1", semAct.getStCur().parent.self.parent.address + "(r0)", "", "% push class instance address");
				} else {
					asmCode("", "lw", "r1", semAct.getStCur().parent.self.parent.address + "(r0)", "", "% offset of class var");
					asmCode("", "addi", "r1", "r1", "" + cls_offset);
					asmCode("", "subi", "r11", "r11", "" + offset);
					asmCode("", "add", "r1", "r1", "r11", "% push class instance address");
				}
				
			} else {
				// subtract offset from r11
				asmCode("", "subi", "r11", "r11", "" + offset);
				asmCode("", "addi", "r1", "r11", cls.address, "% push class instance address");
			}
//			asmPushR("r1");
		}
		
		// entry of function call
		asmCode("", "jl", "r15", s.address + strFuncEntry, "", "% call a function");
		
//		// after calling the function
//		// pop r15
//		asmPopR("r15");
//		
//		// check if it is a recursively calling
//		if (semAct.getStCur().parent != null && semAct.getStCur().parent.address.equals(s.address)) {
//			// pop all the local variables of the function in the reverse order
//			for (int i = semAct.getStCur().symbols.size() - 1; i >= 0; i--) {
//				Symbol tmp = semAct.getStCur().symbols.get(i);
//				if (tmp.symbolType == SYMBOLTYPE.VARIABLE) {
//					if (tmp.dataType.token.equals("integer") || tmp.dataType.token.equals("real")) {
//						//asmPop(tmp);
//					} else {
//						// TODO:
//					}
//				}
//				// and pop parameters
//				if (tmp.symbolType == SYMBOLTYPE.PARAMETER) {
//					if (tmp.dataType.token.equals("integer") || tmp.dataType.token.equals("real")) {
//						//asmPop(tmp);
//					}
//				}
//			}
//		}
		return 0;
	}
	
	// write & read
	private static String floatMask = "1000";
	public int asmWrite(Symbol s) {
//		asmPushR("r15");
		asmLW(s, "r1", "% write " + s.tk.token);
		
		if (s.dataType.token.equals("real")) {//TODO it never gonna reach this step 
			String addr = "PositiveFloat_" + (new Random()).nextInt(999) + (new Random()).nextInt(9999999);

			asmCode("", "clt", "r2", "r1", "r0");		// if r1 < 0
			asmCode("", "bz", "r2", addr, "");
			
			asmCode("", "sub", "r1", "r0", "r1", "% NegativeFloat");	// -11.22 -> 11.22
			asmCode("", "addi", "r2", "r0", "45");		// print '-'
			asmCode("", "putc", "r2", "", "");			
			
			asmCode(addr, "add", "r12", "r0", "r1");	// save float number		
			//asmCode("", "sr", "r1", "8", "");			// print left part
			asmCode("", "divi", "r1", "r1", floatMask);
			asmCode("", "jl", "r15", "putint", "");			
			
			asmCode("", "addi", "r1", "r0", "46");		// print '.'
			asmCode("", "putc", "r1", "", "");
			
			asmCode("", "add", "r1", "r0", "r12", "% fraction");	// load float number again
			//asmCode("", "sl", "r1", "24", "");			// print right part
			//asmCode("", "sr", "r1", "24", "");
			asmCode("", "modi", "r1", "r1", floatMask);
			
			// add '0'
			String addr1 = "FloatAdd00_" + (new Random()).nextInt(999) + (new Random()).nextInt(9999999);
			String addr2 = "FloatAdd0_" + (new Random()).nextInt(999) + (new Random()).nextInt(9999999);
			asmCode("", "cgei", "r2", "r1", "100");		// if r1 >= 100
			asmCode("", "bnz", "r2", addr1, "");
			asmCode("", "cgei", "r2", "r1", "10");		// if r1 >= 10
			asmCode("", "bnz", "r2", addr2, "");
			asmCode("", "addi", "r2", "r0", "48");		// add 0
			asmCode("", "putc", "r2", "", "");		
			asmCode(addr2, "addi", "r2", "r0", "48");	// add 0
			asmCode("", "putc", "r2", "", "");		

			asmCode(addr1, "jl", "r15", "putint", "");
		} else{
			s.address = s.address + "write";
			String strToWrite;
			strToWrite = s.writeExpr;
			
			asmData(s.address, "db", "\"" + strToWrite + "\",13,10,0" , "", "");
			String printInt = "print"+s.address;
			asmCode("", "jl", "r15", printInt , "");
			addWritExp(s , printInt);
		}
		
		asmPopR("r15");
		return 0;
	}
	
	public void addWritExp(Symbol s, String label ){
		String cmd;
		
		cmd = String.format("%-25s", label);
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s, %s", "","lb", "r3", s.address+"(r2)");
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s, %s, %s", "", "ceqi", "r4", "r3", "0");
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s, %s", "" , "bnz", "r4", "D" + s.address );
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s %s", "" ,"putc", "r3", ""); //ddddd
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s, %s, %s", "" , "addi", "r2", "r2", "1");
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s", "" ,"j", label);
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format("%-25s", "D" + s.address);
		secCodeEnd.append(cmd + "\r\n");
		cmd = String.format(" %-25s %-7s %s", "" ,"jr", "r15");
		secCodeEnd.append(cmd + "\r\n");
	}
	
	boolean wroteGetInt = false ;
	public int asmRead(Symbol s) {
		asmCode("", "jl", "r15", "getint", "", "% read: " + s.tk.token);
		asmSW(s);
		if(!wroteGetInt){
			wroteGetInt = true;
			writeGetIntFun();
		}
		return 0;
	}

	public void writeGetIntFun(){
		String cmd ;
		asmData("putint9", "res", " 12" , "", "");
		asmData("getint9", "res", " 12" , "", "");
		
		cmd = String.format("%-25s", "getint");
		secCodeEnd.append(cmd);
		AddTosecCodeEnd("", "add", "r1", "r0", "r0");
		AddTosecCodeEnd("", "add", "r2", "r0", "r0");
		AddTosecCodeEnd( "", "add", "r4", "r0", "r0");
		
		cmd = String.format("%-25s", "getint1");
		secCodeEnd.append(cmd);
		AddTosecCodeEnd("","getc", "r1", "" , "");
		AddTosecCodeEnd("", "ceqi", "r3", "r1", "43");
		AddTosecCodeEnd("", "bnz", "r3", "getint1", "");
		AddTosecCodeEnd("", "ceqi", "r3", "r1", "43");
		AddTosecCodeEnd("", "bz", "r3", "getint2" , "");
		AddTosecCodeEnd("", "addi", "r4", "r0", "1");
		AddTosecCodeEnd("", "j", "getint1" , "" , "");

		
		cmd = String.format("%-25s", "getint2");
		secCodeEnd.append(cmd );
		AddTosecCodeEnd("", "clti", "r3", "r1", "48");
		AddTosecCodeEnd("" , "bnz", "r3", "getint3" , "");
		AddTosecCodeEnd("", "cgti", "r3", "r1", "57");
		AddTosecCodeEnd("" , "bnz", "r3", "getint3", "");
		AddTosecCodeEnd("" , "sb", "getint9(r2)", "r0", "");
		AddTosecCodeEnd("", "addi", "r2", "r2", "1");
		AddTosecCodeEnd("" , "j", "getint1", "", "");
		
		
		cmd = String.format("%-25s", "getint3");
		secCodeEnd.append(cmd);
		AddTosecCodeEnd("", "sb", "getint9(r2)", "r0" , "");
		AddTosecCodeEnd("", "add", "r2", "r0", "r0");
		AddTosecCodeEnd("", "add", "r1", "r0", "r0");
		AddTosecCodeEnd("", "add", "r3", "r0", "r0");

		
		cmd = String.format("%-25s", "getint4");
		secCodeEnd.append(cmd);
		AddTosecCodeEnd("", "lb", "r3", "getint9(r2)" , "");
		AddTosecCodeEnd("", "bz", "r4", "getint5", "");
		AddTosecCodeEnd("", "subi", "r3", "r3", "48");
		AddTosecCodeEnd("", "muli", "r1", "r1", "10");
		AddTosecCodeEnd("", "add", "r1", "r1", "r3");
		AddTosecCodeEnd("", "addi", "r2", "r2", "1");
		AddTosecCodeEnd("", "j", "getint4", "", "");
		
		cmd = String.format("%-25s", "getint5");
		secCodeEnd.append(cmd);
		AddTosecCodeEnd("", "bz", "r4", "getint6" , "");
		AddTosecCodeEnd("", "sub", "r1", "r0", "r1");
		
		cmd = String.format("%-25s", "getint6");
		secCodeEnd.append(cmd);
		AddTosecCodeEnd("", "jr", "r15", "", "");	
	}
	
	
	public int asmGenMathExpr(Symbol a, Symbol b, Token tkOp, int flag) {
		//asmCode("% " + a.tk.token + tkOp.token + b.tk.token);	
		if (flag == 0 || flag == 3) {
				asmLW(b, "r2", "% " + a.tk.token + tkOp.token + b.tk.token);
			
				asmLW(a, "r1", "");			
		} else {
				asmLW(b, "r2", "% integer -> real: " + b.tk.token);
			//asmCode("", "sl", "r2", "8", "");
				asmCode("", "muli", "r2", "r2", floatMask);
			if (flag == 1) {
				asmLW(a, "r1", "% " + b.tk.token + tkOp.token + a.tk.token);
			} else {
				asmLW(a, "r1", "% " + a.tk.token + tkOp.token + b.tk.token);
			}			
		} 
		// ASM operation
		String asmOp = "add";
		if (tkOp.token.equals("+")) {
			asmOp = "add";
		} else if (tkOp.token.equals("-")) {
			asmOp = "sub";
		} else if (tkOp.token.equals("*")) {
			asmOp = "mul";
		} else if (tkOp.token.equals("/")) {
			asmOp = "div";
			if (flag == 1) {
				//asmCode("", "sl", "r2", "8", "", "% << 8");
				asmCode("", "muli", "r2", "r2", floatMask, "% mul " + floatMask);
			} else if (flag != 0) {
				//asmCode("", "sl", "r1", "8", "", "% << 8");
				asmCode("", "muli", "r1", "r1", floatMask, "% mul " + floatMask);
			}			
			//asmCode("", "sl", "r2", "8", "", "% << 8");
		} else if (tkOp.token.equals("and")) {
			asmOp = "and";
		} else if (tkOp.token.equals("or")) {
			asmOp = "or";
		} else if (tkOp.token.equals("==")) {
			asmOp = "ceq";
		} else if (tkOp.token.equals("<>")) {
			asmOp = "cne";
		} else if (tkOp.token.equals("<")) {
			asmOp = "clt";
		} else if (tkOp.token.equals("<=")) {
			asmOp = "cle";
		} else if (tkOp.token.equals(">")) {
			asmOp = "cgt";
		} else if (tkOp.token.equals(">=")) {
			asmOp = "cge";
		} 
		if (flag == 1) {
			asmCode("", asmOp, "r3", "r2", "r1");
		} else {
			asmCode("", asmOp, "r3", "r1", "r2");
		} 
		if (flag != 0 && tkOp.token.equals("*")) {
			//asmCode("", "sr", "r3", "8", "", "% >> 8");
			asmCode("", "divi", "r3", "r3", floatMask, "% div " + floatMask);
		}
		
		// create a temporary address for the result
		String addr = "expr_" + createTempAddr(a);
			asmData(addr, "dw", "0", "", "");
			asmCode("", "sw", addr + "(r0)", "r3", "");
		a.address = addr;		// change the address of the symbol.
		if (a.symbolType == SYMBOLTYPE.NUMBER) {
			a.symbolType = SYMBOLTYPE.UNKNOWN;		// it is not a number anymore
		}
		// the value has been stored into the temporary address.
		//a.isArray = false;
		//asmResetOffset();
		a.symbolType = SYMBOLTYPE.UNKNOWN;
		return 0;
	}
	
	
	public void AddTosecCodeEnd(String lbl, String op, String arg1, String arg2, String arg3){
		secCodeEnd.append(asmCmd(lbl, op, arg1, arg2, arg3, true) + "\r\n");
	}
	

	public SemanticActions getSemAct() {
		return semAct;
	}

	public void setSemAct(SemanticActions semAct) {
		this.semAct = semAct;
	}
	
	
}
