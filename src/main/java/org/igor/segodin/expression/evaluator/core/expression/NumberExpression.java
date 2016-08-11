package org.igor.segodin.expression.evaluator.core.expression;

import java.util.Map;

/**
 * @author igor
 */
public class NumberExpression extends AbstractExpression {

    private Number value;

    public NumberExpression(Number value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Map<String, Object> context) throws EvaluationException {
        return value;
    }
}
