					<h1>Compiler Design</h1>
			
Atomic lexical elements of the language <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	id ::= letter alphanum* <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	alphanum ::= letter | digit | _<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	num ::= integer | float <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	integer ::= nonzero digit* | 0 <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	float ::= integer fraction
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	fraction ::= .digit* nonzero | .0 <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	letter ::= a..z |A..Z <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	digit ::= 0..9 <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	nonzero ::= 1..9 <br>
	
Operators, punctuation and reserved words <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	== 	+ 	( 	if <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<> 	- 	) 	then <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	< 	* 	{ 	else <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	> 	/ 	} 	for <br> 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<=	=	[	class <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	>= 	and 	] 	int <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	;	not 	/* 	float <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	, 	or 	*/	 get <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	. 	// 	put <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	return	program <br>
	
Grammar <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	prog		::= <classDecl>*<progBody> <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	classDecl 	::= class id {<varDecl>*<funcDef>*}; <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	progBody  	::= program<funcBody>;<funcDef>* <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<funcHead> ::= <type>id(<fParams>) <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<funcDef> ::= <funcHead><funcBody>; <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<funcBody> ::= {<varDecl>*<statement>*} <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<varDecl> ::= <type>id<arraySize>*; <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<statement> ::= <assignStat>; <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| if(<expr>)then<statBlock>else<statBlock>; <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| for(<type>id<assignOp><expr>;<relExpr>;<assignStat>)<statBlock>; <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| get(<variable>); <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| put(<expr>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| return(<expr>); <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<assignStat> ::= <variable><assignOp><expr> <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<statBlock> ::= {<statement>*} | <statement> |    <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<expr> ::= <arithExpr> | <relExpr>   <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<relExpr> ::= <arithExpr><relOp><arithExpr>   <br> 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<arithExpr> ::= <arithExpr><addOp><term> | <term> <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<sign> ::= + | -  <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<term> ::= <term><multOp><factor> | <factor> <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<factor> ::= <variable> <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| <idnest>*id(<aParams>) <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| num   <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| (<arithExpr>) <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| not<factor> <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	| <sign><factor> <br> 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<variable> ::= <idnest>*id<indice>* <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<idnest> ::= id<indice>*.  <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<indice> ::= [<arithExpr>] <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<arraySize> ::= [ integer ] <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<type> ::= int | float | id <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<fParams> ::= <type>id<arraySize>*<fParamsTail>* |  <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	<aParams> ::= <expr><aParamsTail>* |   <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	fParamsTail ::= ,<type>id<arraySize>* <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	aParamsTail ::= ,<expr> <br>

Operators and additional lexical conventions   <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	assignOp ::=  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 	=   <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	relOp ::= 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; == | <> | < | > | <= | >= <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	addOp ::= 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; + | - | or <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	multOp ::= 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; * | / | and <br>

