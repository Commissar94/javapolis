package javapolis.factory;

import javapolis.factory.model.*;
import javapolis.factory.student.Mining;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise") @Tag("stage-2")
class Stage2MiningTest {
    @Test void producesOneOreEveryTick() {
        Machine mine = new Machine(1, MachineType.MINE, 0, 0);
        Mining logic = new Mining();
        for (int i = 0; i < 5; i++) logic.tick(mine);
        assertEquals(5, mine.getOutput().count(Resource.ORE));
        assertEquals(5, mine.getProduced());
        assertEquals(Status.WORKING, mine.getStatus());
    }

    @Test void pausesWithoutChangingProduction() {
        Machine mine = new Machine(1, MachineType.MINE, 0, 0);
        mine.setEnabled(false);
        new Mining().tick(mine);
        assertEquals(0, mine.getOutput().total());
        assertEquals(0, mine.getProduced());
        assertEquals(Status.PAUSED, mine.getStatus());
    }

    @Test void stopsOnFullOutputAndResumesWhenSpaceAppears() {
        Machine mine = new Machine(1, MachineType.MINE, 0, 0);
        Mining logic = new Mining();
        for (int i = 0; i < 30; i++) logic.tick(mine);
        assertEquals(24, mine.getOutput().total());
        assertEquals(24, mine.getProduced());
        assertEquals(Status.OUTPUT_FULL, mine.getStatus());
        assertTrue(mine.getOutput().remove(Resource.ORE, 1));
        logic.tick(mine);
        assertEquals(24, mine.getOutput().total());
        assertEquals(25, mine.getProduced());
        assertEquals(Status.WORKING, mine.getStatus());
    }

    @Test void machinesHaveIndependentStocks() {
        Machine a = new Machine(1, MachineType.MINE, 0, 0), b = new Machine(2, MachineType.MINE, 1, 0);
        new Mining().tick(a);
        assertEquals(1, a.getOutput().total());
        assertEquals(0, b.getOutput().total());
    }
}
