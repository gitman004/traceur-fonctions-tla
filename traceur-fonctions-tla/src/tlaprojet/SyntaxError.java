package tlaprojet;

/** Erreur de lecture ou d'analyse, avec la position fautive (indice dans la chaîne saisie). */
class SyntaxError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    final int position;

    SyntaxError(String message, int position) {
        super(message + " (position " + (position + 1) + ")");
        this.position = position;
    }
}
