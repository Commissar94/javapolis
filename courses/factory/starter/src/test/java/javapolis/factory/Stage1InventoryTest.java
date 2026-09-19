package javapolis.factory;

import javapolis.factory.model.Resource;
import javapolis.factory.student.Inventory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise") @Tag("stage-1")
class Stage1InventoryTest {
    @Test void acceptsAndRemovesResources() {
        Inventory stock = new Inventory(10);
        assertEquals(0, stock.count(Resource.ORE));
        assertTrue(stock.add(Resource.ORE, 4));
        assertTrue(stock.add(Resource.ORE, 2));
        assertTrue(stock.remove(Resource.ORE, 3));
        assertEquals(3, stock.count(Resource.ORE));
        assertEquals(7, stock.freeSpace());
    }

    @Test void sharesCapacityAcrossResourcesAndAcceptsExactFit() {
        Inventory stock = new Inventory(5);
        assertTrue(stock.add(Resource.ORE, 3));
        assertTrue(stock.add(Resource.INGOT, 2));
        assertFalse(stock.add(Resource.PLATE, 1));
        assertEquals(5, stock.total());
        assertEquals(0, stock.freeSpace());
    }

    @ParameterizedTest @ValueSource(ints = {0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejectsInvalidOrExcessiveAmountsWithoutMutation(int amount) {
        Inventory stock = new Inventory(10);
        assertTrue(stock.add(Resource.ORE, 3));
        assertFalse(stock.add(Resource.ORE, amount));
        assertFalse(stock.remove(Resource.ORE, amount));
        assertEquals(3, stock.total());
    }

    @Test void rejectsNullAndShortages() {
        Inventory stock = new Inventory(10);
        assertFalse(stock.add(null, 1));
        assertFalse(stock.remove(null, 1));
        assertFalse(stock.remove(Resource.ORE, 1));
        assertTrue(stock.add(Resource.ORE, 2));
        assertFalse(stock.remove(Resource.ORE, 3));
        assertEquals(2, stock.count(Resource.ORE));
    }

    @Test void snapshotsCannotMutateTheOriginalAndDoNotChangeLater() {
        Inventory stock = new Inventory(10);
        assertTrue(stock.add(Resource.ORE, 2));
        var snapshot = stock.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put(Resource.ORE, 50));
        assertTrue(stock.add(Resource.ORE, 1));
        assertEquals(2, snapshot.get(Resource.ORE));
        assertEquals(3, stock.count(Resource.ORE));
    }

    @Test void fullIntCapacityDoesNotOverflow() {
        Inventory stock = new Inventory(Integer.MAX_VALUE);
        assertTrue(stock.add(Resource.ORE, Integer.MAX_VALUE));
        assertFalse(stock.add(Resource.ORE, 1));
        assertEquals(Integer.MAX_VALUE, stock.total());
    }
}
