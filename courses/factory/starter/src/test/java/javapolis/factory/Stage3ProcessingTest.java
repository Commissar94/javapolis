package javapolis.factory;

import javapolis.factory.model.*;
import javapolis.factory.student.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise") @Tag("stage-3")
class Stage3ProcessingTest {
    private final Processing logic = new Processing();
    private final Recipe recipe = new Recipes().forMachine(MachineType.SMELTER);
    private final Machine machine = new Machine(1, MachineType.SMELTER, 0, 0);

    @Test void consumesOnlyWhenTheWholeBatchFinishes() {
        assertTrue(machine.getInput().add(Resource.ORE, 1));
        logic.tick(machine, recipe);
        assertEquals(1, machine.getProgress());
        assertEquals(1, machine.getInput().count(Resource.ORE));
        assertEquals(0, machine.getOutput().total());
        logic.tick(machine, recipe);
        assertEquals(0, machine.getProgress());
        assertEquals(0, machine.getInput().total());
        assertEquals(1, machine.getOutput().count(Resource.INGOT));
        assertEquals(1, machine.getProduced());
    }

    @Test void emptyInputDoesNotAdvanceProgress() {
        logic.tick(machine, recipe);
        assertEquals(Status.NO_INPUT, machine.getStatus());
        assertEquals(0, machine.getProgress());
    }

    @Test void outputMustFitTheWholeBatchBeforeWorkAdvances() {
        assertTrue(machine.getInput().add(Resource.ORE, 1));
        assertTrue(machine.getOutput().add(Resource.INGOT, 24));
        logic.tick(machine, recipe);
        assertEquals(Status.OUTPUT_FULL, machine.getStatus());
        assertEquals(0, machine.getProgress());
        assertEquals(1, machine.getInput().total());
    }

    @Test void pausePreservesPartialProgress() {
        assertTrue(machine.getInput().add(Resource.ORE, 1));
        logic.tick(machine, recipe);
        machine.setEnabled(false);
        logic.tick(machine, recipe);
        assertEquals(Status.PAUSED, machine.getStatus());
        assertEquals(1, machine.getProgress());
        machine.setEnabled(true);
        logic.tick(machine, recipe);
        assertEquals(1, machine.getOutput().count(Resource.INGOT));
    }

    @Test void shortagesPreservePartialProgress() {
        assertTrue(machine.getInput().add(Resource.ORE, 1));
        logic.tick(machine, recipe);
        assertTrue(machine.getInput().remove(Resource.ORE, 1));
        logic.tick(machine, recipe);
        assertEquals(Status.NO_INPUT, machine.getStatus());
        assertEquals(1, machine.getProgress());
        assertTrue(machine.getInput().add(Resource.ORE, 1));
        logic.tick(machine, recipe);
        assertEquals(1, machine.getOutput().total());
    }

    @Test void missingRecipeAndPauseHaveExplicitStates() {
        logic.tick(machine, null);
        assertEquals(Status.NO_RECIPE, machine.getStatus());
        machine.setEnabled(false);
        logic.tick(machine, null);
        assertEquals(Status.PAUSED, machine.getStatus());
    }

    @Test void theAlgorithmUsesRecipeFieldsRatherThanHardcodedIronAmounts() {
        Recipe custom = new Recipe(Resource.ORE, 2, Resource.PLATE, 3, 4);
        assertTrue(machine.getInput().add(Resource.ORE, 4));
        for (int i = 0; i < 8; i++) logic.tick(machine, custom);
        assertEquals(0, machine.getInput().total());
        assertEquals(6, machine.getOutput().count(Resource.PLATE));
        assertEquals(6, machine.getProduced());
    }
}
