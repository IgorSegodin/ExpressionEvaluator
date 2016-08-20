package org.igor.segodin.expression.evaluator.core.parser.token;

import org.igor.segodin.expression.evaluator.core.parser.token.type.TokenType;

/**
 * Atomic presentation of single grammar element
 *
 * @author igor
 */
public class Token {

    private TokenType type;

    /**
     * String sample of a token without unnecessary spaces
     * */
    private String sample;

    /**
     * Start index in original input string (need for syntax error highlighting)
     * */
    private int startIndex;

    public Token(TokenType type, String sample, int startIndex) {
        this.type = type;
        this.sample = sample;
        this.startIndex = startIndex;
    }

    public TokenType getType() {
        return type;
    }

    public String getSample() {
        return sample;
    }

    public int getStartIndex() {
        return startIndex;
    }
}
