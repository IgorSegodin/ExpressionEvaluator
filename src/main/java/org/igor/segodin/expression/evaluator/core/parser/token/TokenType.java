package org.igor.segodin.expression.evaluator.core.parser.token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Known types of token, with specified regex patterns.
 *
 * @author igor
 */
public enum TokenType {

    NUMBER("^\\s*(?<token>\\d*|(\\d+\\.)|(\\d+\\.\\d+))\\s*$"),
    PLUS_OPERATOR("^\\s*(?<token>[+]?)\\s*$"),
    MINUS_OPERATOR("^\\s*(?<token>[\\-]?)\\s*$"),
    MULTIPLY_OPERATOR ("^\\s*(?<token>[*]?)\\s*$"),
    DIVIDE_OPERATOR("^\\s*(?<token>[\\/]?)\\s*$"),
    POW_OPERATOR("^\\s*(?<token>[\\^]?)\\s*$"),
    VARIABLE("^\\s*(?<token>[a-zA-Z_][a-zA-Z_0-9]*)*\\s*$"),
    OPEN_BRACE("^\\s*(?<token>[(]?)\\s*$"),
    CLOSE_BRACE("^\\s*(?<token>[)]?)\\s*$");
    // TODO TEXT type, to use as function arguments

    private Pattern pattern;

    private String extractExpression = "${token}"; // default value - named group 'token'

    TokenType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public boolean notMatches(String sample) {
        return !matches(sample);
    }

    public boolean matches(String sample) {
        return pattern.matcher(sample).matches();
    }

    /**
     * Returns token without unnecessary spaces
     * */
    public String extract(String sample) {
        Matcher matcher = pattern.matcher(sample);
        if (matcher.matches()) {
            return matcher.replaceAll(extractExpression);
        } else {
            throw new IllegalArgumentException("Unsupported token string: " + sample);
        }
    }
}
