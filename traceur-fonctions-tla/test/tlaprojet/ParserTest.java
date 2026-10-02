package tlaprojet;

/**
 * Tests du lexer, du parser et de l'évaluation, sans dépendance externe.
 *
 *   javac -d out src/tlaprojet/*.java test/tlaprojet/*.java
 *   java -cp out tlaprojet.ParserTest
 */
public class ParserTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // Priorité des opérateurs
        tree("1 + 2 * 3", "(1 + (2 * 3))");
        tree("(1 + 2) * 3", "((1 + 2) * 3)");
        tree("2 * x ^ 2", "(2 * (x ^ 2))");
        value("10 - 4 - 3", 0, 3);          // gauche à droite pour - et /
        value("12 / 3 / 2", 0, 2);

        // Puissance associative à droite
        tree("2^3^2", "(2 ^ (3 ^ 2))");
        value("2^3^2", 0, 512);

        // Moins unaire
        value("-3 + 5", 0, 2);
        value("-x^2", 3, -9);               // -(x^2), convention mathématique
        value("(-x)^2", 3, 9);
        value("2^-1", 0, 0.5);
        value("x - -2", 1, 3);
        value("+x", 4, 4);

        // Multiplication implicite
        value("2x", 3, 6);
        value("3(x + 1)", 1, 6);
        value("(x + 1)(x - 1)", 3, 8);
        value("2sin(x)", Math.PI / 2, 2);

        // Fonctions et constantes
        value("sin(pi / 2)", 0, 1);
        value("cos(0) + exp(0)", 0, 2);
        value("sqrt(16) + abs(-3)", 0, 7);
        value("ln(e)", 0, 1);
        value("x^2 + 3*x - 1", 2, 9);
        value("1.5x", 2, 3);

        // Erreurs signalées clairement (et non plus ignorées)
        error("", "Expression vide");
        error("(x + 1", "Parenthèse ouverte non fermée");
        error("x + 1)", "Parenthèse fermante en trop");
        error("x +", "Expression incomplète");
        error("2 * * 3", "Symbole inattendu");
        error("y + 1", "Nom inconnu");
        error("x $ 2", "Caractère non reconnu");
        error("sin x", "doit être suivi d'une parenthèse");
        error("1.2.3", "Nombre mal formé");

        System.out.println();
        System.out.println(passed + " tests réussis, " + failed + " en échec");
        if (failed > 0) System.exit(1);
    }

    private static void tree(String expr, String expected) {
        check(expr + " -> " + expected, Parser.parse(expr).toString().equals(expected),
                "obtenu " + Parser.parse(expr));
    }

    private static void value(String expr, double x, double expected) {
        double got = Parser.parse(expr).evaluate(x);
        check(expr + " (x = " + x + ") = " + expected, Math.abs(got - expected) < 1e-9, "obtenu " + got);
    }

    private static void error(String expr, String expectedMessage) {
        try {
            Parser.parse(expr);
            check("« " + expr + " » refusée", false, "aucune erreur levée");
        } catch (SyntaxError e) {
            check("« " + expr + " » refusée : " + expectedMessage, e.getMessage().contains(expectedMessage),
                    "message : " + e.getMessage());
        }
    }

    private static void check(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("  ok    " + name);
        } else {
            failed++;
            System.out.println("  ÉCHEC " + name + " — " + detail);
        }
    }
}
