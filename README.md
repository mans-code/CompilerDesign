<pre>
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
	&lt;&gt; 	- 	) 	then
	&lt; 	* 	{ 	else
	&gt; 	/ 	} 	for
	&lt;=	=	[ 	class
	&gt;= 	and 	] 	int
	;	not 	/* 	float
	, 	or 	*/	 get
	. 	// 	put
	return	program

Grammar
	&lt;prog&gt; ::= &lt;classDecl&gt;*&lt;progBody&gt;
	&lt;classDecl&gt; ::= class id {&lt;varDecl&gt;*&lt;funcDef&gt;*};
	&lt;progBody&gt; ::= program&lt;funcBody&gt;;&lt;funcDef&gt;*
	&lt;funcHead&gt; ::= &lt;type&gt;id(&lt;fParams&gt;)
	&lt;funcDef&gt; ::= &lt;funcHead&gt;&lt;funcBody&gt;;
	&lt;funcBody&gt; ::= {&lt;varDecl&gt;*&lt;statement&gt;*}
	&lt;varDecl&gt; ::= &lt;type&gt;id&lt;arraySize&gt;*;
	&lt;statement&gt; ::= &lt;assignStat&gt;;
	| if(&lt;expr&gt;)then&lt;statBlock&gt;else&lt;statBlock&gt;;
	| for(&lt;type&gt;id&lt;assignOp&gt;&lt;expr&gt;;&lt;relExpr&gt;;&lt;assignStat&gt;)&lt;statBlock&gt;;
	| get(&lt;variable&gt;);
	| put(&lt;expr&gt;);
	| return(&lt;expr&gt;);
	&lt;assignStat&gt; ::= &lt;variable&gt;&lt;assignOp&gt;&lt;expr&gt;
	&lt;statBlock&gt; ::= {&lt;statement&gt;*} | &lt;statement&gt; | 
	&lt;expr&gt; ::= &lt;arithExpr&gt; | &lt;relExpr&gt;
	&lt;relExpr&gt; ::= &lt;arithExpr&gt;&lt;relOp&gt;&lt;arithExpr&gt;
	&lt;arithExpr&gt; ::= &lt;arithExpr&gt;&lt;addOp&gt;&lt;term&gt; | &lt;term&gt;
	&lt;sign&gt; ::= + | -
	&lt;term&gt; ::= &lt;term&gt;&lt;multOp&gt;&lt;factor&gt; | &lt;factor&gt;
	&lt;factor&gt; ::= &lt;variable&gt;
	| &lt;idnest&gt;*id(&lt;aParams&gt;)
	| num
	| (&lt;arithExpr&gt;)
	| not&lt;factor&gt;
	| &lt;sign&gt;&lt;factor&gt;
	&lt;variable&gt; ::= &lt;idnest&gt;*id&lt;indice&gt;*
	&lt;idnest&gt; ::= id&lt;indice&gt;*.
	&lt;indice&gt; ::= [&lt;arithExpr&gt;]
	&lt;arraySize&gt; ::= [ integer ]
	&lt;type&gt; ::= int | float | id
	&lt;fParams&gt; ::= &lt;type&gt;id&lt;arraySize&gt;*&lt;fParamsTail&gt;* | 
	&lt;aParams&gt; ::= &lt;expr&gt;&lt;aParamsTail&gt;* | 
	&lt;fParamsTail&gt; ::= ,&lt;type&gt;id&lt;arraySize&gt;*
	&lt;aParamsTail&gt; ::= ,&lt;expr&gt;

Operators and additional lexical conventions
	&lt;assignOp&gt; ::= =
	&lt;relOp&gt; ::= == | &lt;&gt; | &lt; | &gt; | &lt;= | &gt;=
	&lt;addOp&gt; ::= + | - | or
	&lt;multOp&gt; ::= * | / | and
	id ::= follows specification for identifiers found in assignment#1

</pre>
