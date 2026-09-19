package javapolis.factory;

import javapolis.factory.model.MachineType;
import javapolis.factory.runtime.FactoryEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** These checks run even before the student implements the exercise methods. */
class FactoryShellTest {
    @Test void newFactoryIsPausedAndHasNoInventedResources() {
        FactoryEngine engine = new FactoryEngine("student");
        var state = engine.snapshot();
        assertFalse(state.running()); assertEquals(0, state.tick());
        assertEquals(3, state.machines().size()); assertEquals(2, state.links().size());
        assertEquals(300, state.budget());
        assertTrue(state.machines().stream().allMatch(m -> m.input().isEmpty() && m.output().isEmpty()));
    }

    @Test void buildingRejectsOccupiedCellsInvalidCoordinatesAndOverspending() {
        FactoryEngine engine = new FactoryEngine("student");
        assertThrows(IllegalArgumentException.class, () -> engine.build(MachineType.MINE, 1, 3));
        assertThrows(IllegalArgumentException.class, () -> engine.build(MachineType.MINE, 12, 3));
        assertEquals(300, engine.snapshot().budget());
        engine.build(MachineType.PRESS, 0, 0); engine.build(MachineType.PRESS, 1, 0);
        assertThrows(IllegalArgumentException.class, () -> engine.build(MachineType.MINE, 2, 0));
        assertEquals(60, engine.snapshot().budget());
    }

    @Test void linksValidateDirectionAndDoNotDuplicate() {
        FactoryEngine engine = new FactoryEngine("student");
        assertThrows(IllegalArgumentException.class, () -> engine.connect(1, 2));
        assertThrows(IllegalArgumentException.class, () -> engine.connect(2, 1));
        assertThrows(IllegalArgumentException.class, () -> engine.connect(1, 1));
        assertThrows(IllegalArgumentException.class, () -> engine.connect(3, 2));
        assertThrows(IllegalArgumentException.class, () -> engine.connect(1, 99));
        assertEquals(2, engine.snapshot().links().size());
    }

    @Test void challengeDisablesSuppliesAndStopsAtTheDeadline() {
        FactoryEngine engine = new FactoryEngine("student");
        engine.reset("contract");
        assertThrows(IllegalArgumentException.class, () -> engine.supply(2));
        engine.advance(100); engine.advance(1);
        assertEquals(100, engine.snapshot().tick());
        assertFalse(engine.snapshot().running());
        assertThrows(IllegalArgumentException.class, () -> engine.setRunning(true));
    }

    @Test void resetAndDemolitionKeepTheBudgetConsistent() {
        FactoryEngine engine = new FactoryEngine("student");
        int id = engine.build(MachineType.PRESS, 0, 0);
        engine.removeMachine(id);
        assertEquals(300, engine.snapshot().budget());
        assertThrows(IllegalArgumentException.class, () -> engine.removeMachine(1));
        engine.setRunning(true); engine.reset("sandbox");
        assertFalse(engine.snapshot().running());
        assertEquals(0, engine.snapshot().tick());
    }
}
