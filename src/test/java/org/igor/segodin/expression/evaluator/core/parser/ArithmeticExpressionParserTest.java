package org.igor.segodin.expression.evaluator.core.parser;

import java.util.HashMap;
import java.util.Map;

import org.igor.segodin.expression.evaluator.core.expression.Expression;
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

}
