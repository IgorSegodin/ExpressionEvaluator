package org.igor.segodin.expression.evaluator.core.parser.grammar;

import org.igor.segodin.expression.evaluator.core.expression.ArithmeticExpression;
import org.igor.segodin.expression.evaluator.core.expression.ContextVariableExpression;
import org.igor.segodin.expression.evaluator.core.expression.Expression;
import org.igor.segodin.expression.evaluator.core.expression.NumberExpression;
import org.igor.segodin.expression.evaluator.core.expression.UnaryArithmeticExpression;
import org.igor.segodin.expression.evaluator.core.parser.token.Token;
import org.igor.segodin.expression.evaluator.core.parser.token.TokenType;

import java.util.Arrays;
import java.util.List;

/**
 * @author igor
 */
public class GrammarParser {

    protected static final List<TokenType> ARITHMETIC_OPERATORS = Arrays.asList(
            TokenType.PLUS_OPERATOR,
            TokenType.MINUS_OPERATOR,
            TokenType.MULTIPLY_OPERATOR,
            TokenType.DIVIDE_OPERATOR,
            TokenType.POW_OPERATOR
    );

    protected static final List<TokenType> HIGH_PRIORITY_OPERATOR = Arrays.asList(
            TokenType.MULTIPLY_OPERATOR,
            TokenType.DIVIDE_OPERATOR,
            TokenType.POW_OPERATOR
    );


    public Expression parse(List<Token> tokens) throws GrammarException {
        return parseInternal(tokens, 0, tokens.size());
    }

    protected Expression parseInternal(List<Token> tokens, int startIdx, int endIdx) throws GrammarException {
        for (int i = startIdx; i < endIdx; i++) {
            Token token = tokens.get(i);

            switch (token.getType()) {

                case NUMBER: {
                    Token next = getToken(i + 1, tokens, startIdx, endIdx);

                    if (next == null) {
                        return new NumberExpression(Double.parseDouble(token.getSample()));
                    } else if (ARITHMETIC_OPERATORS.contains(next.getType())) {
                        continue;
                    } else {
                        throw new GrammarException("Unexpected token", next);
                    }
                }

                case VARIABLE: {
                    Token next = getToken(i + 1, tokens, startIdx, endIdx);

                    if (next == null) {
                        return new ContextVariableExpression(token.getSample());
                    } else if (ARITHMETIC_OPERATORS.contains(next.getType())) {
                        continue;
                    } else if (TokenType.OPEN_BRACE.equals(next.getType())) {
                        // TODO
                        throw new RuntimeException("Unimplemented function expression");

                    } else {
                        throw new GrammarException("Unexpected token", next);
                    }
                }

                case OPEN_BRACE: {
                    int braceIdx = i;
                    return parseOpenBrace(token, i,
                            (closeBraceIndex) -> parseInternal(tokens, braceIdx + 1, closeBraceIndex),
                            tokens, startIdx, endIdx);
                }

                case PLUS_OPERATOR:
                case MINUS_OPERATOR: {
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
                            if (TokenType.OPEN_BRACE.equals(secondOperand.getType())) {
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
                                } else if (ARITHMETIC_OPERATORS.contains(nextToken.getType())) {
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

                case MULTIPLY_OPERATOR:
                case POW_OPERATOR:
                case DIVIDE_OPERATOR: {
                    Token firstOperand = getToken(i - 1, tokens, startIdx, endIdx);
                    if (firstOperand != null) {
                        validateOperand(firstOperand);
                        Expression firstOperandExpression = getExpressionFromToken(firstOperand);
                        return parseHighPriorityArithmeticOperator(token, i, firstOperandExpression, tokens, startIdx, endIdx);
                    } else {
                        throw new GrammarException("Unary operator is not supported", token);
                    }
                }

                case CLOSE_BRACE: throw new GrammarException("Unexpected close brace", token);

                default: throw new IllegalStateException("Unknown token for grammar parser: " + token.getSample());
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
        } else if (ARITHMETIC_OPERATORS.contains(nextToken.getType())) {
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
        if (HIGH_PRIORITY_OPERATOR.contains(operator.getType())) {
            return parseHighPriorityArithmeticOperator(operator, operatorIdx, firstOperand, tokens, operatorIdx, endIdx);
        } else {
            return new ArithmeticExpression(
                    firstOperand,
                    parseInternal(tokens, operatorIdx + 1, endIdx),
                    operator.getSample()
            );
        }
    }

    protected Expression parseHighPriorityArithmeticOperator(Token operator, int operatorIdx, Expression firstOperand, List<Token> tokens, int startIdx, int endIdx) throws GrammarException {
        Token secondOperand = getToken(operatorIdx + 1, tokens, startIdx, endIdx);
        if (secondOperand != null) {
            validateOperand(secondOperand);

            Expression secondOperandExpression;
            int nextStartIdx = operatorIdx + 2;

            if (TokenType.OPEN_BRACE.equals(secondOperand.getType())) {
                int closeBraceIndex = findCloseBraceIndex(tokens, operatorIdx + 2, endIdx);
                if (closeBraceIndex < 0) {
                    throw new GrammarException("No close brace", secondOperand);
                }
                secondOperandExpression = parseInternal(tokens, operatorIdx + 2, closeBraceIndex);
                nextStartIdx = closeBraceIndex + 1;
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
            } else if (ARITHMETIC_OPERATORS.contains(nextToken.getType())) {
                return parseNextArithmeticOperator(nextToken, nextStartIdx, expression, tokens, nextStartIdx, endIdx);
            } else {
                throw new GrammarException("Unexpected token", nextToken);
            }

        } else {
            throw new GrammarException("Second operand is missing", operator);
        }
    }



    protected Token getToken(int position, List<Token> tokens, int startIdx, int endIdx) {
        return position > -1
                && position >= startIdx
                && position < tokens.size()
                && position < endIdx ? tokens.get(position) : null;
    }

    protected void validateOperand(Token token) throws GrammarException {
        if (ARITHMETIC_OPERATORS.contains(token.getType())) {
            throw new GrammarException("Unexpected operand", token);
        }
    }

    protected Expression getExpressionFromToken(Token token) {
        switch (token.getType()) {
            case NUMBER: return new NumberExpression(Double.parseDouble(token.getSample()));
            case VARIABLE: return new ContextVariableExpression(token.getSample());
            default: throw new IllegalStateException("Unsupported type: " + token.getType());
        }
    }

    protected int findCloseBraceIndex(List<Token> tokens, int startIdx, int endIdx) {
        int openedBraces = 1;

        for (int i = startIdx; i < endIdx; i++) {
            Token token = tokens.get(i);
            if (TokenType.OPEN_BRACE.equals(token.getType())) {
                openedBraces++;
            } else if (TokenType.CLOSE_BRACE.equals(token.getType())) {
                openedBraces--;
            }
            if (openedBraces == 0) {
                return i;
            }
        }
        return -1;
    }

}
