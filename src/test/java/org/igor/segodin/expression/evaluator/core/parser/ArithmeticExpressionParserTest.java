package org.igor.segodin.expression.evaluator.core.parser;

import java.util.HashMap;
import java.util.Map;

import org.igor.segodin.expression.evaluator.core.expression.Expression;
import org.igor.segodin.expression.evaluator.core.util.FunctionUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author igor
 */
public class ArithmeticExpressionParserTest {

    private ExpressionParser parser = new ArithmeticExpressionParser();

    @Test
    public void operatorOrderTest() throws ParseException {
        String string = "2+ 2*2";
        Expression expression = parser.parse(string);

        Assert.assertEquals(6.0, expression.evaluate());
    }

    @Test
    public void operatorOrderTest02() throws ParseException {
        String string = "1 + 2*2 + 3";
        Expression expression = parser.parse(string);

        Assert.assertEquals(8.0, expression.evaluate());
    }

    @Test
    public void operatorBraceTest() throws ParseException {
        String string = "(2+ 2)*2";
        Expression expression = parser.parse(string);

        Assert.assertEquals(8.0, expression.evaluate());
    }

    @Test
    public void unaryOperatorTest() throws ParseException {
        String string = "-(2+ 2)*2";
        Expression expression = parser.parse(string);

        Assert.assertEquals(-8.0, expression.evaluate());
    }

    @Test
    public void unaryOperator02Test() throws ParseException {
        String string = "-(2+ 2)^2";
        Expression expression = parser.parse(string);

        Assert.assertEquals(16.0, expression.evaluate());
    }

    @Test
    public void unaryOperator03Test() throws ParseException {
        String string = "- 2 ^ 2";
        Expression expression = parser.parse(string);

        Assert.assertEquals(4.0, expression.evaluate());
    }

    @Test
    public void braceTest() throws ParseException {
        String string = "(3*3)*(2+2)";
        Expression expression = parser.parse(string);

        Assert.assertEquals(36.0, expression.evaluate());
    }

    @Test
    public void contextTest() throws ParseException {
        String string = "-x*(2+2)";
        Expression expression = parser.parse(string);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("x", 3);

        Assert.assertEquals(-12.0, expression.evaluate(ctx));
    }

    @Test
    public void context02Test() throws ParseException {
        String string = "-x*(2+2) * (-1)";;
        Expression expression = parser.parse(string);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("x", 3);

        Assert.assertEquals(12.0, expression.evaluate(ctx));
    }

    @Test
    public void context03Test() throws ParseException {
        String string = "-x*(2+2) * (-1) + (-10)";
        Expression expression = parser.parse(string);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("x", 3);

        Assert.assertEquals(2.0, expression.evaluate(ctx));
    }

    @Test
    public void nestedBraceTest() throws ParseException {
        String string = "2 + (-3) * (10 - 4 * 2) + (2 * ((5-(6/2)) + 1)) + 1.3";
        Expression expression = parser.parse(string);

        Assert.assertEquals(3.3, expression.evaluate());
    }

    @Test
    public void functionTest01() throws ParseException, NoSuchMethodException {
        String string = "singleArg(2+2) * twoArgs(1 + 1, (3*2 +1))";
        Expression expression = parser.parse(string);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("singleArg", FunctionUtil.getFunctionFromClass(this.getClass(), "singleArg"));
        ctx.put("twoArgs", FunctionUtil.getFunctionFromClass(this.getClass(), "twoArgs"));

        Assert.assertEquals(72.0, expression.evaluate(ctx));

    }

    @Test
    public void functionTest02() throws ParseException, NoSuchMethodException {
        String string = "twoArgs(1 + 1, (3*2 +1))";
        Expression expression = parser.parse(string);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("twoArgs", FunctionUtil.getFunctionFromClass(this.getClass(), "twoArgs"));

        Assert.assertEquals(9.0, expression.evaluate(ctx));

    }

    public static Double singleArg(Double a) {
        return a * 2;
    }

    public static Double twoArgs(Double a, Double b) {
        return a + b;
    }

}
