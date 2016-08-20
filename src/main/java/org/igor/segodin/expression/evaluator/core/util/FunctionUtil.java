package org.igor.segodin.expression.evaluator.core.util;

import org.igor.segodin.expression.evaluator.core.expression.function.ContextFunction;
import org.igor.segodin.expression.evaluator.core.expression.function.ReflectionContextFunction;

import java.lang.reflect.Method;

/**
 * Helper for context functions
 *
 * @author igor
 */
public class FunctionUtil {

    public static ContextFunction getFunctionFromClass(Class source, String methodName) throws NoSuchMethodException {
        return getFunctionFromClass(source, null, methodName);
    }

    public static ContextFunction getFunctionFromClass(Object state, String methodName) throws NoSuchMethodException {
        return getFunctionFromClass(state.getClass(), state, methodName);
    }

    public static ContextFunction getFunctionFromClass(Class sourceClass, Object state, String methodName) throws NoSuchMethodException {
        for (Method m : sourceClass.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                return new ReflectionContextFunction(m, state);
            }
        }
        throw new NoSuchMethodException("Can't find method '" + methodName + "' on type '" + sourceClass.getName() + "'");
    }
}
