package javapolis.factory.student;

import javapolis.factory.model.Machine;
import javapolis.factory.model.Recipe;
import javapolis.factory.model.Status;

/** Stage 3. Ingredients stay at the input until a whole batch finishes. */
public final class Processing {
    public void tick(Machine machine, Recipe recipe) {
        if (!machine.isEnabled()) {
            machine.setStatus(Status.PAUSED);
            return;
        }
        if (recipe == null) {
            machine.setStatus(Status.NO_RECIPE);
            return;
        }
        if (machine.getInput().count(recipe.getInput()) < recipe.getInputAmount()) {
            machine.setStatus(Status.NO_INPUT);
            return;
        }
        if (machine.getOutput().freeSpace() < recipe.getOutputAmount()) {
            machine.setStatus(Status.OUTPUT_FULL);
            return;
        }
        machine.setProgress(machine.getProgress() + 1);
        machine.setStatus(Status.WORKING);
        if (machine.getProgress() >= recipe.getDuration()) {
            machine.getInput().remove(recipe.getInput(), recipe.getInputAmount());
            machine.getOutput().add(recipe.getOutput(), recipe.getOutputAmount());
            machine.addProduced(recipe.getOutputAmount());
            machine.setProgress(0);
        }
    }
}
