package org.igor.segodin.expression.evaluator.core.parser.grammar;

import org.igor.segodin.expression.evaluator.core.expression.ArithmeticExpression;
import org.igor.segodin.expression.evaluator.core.expression.ContextVariableExpression;
import org.igor.segodin.expression.evaluator.core.expression.Expression;
import org.igor.segodin.expression.evaluator.core.expression.FunctionExpression;
import org.igor.segodin.expression.evaluator.core.expression.NumberExpression;
import org.igor.segodin.expression.evaluator.core.expression.UnaryArithmeticExpression;
import org.igor.segodin.expression.evaluator.core.parser.token.Token;
import org.igor.segodin.expression.evaluator.core.parser.token.type.StandardTokenTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author igor
 */
public class ArithmeticGrammarParser {

    protected final static String NUMBER = StandardTokenTypes.NUMBER.name();
    protected final static String PLUS_OPERATOR = StandardTokenTypes.PLUS_OPERATOR.name();
    protected final static String MINUS_OPERATOR = StandardTokenTypes.MINUS_OPERATOR.name();
    protected final static String MULTIPLY_OPERATOR = StandardTokenTypes.MULTIPLY_OPERATOR.name();
    protected final static String DIVIDE_OPERATOR = StandardTokenTypes.DIVIDE_OPERATOR.name();
    protected final static String POW_OPERATOR = StandardTokenTypes.POW_OPERATOR.name();
    protected final static String VARIABLE = StandardTokenTypes.VARIABLE.name();
    protected final static String OPEN_BRACE = StandardTokenTypes.OPEN_BRACE.name();
    protected final static String CLOSE_BRACE = StandardTokenTypes.CLOSE_BRACE.name();
    protected final static String COMMA = StandardTokenTypes.COMMA.name();

    protected static final List<String> VALID_OPERAND_TYPES = Arrays.asList(
            NUMBER,
            VARIABLE,
            OPEN_BRACE
    );

    protected static final List<String> ARITHMETIC_OPERATORS = Arrays.asList(
            PLUS_OPERATOR,
            MINUS_OPERATOR,
            MULTIPLY_OPERATOR,
            DIVIDE_OPERATOR,
            POW_OPERATOR
    );

    protected static final List<String> HIGH_PRIORITY_OPERATOR = Arrays.asList(
            MULTIPLY_OPERATOR,
            DIVIDE_OPERATOR,
            POW_OPERATOR
    );


    public Expression parse(List<Token> tokens) throws GrammarException {
        return parseInternal(tokens, 0, tokens.size());
    }

    protected Expression parseInternal(List<Token> tokens, int startIdx, int endIdx) throws GrammarException {
        if (tokens.isEmpty()) {
            return null;
        }
        if (endIdx - startIdx < 1) {
            throw new RuntimeException("Empty parse region, number of tokens are less than 1");
        }

        int operatorIdx = findNextTopOperatorIndex(tokens, startIdx, endIdx);
        Token operator = getToken(operatorIdx, tokens, startIdx, endIdx);
        if (operator != null) {

            return parseNextOperator(parseOperand(tokens, startIdx, operatorIdx), operator, operatorIdx, tokens, endIdx);

        } else {
            return parseOperand(tokens, startIdx, endIdx);
        }
    }

    protected Expression parseNextOperator(Expression firstOperand, Token operator, int operatorIdx, List<Token> tokens, int endIdx) {

        int nextContentStartIdx = operatorIdx + 1;

        int nextOperatorIdx = findNextTopOperatorIndex(tokens, nextContentStartIdx, endIdx);
        Token nextOperator = getToken(nextOperatorIdx, tokens, nextContentStartIdx, endIdx);

        Expression secondOperand;
        if (nextOperator != null) {
            secondOperand = parseOperand(tokens, nextContentStartIdx, nextOperatorIdx);
        } else {
            secondOperand = parseOperand(tokens, nextContentStartIdx, endIdx);
        }

        if (secondOperand == null) {
            throw new GrammarException("Second operand should not be empty", operator);
        }

        if (HIGH_PRIORITY_OPERATOR.contains(operator.getType().name())) {
            if (firstOperand == null) {
                throw new GrammarException("Unary operator is not supported", operator);
            }

            Expression expression = new ArithmeticExpression(firstOperand, secondOperand, operator.getSample());

            if (nextOperator != null) {
                return parseNextOperator(expression, nextOperator, nextOperatorIdx, tokens, endIdx);
            } else {
                return expression;
            }
        } else if (firstOperand == null) {
            Expression expression = new UnaryArithmeticExpression(secondOperand, operator.getSample());

            if (nextOperator != null) {
                return parseNextOperator(expression, nextOperator, nextOperatorIdx, tokens, endIdx);
            } else {
                return expression;
            }
        } else {
            if (nextOperator != null) {
                return new ArithmeticExpression(
                        firstOperand,
                        parseInternal(tokens, nextContentStartIdx, endIdx),
                        operator.getSample()
                );
            } else {
                return new ArithmeticExpression(firstOperand, secondOperand, operator.getSample());
            }
        }
    }

