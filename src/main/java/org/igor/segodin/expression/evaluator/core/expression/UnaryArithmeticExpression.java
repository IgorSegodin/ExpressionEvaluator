package org.igor.segodin.expression.evaluator.core.expression;

import java.util.Map;

/**
 * @author igor
 */
public class UnaryArithmeticExpression extends AbstractExpression {

    private Expression value;
    private String operator;

    public UnaryArithmeticExpression(Expression value, String operator) {
        this.value = value;
        this.operator = operator;
    }

    @Override
    public Object evaluate(Map<String, Object> context) throws EvaluationException {
        Number valueNumber = (Number) value.evaluate(context);
        switch (operator) {
            case "+": return valueNumber.doubleValue();
            case "-": return valueNumber.doubleValue() * (-1);
            default: throw new EvaluationException("Unsupported operator: " + operator);
        }
    }
}
