					<h1>Compiler Design</h1>
			
Atomic lexical elements of the language <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	id ::= letter alphanum* <br>
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
	
Grammar <br>
	<prog> ::= <classDecl>*<progBody> <br>
	<classDecl> ::= class id {<varDecl>*<funcDef>*}; <br>
	<progBody> ::= program<funcBody>;<funcDef>* <br>
	<funcHead> ::= <type>id(<fParams>) <br>
	<funcDef> ::= <funcHead><funcBody>; <br>
	<funcBody> ::= {<varDecl>*<statement>*} <br>
	<varDecl> ::= <type>id<arraySize>*; <br>
	<statement> ::= <assignStat>; <br>
	| if(<expr>)then<statBlock>else<statBlock>; <br>
	| for(<type>id<assignOp><expr>;<relExpr>;<assignStat>)<statBlock>; <br>
	| get(<variable>); <br>
	| put(<expr>);
	| return(<expr>); <br>
	<assignStat> ::= <variable><assignOp><expr> <br>
	<statBlock> ::= {<statement>*} | <statement> |    <br>
	<expr> ::= <arithExpr> | <relExpr>   <br>
	<relExpr> ::= <arithExpr><relOp><arithExpr>   <br> 
	<arithExpr> ::= <arithExpr><addOp><term> | <term> <br>
	<sign> ::= + | -  <br>
	<term> ::= <term><multOp><factor> | <factor> <br>
	<factor> ::= <variable> <br>
	| <idnest>*id(<aParams>) <br>
	| num   <br>
	| (<arithExpr>) <br>
	| not<factor> <br>
	| <sign><factor> <br> 
	<variable> ::= <idnest>*id<indice>* <br>
	<idnest> ::= id<indice>*.  <br>
	<indice> ::= [<arithExpr>] <br>
	<arraySize> ::= [ integer ] <br>
	<type> ::= int | float | id <br>
	<fParams> ::= <type>id<arraySize>*<fParamsTail>* |  <br>
	<aParams> ::= <expr><aParamsTail>* |   <br>
	<fParamsTail> ::= ,<type>id<arraySize>* <br>
	<aParamsTail> ::= ,<expr> <br>

Operators and additional lexical conventions   <br>
	<assignOp> ::= =   <br>
	<relOp> ::= == | <> | < | > | <= | >= <br>
	<addOp> ::= + | - | or <br>
	<multOp> ::= * | / | and <br>

