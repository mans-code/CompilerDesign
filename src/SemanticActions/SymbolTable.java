
package SemanticActions;

import java.util.ArrayList;

public class SymbolTable implements Cloneable {
	// pointer to its parent symbol which created this symbol table;
	// null means the first table
	Symbol parent = null;			
	ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	
	String addrPrefix;		// prefix of all the symbol addresses of this table
	
    public Object clone(){
    	SymbolTable tk = null;
        try{
        	tk = (SymbolTable)super.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return tk;
    }
}
