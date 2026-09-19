package javapolis.factory.student;

import javapolis.factory.model.MachineType;
import javapolis.factory.model.Recipe;
import javapolis.factory.model.Resource;
import java.util.Map;

/** Stage 4. Extend this catalogue; the processing algorithm should stay the same. */
public final class Recipes {
    private final Map<MachineType, Recipe> recipes = Map.of(
            MachineType.SMELTER, new Recipe(Resource.ORE, 1, Resource.INGOT, 1, 2),
            MachineType.PRESS, new Recipe(Resource.INGOT, 3, Resource.PLATE, 2, 3)
    );

    public Recipe forMachine(MachineType type) { return recipes.get(type); }
}
