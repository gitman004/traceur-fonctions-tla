package tlaprojet;

import java.util.List;

/**
 * Analyseur syntaxique récursif descendant.
 *
 * Grammaire (de la priorité la plus faible à la plus forte) :
 *
 *   expr    := term   (('+' | '-') term)*
 *   term    := unary  (('*' | '/') unary)*
 *   unary   := ('-' | '+') unary | power
 *   power   := primary ('^' unary)?          -- associatif à droite : 2^3^2 = 2^(3^2)
 *   primary := NUMBER | 'x' | CONSTANT
 *            | FUNCTION '(' expr ')'
 *            | '(' expr ')'
 *
 * Conséquences voulues : -x^2 = -(x^2), 2^-1 = 0.5, 2^3^2 = 512.
 */
class Parser {
    private final List<Token> tokens;
    private final int inputLength;
    private int pos;

    Parser(List<Token> tokens, int inputLength) {
        this.tokens = tokens;
        this.inputLength = inputLength;
        this.pos = 0;
    }

    /** Analyse l'expression complète ; tout lexème restant est une erreur. */
    ASTNode parse() {
        if (tokens.isEmpty()) throw new SyntaxError("Expression vide", 0);
        ASTNode node = parseExpr();
        if (pos < tokens.size()) {
            Token t = tokens.get(pos);
            String msg = t.type == Token.Type.RPAREN ? "Parenthèse fermante en trop" : "Symbole inattendu « " + t.value + " »";
            throw new SyntaxError(msg, t.position);
        }
        return node;
    }

    private ASTNode parseExpr() {
        ASTNode node = parseTerm();
        while (peekOperator("+") || peekOperator("-")) {
            String op = tokens.get(pos++).value;
            node = ASTNode.binary(op, node, parseTerm());
        }
        return node;
    }

    private ASTNode parseTerm() {
        ASTNode node = parseUnary();
        while (peekOperator("*") || peekOperator("/")) {
            String op = tokens.get(pos++).value;
            node = ASTNode.binary(op, node, parseUnary());
        }
        return node;
    }

    private ASTNode parseUnary() {
        if (peekOperator("-")) {
            pos++;
            return ASTNode.negate(parseUnary());
        }
        if (peekOperator("+")) {
            pos++;
            return parseUnary();
        }
        return parsePower();
    }

    private ASTNode parsePower() {
        ASTNode base = parsePrimary();
        if (peekOperator("^")) {
            pos++;
            // Récursion à droite : l'exposant peut lui-même contenir un ^ (associativité à droite)
            return ASTNode.binary("^", base, parseUnary());
        }
        return base;
    }

    private ASTNode parsePrimary() {
        if (pos >= tokens.size()) throw new SyntaxError("Expression incomplète", inputLength);
        Token t = tokens.get(pos++);
        switch (t.type) {
            case NUMBER:
                return ASTNode.number(t.value);
            case VARIABLE:
                return ASTNode.variable();
            case CONSTANT:
                return ASTNode.constant(t.value);
            case FUNCTION: {
                if (pos >= tokens.size() || tokens.get(pos).type != Token.Type.LPAREN) {
                    throw new SyntaxError("« " + t.value + " » doit être suivi d'une parenthèse", t.position);
                }
                pos++;
                ASTNode arg = parseExpr();
                expectClosing(t.position);
                return ASTNode.function(t.value, arg);
            }
            case LPAREN: {
                ASTNode inner = parseExpr();
                expectClosing(t.position);
                return inner;
            }
            default:
                throw new SyntaxError("Symbole inattendu « " + t.value + " »", t.position);
        }
    }

    private void expectClosing(int openedAt) {
        if (pos >= tokens.size() || tokens.get(pos).type != Token.Type.RPAREN) {
            throw new SyntaxError("Parenthèse ouverte non fermée", openedAt);
        }
        pos++;
    }

    private boolean peekOperator(String op) {
        return pos < tokens.size() && tokens.get(pos).is(Token.Type.OPERATOR, op);
    }

    /** Raccourci : lexer + parser. */
    static ASTNode parse(String expression) {
        return new Parser(new Lexer(expression).tokenize(), expression.length()).parse();
    }
}
