				Design and implement

Atomic lexical elements of the language
	id ::= letter alphanum*
	alphanum ::= letter | digit | _
	num ::= integer | float
	integer ::= nonzero digit* | 0
	float ::= integer fraction
	fraction ::= .digit* nonzero | .0
	letter ::= a..z |A..Z
	digit ::= 0..9
	nonzero ::= 1..9


Operators, punctuation and reserved words
	== 	+ 	( 	if
	<> 	- 	) 	then
	< 	* 	{ 	else
	> 	/ 	} 	for
	<=	=	[ 	class
	>= 	and 	] 	int
	;	not 	/* 	float
	, 	or 	*/	 get
	. 	// 	put
	return	program

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

