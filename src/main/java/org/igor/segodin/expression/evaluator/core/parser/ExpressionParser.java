package org.igor.segodin.expression.evaluator.core.parser;

import org.igor.segodin.expression.evaluator.core.expression.Expression;

/**
 * @author igor
 */
public interface ExpressionParser {

    Expression parse(String expression) throws ParseException;
}