    protected Expression parseOperand(List<Token> tokens, int startIdx, int endIdx) {
        Token token = getToken(startIdx, tokens, startIdx, endIdx);
        if (token != null) {
            validateOperand(token);

            if (isType(NUMBER, token)) {
                if (endIdx - startIdx > 1) {
                    throw new RuntimeException("Number operand should consist of 1 token, but found " + (endIdx - startIdx) + " tokens at index " + startIdx);
                }

                return new NumberExpression(Double.parseDouble(token.getSample()));
            } else if (isType(VARIABLE, token)) {
                Token nextToken = getToken(startIdx + 1, tokens, startIdx, endIdx);

                if (nextToken != null) {
                    if (isType(OPEN_BRACE, nextToken)) {

                        int contentStartIdx = startIdx + 2;
                        int closeBraceIndex = findCloseBraceIndex(tokens, contentStartIdx, endIdx);

                        if (closeBraceIndex == -1) {
                            throw new GrammarException("Missing close brace", token);
                        }

                        if (endIdx - closeBraceIndex > 1) {
                            throw new GrammarException("Unexpected token after brace", nextToken);
                        }

                        return new FunctionExpression(token.getSample(), findFunctionArguments(tokens, contentStartIdx, closeBraceIndex));
                    } else {
                        throw new GrammarException("Variable is followed by unexpected token", nextToken);
                    }
                } else {
                    return new ContextVariableExpression(token.getSample());
                }
            } else if (isType(OPEN_BRACE, token)) {
                int contentStartIdx = startIdx + 1;
                int closeBraceIndex = findCloseBraceIndex(tokens, contentStartIdx, endIdx);

                if (closeBraceIndex == -1) {
                    throw new GrammarException("Missing close brace", token);
                }

                if (closeBraceIndex - contentStartIdx < 1) {
                    throw new GrammarException("Empty braces", token);
                }

                return parseInternal(tokens, contentStartIdx, closeBraceIndex);
            } else {
                throw new RuntimeException("Unexpected operand token " + token.getSample() + " at index " + token.getStartIndex());
            }
        } else {
            return null;
        }
    }

    protected boolean isType(String typeName, Token token) {
        return token != null && typeName.equals(token.getType().name());
    }

    protected Token getToken(int position, List<Token> tokens, int startIdx, int endIdx) {
        return position > -1
                && position >= startIdx
                && position < tokens.size()
                && position < endIdx ? tokens.get(position) : null;
    }

    protected void validateOperand(Token token) throws GrammarException {
        if (!VALID_OPERAND_TYPES.contains(token.getType().name())) {
            throw new GrammarException("Unexpected operand", token);
        }
    }

    protected int findCloseBraceIndex(List<Token> tokens, int startIdx, int endIdx) {
        int openedBraces = 1;

        for (int i = startIdx; i < endIdx; i++) {
            Token token = tokens.get(i);
            if (isType(OPEN_BRACE, token)) {
                openedBraces++;
            } else if (isType(CLOSE_BRACE, token)) {
                openedBraces--;
            }
            if (openedBraces == 0) {
                return i;
            }
        }
        return -1;
    }

    protected int findNextTopOperatorIndex(List<Token> tokens, int startIdx, int endIdx) {

        for (int i = startIdx; i < endIdx; i++) {
            Token token = tokens.get(i);
            if (isType(OPEN_BRACE, token)) {

                int closeIdx = findCloseBraceIndex(tokens, i + 1, endIdx);
                if (closeIdx > -1) {
                    i = closeIdx;
                } else {
                    throw new GrammarException("Missing close brace", token);
                }
            } else if (ARITHMETIC_OPERATORS.contains(token.getType().name())) {
                return i;
            }
        }

        return -1;
    }

    protected List<Expression> findFunctionArguments(List<Token> tokens, int startIdx, int endIdx) {
        List<Expression> args = new ArrayList<>();

        if (endIdx - startIdx > 1) {
            // function has arguments

            List<Integer> commaIndexes = new ArrayList<>(5); // think that usually there will be no more than 5 args

            for (int i = startIdx; i < endIdx; i++) {
                Token token = tokens.get(i);
                if (isType(OPEN_BRACE, token)) {

                    int closeIdx = findCloseBraceIndex(tokens, i + 1, endIdx);
                    if (closeIdx > -1) {
                        i = closeIdx;
                    } else {
                        throw new GrammarException("Missing close brace", token);
                    }
                } else if (isType(COMMA, token)) {

                    commaIndexes.add(i);

                    Token nextToken = getToken(i + 1, tokens, startIdx, endIdx);
                    if (nextToken != null) {
                        validateOperand(nextToken);
                    } else {
                        throw new GrammarException("Missing expression after comma", token);
                    }
                }
            }

            int argStartIdx = startIdx;
            Iterator<Integer> iterator = commaIndexes.iterator();

            while (true) {
                try {
                    Integer nextCommaIdx = iterator.next();

                    args.add(parseInternal(tokens, argStartIdx, nextCommaIdx));

                    argStartIdx = nextCommaIdx + 1;
                } catch (NoSuchElementException e) {
                    args.add(parseInternal(tokens, argStartIdx, endIdx));
                    break;
                }
            }
        }

        return args;
    }

}
