package dtool.parser;

import static dtool.util.NewUtils.assertNotNull_;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import dtool.ast.SourceRange;

public class Token {
	
	public final DeeTokens type;
	public final int startPos;
	public final String tokenSource;
	
	public Token(DeeTokens tokenCode, String source, int startPos) {
		this.type = assertNotNull_(tokenCode);
		this.tokenSource = source;
		this.startPos = startPos;
	}
	
	public final DeeTokens getTokenType() {
		return type;
	}
	
	public final int getStartPos() {
		return startPos;
	}
	
	public int getLength() {
		return tokenSource.length();
	}
	
	public int getEndPos() {
		return startPos + tokenSource.length();
	}
	
	public SourceRange getSourceRange() {
		return new SourceRange(getStartPos(), getLength());
	}
	
	public final String getSourceValue() {
		return tokenSource;
	}
	
	public LexerErrorTypes getError() {
		return null;
	}
	
	public static class ErrorToken extends Token {
		
		protected final LexerErrorTypes error;
		
		public ErrorToken(String value, int start, DeeTokens originalType, LexerErrorTypes error) {
			super(originalType, value, start);
			this.error = error;
			if(originalType == DeeTokens.INVALID_TOKEN || error == LexerErrorTypes.INVALID_CHARACTERS) {
				assertTrue(originalType == DeeTokens.INVALID_TOKEN);
				assertTrue(error == LexerErrorTypes.INVALID_CHARACTERS);
			}
		}
		
		@Override
		public LexerErrorTypes getError() {
			return error;
		}
	}
	
}