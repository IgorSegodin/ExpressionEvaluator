package org.igor.segodin.expression.evaluator.core.parser.token.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Known types of token, with specified regex patterns.
 *
 * @author igor
 */
public enum StandardTokenTypes implements TokenType {

    /**
     * Simple number, can be decimal
     * */
    NUMBER("^\\s*(?<token>\\d*|(\\d+\\.)|(\\d+\\.\\d+))\\s*$"),

    /**
     * Arithmetic operators
     * */
    PLUS_OPERATOR("^\\s*(?<token>[+]?)\\s*$"),
    MINUS_OPERATOR("^\\s*(?<token>[\\-]?)\\s*$"),
    MULTIPLY_OPERATOR ("^\\s*(?<token>[*]?)\\s*$"),
    DIVIDE_OPERATOR("^\\s*(?<token>[\\/]?)\\s*$"),
    POW_OPERATOR("^\\s*(?<token>[\\^]?)\\s*$"),

    /**
     * Context variable, or function name
     * TODO can be nested property, separated with dots
     * */
    VARIABLE("^\\s*(?<token>[a-zA-Z_][a-zA-Z_0-9]*)*\\s*$"),

    OPEN_BRACE("^\\s*(?<token>[(]?)\\s*$"),
    CLOSE_BRACE("^\\s*(?<token>[)]?)\\s*$"),

    COMMA("^\\s*(?<token>[,]?)\\s*$");

    // TODO TEXT type, to use as function arguments

    private Pattern pattern;

    private String extractExpression = "${token}"; // default value - named group 'token'

    StandardTokenTypes(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean matches(String sample) {
        return pattern.matcher(sample).matches();
    }

    /**
     * Returns token without unnecessary spaces
     * */
    @Override
    public String extract(String sample) {
        Matcher matcher = pattern.matcher(sample);
        if (matcher.matches()) {
            return matcher.replaceAll(extractExpression);
        } else {
            throw new IllegalArgumentException("Unsupported token string: " + sample);
        }
    }
}
