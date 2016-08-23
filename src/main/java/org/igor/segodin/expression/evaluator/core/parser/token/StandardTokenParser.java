package org.igor.segodin.expression.evaluator.core.parser.token;

import org.igor.segodin.expression.evaluator.core.parser.token.type.StandardTokenTypes;
import org.igor.segodin.expression.evaluator.core.parser.token.type.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Splits input string for separate tokens, removes unnecessary spaces.
 *
 * @author igor
 */
public class StandardTokenParser {

    private List<TokenType> tokenTypes = Arrays.asList(StandardTokenTypes.values());

    public List<Token> parse(String expressionString) throws TokenException {
        char[] chars = expressionString.toCharArray();
        if (chars.length > 0) {

            List<Token> tokens = new ArrayList<>();

            StringBuilder currentSample = new StringBuilder();

            for (int i = 0; i < chars.length;) {

                currentSample.append(chars[i]);

                for (int j = i + 1; j <= chars.length; j++) {
                    Character nextChar = null;
                    if (j < chars.length) {
                        nextChar = chars[j];
                    }

                    List<TokenType> currentMatchingTokenTypes = new ArrayList<>(tokenTypes.size());

                    String sample = currentSample.toString();

                    for (TokenType t : tokenTypes) {
                        if (t.matches(sample)) {
                            currentMatchingTokenTypes.add(t);
                        }
                    }

                    if (currentMatchingTokenTypes.isEmpty()) {
                        throw new TokenException("Unknown token", i, sample);
                    }

                    Token token = null;

                    if (nextChar == null) {
                        if (currentMatchingTokenTypes.size() > 1) {
                            throw new TokenException("Ambiguous token, has more than one matching type at the end of a string", i, sample);
                        } else {
                            TokenType type = currentMatchingTokenTypes.get(0);
                            token = new Token(type, type.extract(sample), i);
                        }
                    } else {
                        List<TokenType> currentBreakTokenTypes = new ArrayList<>(currentMatchingTokenTypes.size());

                        String fullSample = sample + nextChar.toString();

                        for (TokenType t : currentMatchingTokenTypes) {
                            if (!t.matches(fullSample)) {
                                currentBreakTokenTypes.add(t);
                            }
                        }

                        if (currentMatchingTokenTypes.size() > 1
                                && currentBreakTokenTypes.size() == currentMatchingTokenTypes.size()) {

                            throw new TokenException("Ambiguous token, has more than one matching type for token end recognition", i, fullSample);

                        } else if (currentBreakTokenTypes.size() == 1) {

                            TokenType type = currentBreakTokenTypes.get(0);
                            token = new Token(type, type.extract(sample), i);

                        }
                    }

                    if (token != null) {

                        tokens.add(token);

                        currentSample = new StringBuilder();
                        i = j;
                        break;
                    }

                    currentSample.append(nextChar);
                }
            }

            return tokens;

        } else {
            return Collections.emptyList();
        }
    }

}
