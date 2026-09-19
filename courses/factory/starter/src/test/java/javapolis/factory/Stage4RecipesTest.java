package javapolis.factory;

import javapolis.factory.model.*;
import javapolis.factory.student.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise") @Tag("stage-4")
class Stage4RecipesTest {
    @Test void pressHasTheDocumentedRecipe() {
        Recipe recipe = new Recipes().forMachine(MachineType.PRESS);
        assertNotNull(recipe, "Добавь рецепт пресса в Recipes.java");
        assertEquals(Resource.INGOT, recipe.getInput());
        assertEquals(3, recipe.getInputAmount());
        assertEquals(Resource.PLATE, recipe.getOutput());
        assertEquals(2, recipe.getOutputAmount());
        assertEquals(3, recipe.getDuration());
    }

    @Test void pressUsesTheSameProcessingAlgorithm() {
        Machine press = new Machine(1, MachineType.PRESS, 0, 0);
        Recipe recipe = new Recipes().forMachine(MachineType.PRESS);
        assertNotNull(recipe);
        assertTrue(press.getInput().add(Resource.INGOT, 6));
        Processing logic = new Processing();
        for (int i = 0; i < 6; i++) logic.tick(press, recipe);
        assertEquals(0, press.getInput().total());
        assertEquals(4, press.getOutput().count(Resource.PLATE));
        assertEquals(4, press.getProduced());
    }

    @Test void oneFreeSlotCannotFitTwoPlates() {
        Machine press = new Machine(1, MachineType.PRESS, 0, 0);
        Recipe recipe = new Recipes().forMachine(MachineType.PRESS);
        assertNotNull(recipe);
        assertTrue(press.getInput().add(Resource.INGOT, 3));
        assertTrue(press.getOutput().add(Resource.PLATE, 23));
        new Processing().tick(press, recipe);
        assertEquals(Status.OUTPUT_FULL, press.getStatus());
        assertEquals(3, press.getInput().total());
        assertEquals(23, press.getOutput().total());
    }

    @Test void mineAndWarehouseDoNotHaveProcessingRecipes() {
        Recipes recipes = new Recipes();
        assertNull(recipes.forMachine(MachineType.MINE));
        assertNull(recipes.forMachine(MachineType.WAREHOUSE));
        assertNotNull(recipes.forMachine(MachineType.SMELTER));
    }
}
