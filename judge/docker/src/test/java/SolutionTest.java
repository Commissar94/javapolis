import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolutionTest {

    @Test
    void sum_basic() {
        assertEquals(5, Solution.sum(2, 3));
    }

    @Test
    void sum_with_negative() {
        assertEquals(1, Solution.sum(3, -2));
    }

    @Test
    void sum_with_negative_2() {
        assertEquals(0, Solution.sum(3, -2));
    }
}