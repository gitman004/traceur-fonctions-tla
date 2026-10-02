package tlaprojet;

/** Un lexème : son type, sa valeur textuelle et sa position dans l'expression. */
class Token {
    enum Type { NUMBER, VARIABLE, CONSTANT, FUNCTION, OPERATOR, LPAREN, RPAREN }

    final Type type;
    final String value;
    final int position;

    Token(Type type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    boolean is(Type t, String v) {
        return type == t && value.equals(v);
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}
