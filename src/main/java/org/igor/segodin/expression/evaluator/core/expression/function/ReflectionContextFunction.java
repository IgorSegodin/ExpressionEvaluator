package org.igor.segodin.expression.evaluator.core.expression.function;

import java.lang.reflect.Method;

/**
 * @author igor
 */
public class ReflectionContextFunction implements ContextFunction {

    protected Method method;
    protected Object state;

    public ReflectionContextFunction(Method method, Object state) {
        this.method = method;
        this.state = state;
    }

    @Override
    public Object invoke(Object... args) throws Exception {
        return method.invoke(state, args);
    }
}
