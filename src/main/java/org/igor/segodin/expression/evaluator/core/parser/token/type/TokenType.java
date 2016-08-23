package org.igor.segodin.expression.evaluator.core.parser.token.type;

/**
 * Interface for text token, which is part of grammar combination
 *
 * @author igor
 */
public interface TokenType {

    /**
     * Name of a token type, should be unique
     * */
    String name();

    /**
     * Check if given text sample matches this type
     * */
    boolean matches(String sample);

    /**
     * Extracts token from given sample, with all redundant characters removed (like spaces, or quotes if it is type for text variable)
     * */
    String extract(String sample);
}
