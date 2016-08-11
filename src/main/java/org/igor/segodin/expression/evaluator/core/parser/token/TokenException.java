package org.igor.segodin.expression.evaluator.core.parser.token;

/**
 * @author igor
 */
public class TokenException extends RuntimeException {

    private int startIndex;

    private String sample;

    public TokenException(String message, int startIndex, String sample) {
        super(message);
        this.startIndex = startIndex;
        this.sample = sample;
    }

    public TokenException(String message, Throwable cause, int startIndex, String sample) {
        super(message, cause);
        this.startIndex = startIndex;
        this.sample = sample;
    }

    public TokenException(Throwable cause, int startIndex, String sample) {
        super(cause);
        this.startIndex = startIndex;
        this.sample = sample;
    }

    public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int startIndex, String sample) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.startIndex = startIndex;
        this.sample = sample;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getSample() {
        return sample;
    }
}
