package javapolis.factory.student;

import javapolis.factory.model.Machine;
import javapolis.factory.model.Recipe;
import javapolis.factory.model.Status;

/** Stage 3. Ingredients stay at the input until a whole batch finishes. */
public final class Processing {
    public void tick(Machine machine, Recipe recipe) {
        // TODO stage 3: pause -> recipe -> input -> output -> progress -> complete batch.
        // Preserve progress during a pause or shortage. Change stocks only on completion.
        machine.setStatus(Status.TODO);
    }
}
