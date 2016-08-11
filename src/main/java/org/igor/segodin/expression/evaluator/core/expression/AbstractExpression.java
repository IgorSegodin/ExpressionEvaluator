package org.igor.segodin.expression.evaluator.core.expression;

import java.util.Map;

/**
 * @author igor
 */
public abstract class AbstractExpression implements Expression {

    @Override
    public Object evaluate() throws EvaluationException {
        return evaluate(null);
    }

    @Override
    public abstract Object evaluate(Map<String, Object> context) throws EvaluationException;
}
