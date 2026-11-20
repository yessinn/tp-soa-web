package metier;

/**
 * Classe métier : logique de calcul, indépendante de REST/HTTP.
 */
public class Calculatrice {

    public double somme(double a, double b) {
        return a + b;
    }

    public double soustraction(double a, double b) {
        return a - b;
    }

    public double multiplication(double a, double b) {
        return a * b;
    }

    /**
     * @throws ArithmeticException si b vaut 0
     */
    public double division(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division par zéro impossible : b doit être différent de 0");
        }
        return a / b;
    }

    /**
     * Effectue l'opération désignée par son symbole (+, -, *, /).
     *
     * @throws IllegalArgumentException si le symbole est inconnu
     * @throws ArithmeticException      en cas de division par zéro
     */
    public double calculer(String op, double a, double b) {
        if (op == null) {
            throw new IllegalArgumentException("L'opérateur 'op' est obligatoire (+, -, *, /)");
        }
        return switch (op.trim()) {
            case "+" -> somme(a, b);
            case "-" -> soustraction(a, b);
            case "*" -> multiplication(a, b);
            case "/" -> division(a, b);
            default -> throw new IllegalArgumentException(
                    "Opérateur inconnu : '" + op + "' (valeurs acceptées : +, -, *, /)");
        };
    }
}
