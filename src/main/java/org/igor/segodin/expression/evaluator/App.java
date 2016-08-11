package org.igor.segodin.expression.evaluator;

import org.igor.segodin.expression.evaluator.core.expression.Expression;
import org.igor.segodin.expression.evaluator.core.parser.ArithmeticExpressionParser;
import org.igor.segodin.expression.evaluator.core.parser.ExpressionParser;
import org.igor.segodin.expression.evaluator.core.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by igor on 8/7/16.
 */
public class App {

    public static void main(String[] args) throws ParseException {

//        String string = "x+(2+2)*2 + 3^2";
//        String string = "2+2*2";
        String string = "-2+3*2";
//        String string = "(2+2)*2 + 2";
//        String string = "-(2+1)*3 + 2 + (-1)";

        ExpressionParser parser = new ArithmeticExpressionParser();

        Expression expression = parser.parse(string);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("x", 10);

        Object result = expression.evaluate(ctx);

        System.out.println(result);
    }
}
