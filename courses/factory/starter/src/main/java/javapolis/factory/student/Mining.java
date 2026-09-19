package javapolis.factory.student;

import javapolis.factory.model.Machine;
import javapolis.factory.model.Resource;
import javapolis.factory.model.Status;

/** Stage 2. A running mine produces one ore per logical tick. */
public final class Mining {
    public void tick(Machine machine) {
        // TODO stage 2: respect pause and capacity; count only ore actually added.
        machine.setStatus(Status.TODO);
    }
}
