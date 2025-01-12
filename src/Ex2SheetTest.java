import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class Ex2SheetTest {
    private Ex2Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Ex2Sheet();
    }

    @Test
    void testEmptyCell() {
        assertEquals("", sheet.value(0, 0));
    }

    @Test
    void testSimpleNumber() {
        sheet.set(0, 0, "5.0");
        assertEquals("5.0", sheet.value(0, 0));
    }

    @Test
    void testSimpleText() {
        sheet.set(0, 0, "Hello");
        assertEquals("Hello", sheet.value(0, 0));
    }

    @Test
    void testSimpleFormula() {
        sheet.set(0, 0, "=1+1");
        assertEquals("2.0", sheet.value(0, 0));
    }

    @Test
    void testFormulaWithCell() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A0");
        assertEquals("5.0", sheet.value(0, 1));
    }

    @Test
    void testComplexFormula() {
        sheet.set(0, 0, "=2*3+4");
        assertEquals("10.0", sheet.value(0, 0));
    }

    @Test
    void testFormulaWithParentheses() {
        sheet.set(0, 0, "=(2+3)*4");
        assertEquals("20.0", sheet.value(0, 0));
    }

    @Test
    void testMultipleCellReferences() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "3");
        sheet.set(0, 2, "=A0+A1");
        assertEquals("8.0", sheet.value(0, 2));
    }

    @Test
    void testCyclicReference() {
        sheet.set(0, 0, "=A1");
        sheet.set(0, 1, "=A0");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));
    }

    @Test
    void testInvalidFormula() {
        sheet.set(0, 0, "=1+");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));
    }

    @Test
    void testInvalidCellReference() {
        sheet.set(0, 0, "=Z99");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));
    }

    @Test
    void testMathOperations() {
        sheet.set(0, 0, "=2+3");
        assertEquals("5.0", sheet.value(0, 0));
        sheet.set(0, 1, "=5-2");
        assertEquals("3.0", sheet.value(0, 1));
        sheet.set(0, 2, "=4*3");
        assertEquals("12.0", sheet.value(0, 2));
        sheet.set(0, 3, "=10/2");
        assertEquals("5.0", sheet.value(0, 3));
    }

    @Test
    void testDivisionByZero() {
        sheet.set(0, 0, "=1/0");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));
    }

    @Test
    void testOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> {
            sheet.set(-1, 0, "test");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            sheet.set(0, -1, "test");
        });
    }
}
