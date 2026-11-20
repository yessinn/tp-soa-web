package metier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatriceTest {

    private final Calculatrice calculatrice = new Calculatrice();

    @Test
    void quatreOperations() {
        assertEquals(8.0, calculatrice.somme(5, 3));
        assertEquals(6.0, calculatrice.soustraction(10, 4));
        assertEquals(42.0, calculatrice.multiplication(6, 7));
        assertEquals(3.75, calculatrice.division(15, 4));
    }

    @Test
    void divisionParZeroLeveUneException() {
        ArithmeticException e = assertThrows(ArithmeticException.class, () -> calculatrice.division(10, 0));
        assertTrue(e.getMessage().contains("zéro"));
    }

    @Test
    void calculerSelonLeSymbole() {
        assertEquals(8.0, calculatrice.calculer("+", 5, 3));
        assertEquals(6.0, calculatrice.calculer("-", 10, 4));
        assertEquals(42.0, calculatrice.calculer(" * ", 6, 7));
        assertEquals(3.75, calculatrice.calculer("/", 15, 4));
        assertThrows(IllegalArgumentException.class, () -> calculatrice.calculer("%", 1, 2));
        assertThrows(IllegalArgumentException.class, () -> calculatrice.calculer(null, 1, 2));
    }
}
