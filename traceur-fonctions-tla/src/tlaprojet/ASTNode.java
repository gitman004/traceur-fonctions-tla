package tlaprojet;

/**
 * Nœud de l'arbre de syntaxe abstraite.
 * Feuilles : nombre, variable x, constante. Nœuds internes : opérateur binaire,
 * moins unaire, appel de fonction.
 */
class ASTNode {
    enum Kind { NUMBER, VARIABLE, CONSTANT, BINARY, NEGATE, FUNCTION }

    final Kind kind;
    final String value;   // opérateur, nom de fonction/constante, ou texte du nombre
    final ASTNode left;   // opérande gauche, ou unique opérande (moins unaire, fonction)
    final ASTNode right;  // opérande droit (opérateur binaire seulement)
    private final double number;

    private ASTNode(Kind kind, String value, ASTNode left, ASTNode right) {
        this.kind = kind;
        this.value = value;
        this.left = left;
        this.right = right;
        this.number = kind == Kind.NUMBER ? Double.parseDouble(value) : 0;
    }

    static ASTNode number(String text) { return new ASTNode(Kind.NUMBER, text, null, null); }
    static ASTNode variable() { return new ASTNode(Kind.VARIABLE, "x", null, null); }
    static ASTNode constant(String name) { return new ASTNode(Kind.CONSTANT, name, null, null); }
    static ASTNode binary(String op, ASTNode l, ASTNode r) { return new ASTNode(Kind.BINARY, op, l, r); }
    static ASTNode negate(ASTNode operand) { return new ASTNode(Kind.NEGATE, "-", operand, null); }
    static ASTNode function(String name, ASTNode arg) { return new ASTNode(Kind.FUNCTION, name, arg, null); }

    double evaluate(double x) {
        switch (kind) {
            case NUMBER: return number;
            case VARIABLE: return x;
            case CONSTANT: return value.equals("pi") ? Math.PI : Math.E;
            case NEGATE: return -left.evaluate(x);
            case FUNCTION: return applyFunction(left.evaluate(x));
            case BINARY: {
                double a = left.evaluate(x), b = right.evaluate(x);
                switch (value) {
                    case "+": return a + b;
                    case "-": return a - b;
                    case "*": return a * b;
                    case "/": return a / b;
                    case "^": return Math.pow(a, b);
                    default: throw new IllegalStateException("Opérateur inconnu : " + value);
                }
            }
            default: throw new IllegalStateException("Nœud inconnu : " + kind);
        }
    }

    private double applyFunction(double a) {
        switch (value) {
            case "sin": return Math.sin(a);
            case "cos": return Math.cos(a);
            case "tan": return Math.tan(a);
            case "sqrt": return Math.sqrt(a);
            case "exp": return Math.exp(a);
            case "ln": return Math.log(a);
            case "abs": return Math.abs(a);
            default: throw new IllegalStateException("Fonction inconnue : " + value);
        }
    }

    /** Forme entièrement parenthésée, utile pour vérifier la structure de l'arbre dans les tests. */
    @Override
    public String toString() {
        switch (kind) {
            case NUMBER: case VARIABLE: case CONSTANT: return value;
            case NEGATE: return "(-" + left + ")";
            case FUNCTION: return value + "(" + left + ")";
            default: return "(" + left + " " + value + " " + right + ")";
        }
    }
}
