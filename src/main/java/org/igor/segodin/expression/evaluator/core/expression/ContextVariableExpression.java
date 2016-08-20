package org.igor.segodin.expression.evaluator.core.expression;

import java.util.Map;

/**
 * @author igor
 */
public class ContextVariableExpression extends AbstractExpression {

    protected String variable;

    public ContextVariableExpression(String variable) {
        this.variable = variable;
    }

    @Override
    public Object evaluate(Map<String, Object> context) throws EvaluationException {
        if (context == null || !context.containsKey(variable)) {
            throw new EvaluationException("Can't resolve context variable: " + variable);
        }
        // TODO nested property
        return context.get(variable);
    }
}
