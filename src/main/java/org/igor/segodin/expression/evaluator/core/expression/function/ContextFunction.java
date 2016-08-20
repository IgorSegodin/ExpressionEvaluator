package org.igor.segodin.expression.evaluator.core.expression.function;

/**
 * @author igor
 */
public interface ContextFunction {

    Object invoke(Object... args) throws Exception;

}
