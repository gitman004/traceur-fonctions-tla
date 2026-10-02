package tlaprojet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Analyse lexicale : découpe l'expression en lexèmes.
 * Ajoute aussi les multiplications implicites : 2x, 3(x+1), (x+1)(x-1), 2sin(x).
 */
class Lexer {
    static final Set<String> FUNCTIONS = Set.of("sin", "cos", "tan", "sqrt", "exp", "ln", "abs");
    static final Set<String> CONSTANTS = Set.of("pi", "e");

    private final String input;
    private int pos;

    Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    List<Token> tokenize() {
        List<Token> raw = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            int start = pos;
            if (Character.isWhitespace(c)) {
                pos++;
            } else if (Character.isDigit(c) || c == '.') {
                raw.add(new Token(Token.Type.NUMBER, readNumber(), start));
            } else if (Character.isLetter(c)) {
                raw.addAll(readWord());
            } else if ("+-*/^".indexOf(c) >= 0) {
                raw.add(new Token(Token.Type.OPERATOR, String.valueOf(c), start));
                pos++;
            } else if (c == '(') {
                raw.add(new Token(Token.Type.LPAREN, "(", start));
                pos++;
            } else if (c == ')') {
                raw.add(new Token(Token.Type.RPAREN, ")", start));
                pos++;
            } else {
                throw new SyntaxError("Caractère non reconnu « " + c + " »", start);
            }
        }
        return insertImplicitMultiplication(raw);
    }

    private String readNumber() {
        int start = pos;
        boolean dot = false;
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            if (input.charAt(pos) == '.') {
                if (dot) throw new SyntaxError("Nombre mal formé", start);
                dot = true;
            }
            pos++;
        }
        String number = input.substring(start, pos);
        if (number.equals(".")) throw new SyntaxError("Nombre mal formé", start);
        return number;
    }

    /** Lit une suite de lettres : nom de fonction, constante, ou x (éventuellement collés : « xx » = x·x). */
    private List<Token> readWord() {
        int start = pos;
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) pos++;
        String word = input.substring(start, pos).toLowerCase();

        List<Token> out = new ArrayList<>();
        if (FUNCTIONS.contains(word)) {
            out.add(new Token(Token.Type.FUNCTION, word, start));
        } else if (CONSTANTS.contains(word)) {
            out.add(new Token(Token.Type.CONSTANT, word, start));
        } else if (word.chars().allMatch(ch -> ch == 'x')) {
            for (int i = 0; i < word.length(); i++) out.add(new Token(Token.Type.VARIABLE, "x", start + i));
        } else {
            throw new SyntaxError("Nom inconnu « " + word + " » (fonctions : " + String.join(", ", FUNCTIONS.stream().sorted().toList()) + ")", start);
        }
        return out;
    }

    private static boolean endsOperand(Token t) {
        return t.type == Token.Type.NUMBER || t.type == Token.Type.VARIABLE
                || t.type == Token.Type.CONSTANT || t.type == Token.Type.RPAREN;
    }

    private static boolean startsOperand(Token t) {
        return t.type == Token.Type.NUMBER || t.type == Token.Type.VARIABLE || t.type == Token.Type.CONSTANT
                || t.type == Token.Type.FUNCTION || t.type == Token.Type.LPAREN;
    }

    private static List<Token> insertImplicitMultiplication(List<Token> raw) {
        List<Token> out = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            Token t = raw.get(i);
            if (i > 0 && endsOperand(raw.get(i - 1)) && startsOperand(t)) {
                out.add(new Token(Token.Type.OPERATOR, "*", t.position));
            }
            out.add(t);
        }
        return out;
    }
}
