package javapolis.factory.student;

import javapolis.factory.model.Resource;

/** Stage 5. Deliver 40 plates by tick 100, including the deadline tick itself. */
public final class Contract {
    private final int target;
    private final int deadline;
    private boolean complete;

    public Contract(int target, int deadline) {
        if (target <= 0 || deadline <= 0) throw new IllegalArgumentException("Некорректный контракт");
        this.target = target;
        this.deadline = deadline;
    }

    public int getTarget() { return target; }
    public int getDeadline() { return deadline; }
    public boolean isComplete() { return complete; }

    public boolean submit(Inventory warehouse, int tick) {
        // TODO stage 5: validate time and completion, withdraw exactly target plates, remember success.
        return false;
    }
}
