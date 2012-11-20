package dtool.parser;

public enum DeeTokens {
	
	EOF,
	ERROR,
	
	EOL,
	WHITESPACE,
	SCRIPT_LINE_INTRO,
	
	COMMENT_MULTI,
	COMMENT_NESTED,
	COMMENT_LINE,
	
	IDENTIFIER,
	
	STRING_WYSIWYG,
	STRING_DQ,
	STRING_HEX,
	STRING_DELIM,
	STRING_TOKENS,
	
	DIV,
	
	INTEGER,
	;
	
}
