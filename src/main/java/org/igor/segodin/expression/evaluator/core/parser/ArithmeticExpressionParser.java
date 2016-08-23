package org.igor.segodin.expression.evaluator.core.parser;

import org.igor.segodin.expression.evaluator.core.expression.Expression;
import org.igor.segodin.expression.evaluator.core.parser.grammar.ArithmeticGrammarParser;
import org.igor.segodin.expression.evaluator.core.parser.grammar.GrammarException;
import org.igor.segodin.expression.evaluator.core.parser.token.StandardTokenParser;
import org.igor.segodin.expression.evaluator.core.parser.token.Token;
import org.igor.segodin.expression.evaluator.core.parser.token.TokenException;

/**
 * @author igor
 */
public class ArithmeticExpressionParser implements ExpressionParser {

    protected StandardTokenParser tokenParser = new StandardTokenParser();

    protected ArithmeticGrammarParser grammarParser = new ArithmeticGrammarParser();

    public Expression parse(String expression) throws ParseException {
        try {
            return grammarParser.parse(tokenParser.parse(expression));
        } catch (GrammarException e) {
            Token token = e.getToken();
            throw new ParseException("Can't parse expression '" + expression + "' grammar parse error: " + e.getMessage() + " at index " + token.getStartIndex() + " with token '" + token.getSample() + "'", e);
        } catch (TokenException e) {
            throw new ParseException("Can't parse expression '" + expression + "' token parse error: " + e.getMessage() + " at index " + e.getStartIndex() + " with token '" + e.getSample() + "'", e);
        } catch (Exception e) {
            throw new ParseException("Can't parse expression due to unknown exception", e);
        }
    }
}
