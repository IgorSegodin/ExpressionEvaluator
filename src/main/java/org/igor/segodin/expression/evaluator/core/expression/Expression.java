package org.igor.segodin.expression.evaluator.core.expression;

import java.util.Map;

/**
 * @author igor
 */
public interface Expression {

    Object evaluate() throws EvaluationException;

    Object evaluate(Map<String, Object> context) throws EvaluationException;
}
