package org.igor.segodin.expression.evaluator.core.parser.grammar;

import org.igor.segodin.expression.evaluator.core.parser.token.Token;

/**
 * @author i.segodin
 */
public class GrammarException extends RuntimeException {

    private Token token;

    public GrammarException(String message, Token token) {
        super(message);
        this.token = token;
    }

    public GrammarException(String message, Throwable cause, Token token) {
        super(message, cause);
        this.token = token;
    }

    public GrammarException(Throwable cause, Token token) {
        super(cause);
        this.token = token;
    }

    public GrammarException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Token token) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
