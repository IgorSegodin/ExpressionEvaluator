package org.igor.segodin.expression.evaluator.core.expression;

import java.util.Map;

/**
 * @author igor
 */
public class ArithmeticExpression extends AbstractExpression {

    private Expression first;
    private Expression second;
    private String operator;

    public ArithmeticExpression(Expression first, Expression second, String operator) {
        this.first = first;
        this.second = second;
        this.operator = operator;
    }

    @Override
    public Object evaluate(Map<String, Object> context) throws EvaluationException {
        Number firstNumber = (Number) first.evaluate(context);
        Number secondNumber = (Number) second.evaluate(context);

        switch (operator) {
            case "+": return firstNumber.doubleValue() + secondNumber.doubleValue();
            case "-": return firstNumber.doubleValue() - secondNumber.doubleValue();
            case "*": return firstNumber.doubleValue() * secondNumber.doubleValue();
            case "/": return firstNumber.doubleValue() / secondNumber.doubleValue();
            case "^": return Math.pow(firstNumber.doubleValue(), secondNumber.doubleValue());
            default: throw new EvaluationException("Unsupported operator: " + operator);
        }
    }
}
