package org.igor.segodin.expression.evaluator.core.expression;

import org.igor.segodin.expression.evaluator.core.expression.function.ContextFunction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author igor
 */
public class FunctionExpression extends ContextVariableExpression {

    private List<Expression> args;

    public FunctionExpression(String variable, List<Expression> args) {
        super(variable);
        this.args = args;
    }

    @Override
    public Object evaluate(Map<String, Object> context) throws EvaluationException {
        Object functionSource = super.evaluate(context);
        // TODO nested function name
        if (ContextFunction.class.isAssignableFrom(functionSource.getClass())) {
            ContextFunction function = (ContextFunction) super.evaluate(context);
            try {
                return function.invoke(args.stream()
                        .map(arg -> arg.evaluate(context))
                        .collect(Collectors.toList())
                        .toArray());
            } catch (Exception e) {
                throw new EvaluationException("Function invocation exception " + variable + ": " + e.getMessage(), e);
            }
        } else {
            throw new EvaluationException("Context function " + variable + " should be object of type " + ContextFunction.class.getName());
        }
    }
}
