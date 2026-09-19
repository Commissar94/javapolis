package javapolis.factory.model;

import javapolis.factory.student.Inventory;

/** Ready-made model for the map. Production rules live in the student package. */
public final class Machine {
    private final int id;
    private final MachineType type;
    private final int x;
    private final int y;
    private final Inventory input;
    private final Inventory output;
    private boolean enabled = true;
    private int progress;
    private int produced;
    private Status status = Status.READY;

    public Machine(int id, MachineType type, int x, int y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.input = new Inventory(type == MachineType.WAREHOUSE ? 200 : 24);
        this.output = new Inventory(24);
    }

    public int getId() { return id; }
    public MachineType getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Inventory getInput() { return input; }
    public Inventory getOutput() { return output; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getProduced() { return produced; }
    public void addProduced(int amount) { this.produced += amount; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
