# Traceur de fonctions — Projet TLA

Petite application Java (Swing) qui trace le graphe d'une fonction mathématique saisie en texte, par exemple `x^2 + 3*x - 1`. Projet réalisé dans le cadre du cours de Théorie des Langages et Automates (TLA).

## Principe

Le cœur du projet n'est pas l'interface graphique, mais ce qui se passe derrière : transformer une chaîne de caractères en une structure exploitable, comme le fait un interpréteur.

1. **Lexer** (`Lexer.java`) — découpe l'expression saisie en tokens (nombres, variables, opérateurs, parenthèses)
2. **Parser** (`Parser.java`) — parseur récursif descendant qui construit un arbre de syntaxe abstraite (AST) à partir des tokens, en respectant la priorité des opérateurs : `+`/`-` avant `*`/`/` avant `^`
3. **ASTNode** (`ASTNode.java`) — représente les nœuds de l'arbre (opérateurs, nombres, variable `x`)
4. **Plot / PlotPanel** (`Plot.java`, `PlotPanel.java`) — évalue l'AST pour chaque valeur de `x` et trace le graphe correspondant
5. **Main** (`Main.java`) — interface Swing : champ de saisie de la fonction, bouton de validation, slider pour ajuster la plage affichée

## Grammaire supportée

- Opérateurs : `+`, `-`, `*`, `/`, `^`
- Parenthèses pour forcer l'ordre d'évaluation
- Variable `x` et nombres

## Limites connues

- Pas de fonctions mathématiques (`sin`, `cos`, `sqrt`...)
- Pas de nombres négatifs en entrée directe
- `^` est évalué de gauche à droite (non associatif à droite, contrairement à la convention mathématique usuelle)

## Lancer le projet

```bash
javac *.java
java Main
```

## Stack technique

Java · Swing · parsing récursif descendant (sans bibliothèque externe)
