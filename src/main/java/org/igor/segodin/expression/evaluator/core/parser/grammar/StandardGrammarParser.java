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
 * @deprecated use {@link ArithmeticGrammarParser} instead
 * @author igor
 */
public class StandardGrammarParser {

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
            PLUS_OPERATOR,
            MINUS_OPERATOR,
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
        for (int i = startIdx; i < endIdx; i++) {
            Token token = tokens.get(i);

            if (isType(NUMBER, token)) {

                Token next = getToken(i + 1, tokens, startIdx, endIdx);

                if (next == null) {
                    return new NumberExpression(Double.parseDouble(token.getSample()));
                } else if (ARITHMETIC_OPERATORS.contains(next.getType().name())) {
                    continue;
                } else {
                    throw new GrammarException("Unexpected token", next);
                }
            }
            else if (isType(VARIABLE, token)) {

                Token next = getToken(i + 1, tokens, startIdx, endIdx);

                if (next == null) {
                    return new ContextVariableExpression(token.getSample());
                } else if (ARITHMETIC_OPERATORS.contains(next.getType().name())) {
                    continue;
                } else if (isType(OPEN_BRACE, next)) {

                    int idx = i;

                    return parseOpenBrace(next, i + 1,
                            closeBraceIndex -> new FunctionExpression(token.getSample(), findFunctionArguments(tokens, idx + 2, closeBraceIndex)),
                            tokens, i + 2, endIdx);
                } else {
                    throw new GrammarException("Unexpected token", next);
                }
            }
            else if (isType(OPEN_BRACE, token)) {

                int braceIdx = i;
                return parseOpenBrace(token, i,
                        (closeBraceIndex) -> parseInternal(tokens, braceIdx + 1, closeBraceIndex),
                        tokens, startIdx, endIdx);
            }
            else if (isType(PLUS_OPERATOR, token)
                    || isType(MINUS_OPERATOR, token)) {

                Token firstOperand = getToken(i - 1, tokens, startIdx, endIdx);
                Token secondOperand = getToken(i + 1, tokens, startIdx, endIdx);
                if (secondOperand != null) {
                    validateOperand(secondOperand);
                    if (firstOperand != null) {
                        validateOperand(firstOperand);
                        return new ArithmeticExpression(
                                getExpressionFromToken(firstOperand),
                                parseInternal(tokens, i + 1, endIdx),
                                token.getSample()
                        );
                    } else {
                        if (isType(OPEN_BRACE, secondOperand)) {
                            int braceIdx = i + 1;
                            return parseOpenBrace(secondOperand, braceIdx,
                                    (closeBraceIndex) -> {
                                        Expression innerExpression = parseInternal(tokens, braceIdx + 1, closeBraceIndex);
                                        return new UnaryArithmeticExpression(innerExpression, token.getSample());
                                    },
                                    tokens, startIdx, endIdx);
                        } else {
                            Token nextToken = getToken(i + 2, tokens, startIdx, endIdx);
                            if (nextToken == null) {
                                return new UnaryArithmeticExpression(getExpressionFromToken(secondOperand), token.getSample());
                            } else if (ARITHMETIC_OPERATORS.contains(nextToken.getType().name())) {
                                Expression unaryExpression = new UnaryArithmeticExpression(getExpressionFromToken(secondOperand), token.getSample());
                                return parseNextArithmeticOperator(nextToken, i + 2, unaryExpression, tokens, i + 2, endIdx);
                            } else {
                                throw new GrammarException("Unexpected token", nextToken);
                            }
                        }
                    }
                } else {
                    throw new GrammarException("Second operand is missing", token);
                }
            }
            else if (isType(MULTIPLY_OPERATOR, token)
                    || isType(POW_OPERATOR, token)
                    || isType(DIVIDE_OPERATOR, token)) {

                Token firstOperand = getToken(i - 1, tokens, startIdx, endIdx);
                if (firstOperand != null) {
                    validateOperand(firstOperand);
                    Expression firstOperandExpression = getExpressionFromToken(firstOperand);
                    return parseHighPriorityArithmeticOperator(token, i, firstOperandExpression, tokens, startIdx, endIdx);
                } else {
                    throw new GrammarException("Unary operator is not supported", token);
                }
            }
            else if (isType(CLOSE_BRACE, token)) {
                throw new GrammarException("Unexpected close brace", token);
            }
            else {
                throw new IllegalStateException("Unknown token for grammar parser: " + token.getSample());
            }

        }
        throw new IllegalStateException("Unexpected behaviour, this exception should not occur, something went wrong!");
    }

    protected Expression parseOpenBrace(Token brace, int braceIdx, OpenBraceParser openBraceParser, List<Token> tokens, int startIdx, int endIdx) throws GrammarException {
        int closeBraceIndex = findCloseBraceIndex(tokens, braceIdx + 1, endIdx);
        if (closeBraceIndex < 0) {
            throw new GrammarException("No close brace", brace);
        }
        Expression insideBraceExpression = openBraceParser.parse(closeBraceIndex);
        Token nextToken = getToken(closeBraceIndex + 1, tokens, startIdx, endIdx);
        if (nextToken == null) {
            return insideBraceExpression;
        } else if (ARITHMETIC_OPERATORS.contains(nextToken.getType().name())) {
            return parseNextArithmeticOperator(nextToken, closeBraceIndex + 1, insideBraceExpression, tokens, closeBraceIndex + 1, endIdx);
        } else {
            throw new GrammarException("Unexpected token", nextToken);
        }
    }

    protected interface OpenBraceParser {
        Expression parse(int closeBraceIndex);
    }

    protected Expression parseNextArithmeticOperator(Token operator, int operatorIdx, Expression firstOperand, List<Token> tokens, int startIdx, int endIdx) throws GrammarException {
        if (operatorIdx + 1 >= endIdx) {
            throw new GrammarException("Unexpected end of expression", operator);
        }
        if (HIGH_PRIORITY_OPERATOR.contains(operator.getType().name())) {
            return parseHighPriorityArithmeticOperator(operator, operatorIdx, firstOperand, tokens, operatorIdx, endIdx);
        } else {
            return new ArithmeticExpression(
                    firstOperand,
                    parseInternal(tokens, operatorIdx + 1, endIdx),
                    operator.getSample()
            );
        }
    }

    // TODO make parsing operator - centric, because now a lot of logic is being duplicated
    protected Expression parseHighPriorityArithmeticOperator(Token operator, int operatorIdx, Expression firstOperand, List<Token> tokens, int startIdx, int endIdx) throws GrammarException {
        Token secondOperand = getToken(operatorIdx + 1, tokens, startIdx, endIdx);
        if (secondOperand != null) {
            validateOperand(secondOperand);

            Expression secondOperandExpression;
            int nextStartIdx = operatorIdx + 2;

            if (isType(OPEN_BRACE, secondOperand)) {
                int closeBraceIndex = findCloseBraceIndex(tokens, operatorIdx + 2, endIdx);
                if (closeBraceIndex < 0) {
                    throw new GrammarException("No close brace", secondOperand);
                }
                secondOperandExpression = parseInternal(tokens, operatorIdx + 2, closeBraceIndex);
                nextStartIdx = closeBraceIndex + 1;
            } else if (isType(VARIABLE, secondOperand)) {
                Token nextAfterVariable = getToken(operatorIdx + 2, tokens, startIdx, endIdx);
                if (nextAfterVariable != null
                        && isType(OPEN_BRACE, nextAfterVariable)) {

                    int closeBraceIndex = findCloseBraceIndex(tokens, operatorIdx + 3, endIdx);
                    if (closeBraceIndex < 0) {
                        throw new GrammarException("No close brace", secondOperand);
                    }
                    secondOperandExpression = new FunctionExpression(secondOperand.getSample(), findFunctionArguments(tokens, operatorIdx + 3, closeBraceIndex));
                    nextStartIdx = closeBraceIndex + 1;

                } else {
                    secondOperandExpression = getExpressionFromToken(secondOperand);
                }
            } else {
                secondOperandExpression = getExpressionFromToken(secondOperand);
            }

            Expression expression = new ArithmeticExpression(
                    firstOperand,
                    secondOperandExpression,
                    operator.getSample()
            );

            Token nextToken = getToken(nextStartIdx, tokens, nextStartIdx, endIdx);

            if (nextToken == null) {
                return expression;
            } else if (ARITHMETIC_OPERATORS.contains(nextToken.getType().name())) {
                return parseNextArithmeticOperator(nextToken, nextStartIdx, expression, tokens, nextStartIdx, endIdx);
            } else {
                throw new GrammarException("Unexpected token", nextToken);
            }

        } else {
            throw new GrammarException("Second operand is missing", operator);
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

    protected Expression getExpressionFromToken(Token token) {
        if (isType(NUMBER, token)) {
            return new NumberExpression(Double.parseDouble(token.getSample()));
        }
        else if (isType(VARIABLE, token)) {
            return new ContextVariableExpression(token.getSample());
        }
        else {
            throw new IllegalStateException("Unsupported type: " + token.getType());
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

    protected List<Expression> findFunctionArguments(List<Token> tokens, int startIdx, int endIdx) {
        List<Expression> args = new ArrayList<>();

        if (endIdx - startIdx > 1) {
            // function has arguments

            int openedBraces = 0;
            List<Integer> commaIndexes = new ArrayList<>(5); // think that usually there will be no more than 5 args

            for (int i = startIdx; i < endIdx; i++) {
                Token token = tokens.get(i);
                if (isType(OPEN_BRACE, token)) {
                    openedBraces++;
                } else if (isType(CLOSE_BRACE, token)) {
                    openedBraces--;
                } else if (isType(COMMA, token)) {
                    if (openedBraces == 0) {

                        commaIndexes.add(i);

                        Token nextToken = getToken(i + 1, tokens, startIdx, endIdx);
                        if (nextToken != null) {
                            validateOperand(nextToken);
                        } else {
                            throw new GrammarException("Missing expression after comma", token);
                        }
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
