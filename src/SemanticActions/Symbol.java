
package SemanticActions;

import java.util.ArrayList;
import LexicalAnalyzer.Token;

public class Symbol implements Cloneable {
	public static enum SYMBOLTYPE {
		UNKNOWN, CLASS, FUNCTION, VARIABLE, PARAMETER, 
		UNKNOWN_EXITTABLE, ARRAYSIZE, CHKVAR, CHKTYPE, CHKMEMBER,
		NUMBER, CIREFE};
	
	public SYMBOLTYPE symbolType;
	public Token tk;
	public Token dataType;
	public int size;				// size of the symbol
	public String address;			// unique name
	//public String addrTmp;			// temporary address used by expression
	
	public boolean ifAlreadyDefined = false;
	public boolean ifUnkownDataType = false;

	public SymbolTable self = null;		// pointer to the symbol table it is placed;

	// class or function
	public SymbolTable child = null;		// pointer to its symbol table;
	
	// array
	public boolean isArray = false;
	public int dimensions;		// the number of dimensions
	public ArrayList<Integer> sizeOfDimension = new ArrayList<Integer>();
	
	// class name
	public String className;
	public boolean ifPassedByAddress = false;
	
	public String writeExpr; 
	
	public int offset;
	public boolean dataMember  = false ; 
	public String  addresToPass;
	
    public Object clone(){
    	Symbol tk = null;
        try{
        	tk = (Symbol)super.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return tk;
    }
}
