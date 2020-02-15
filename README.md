					<h1>Compiler Design</h1>
			
Atomic lexical elements of the language <br>
	id ::= letter alphanum* <br>
	alphanum ::= letter | digit | _<br>
	num ::= integer | float <br>
	integer ::= nonzero digit* | 0 <br>
	float ::= integer fraction
	fraction ::= .digit* nonzero | .0 <br>
	letter ::= a..z |A..Z <br>
	digit ::= 0..9 <br>
	nonzero ::= 1..9 <br>
	
Operators, punctuation and reserved words <br>
	== 	+ 	( 	if <br>
	<> 	- 	) 	then <br>
	< 	* 	{ 	else <br>
	> 	/ 	} 	for <br> 
	<=	=	[	class <br>
	>= 	and 	] 	int <br>
	;	not 	/* 	float <br>
	, 	or 	*/	 get <br>
	. 	// 	put <br>
	return	program <br>
	
Grammar
	<prog> ::= <classDecl>*<progBody>
	<classDecl> ::= class id {<varDecl>*<funcDef>*};
	<progBody> ::= program<funcBody>;<funcDef>*
	<funcHead> ::= <type>id(<fParams>)
	<funcDef> ::= <funcHead><funcBody>;
	<funcBody> ::= {<varDecl>*<statement>*}
	<varDecl> ::= <type>id<arraySize>*;
	<statement> ::= <assignStat>;
	| if(<expr>)then<statBlock>else<statBlock>;
	| for(<type>id<assignOp><expr>;<relExpr>;<assignStat>)<statBlock>;
	| get(<variable>);
	| put(<expr>);
	| return(<expr>);
	<assignStat> ::= <variable><assignOp><expr>
	<statBlock> ::= {<statement>*} | <statement> | 
	<expr> ::= <arithExpr> | <relExpr>
	<relExpr> ::= <arithExpr><relOp><arithExpr>
	<arithExpr> ::= <arithExpr><addOp><term> | <term>
	<sign> ::= + | -
	<term> ::= <term><multOp><factor> | <factor>
	<factor> ::= <variable>
	| <idnest>*id(<aParams>)
	| num
	| (<arithExpr>)
	| not<factor>
	| <sign><factor>
	<variable> ::= <idnest>*id<indice>*
	<idnest> ::= id<indice>*.
	<indice> ::= [<arithExpr>]
	<arraySize> ::= [ integer ]
	<type> ::= int | float | id
	<fParams> ::= <type>id<arraySize>*<fParamsTail>* | 
	<aParams> ::= <expr><aParamsTail>* | 
	<fParamsTail> ::= ,<type>id<arraySize>*
	<aParamsTail> ::= ,<expr>

Operators and additional lexical conventions
	<assignOp> ::= =
	<relOp> ::= == | <> | < | > | <= | >=
	<addOp> ::= + | - | or
	<multOp> ::= * | / | and
	id ::= follows specification for identifiers found in assignment#1

