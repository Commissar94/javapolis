package javapolis.factory.student;

import javapolis.factory.model.Machine;
import javapolis.factory.model.Resource;
import javapolis.factory.model.Status;

/** Stage 2. A running mine produces one ore per logical tick. */
public final class Mining {
    public void tick(Machine machine) {
        if (!machine.isEnabled()) {
            machine.setStatus(Status.PAUSED);
            return;
        }
        if (!machine.getOutput().add(Resource.ORE, 1)) {
            machine.setStatus(Status.OUTPUT_FULL);
            return;
        }
        machine.addProduced(1);
        machine.setStatus(Status.WORKING);
    }
}
