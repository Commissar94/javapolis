package javapolis.factory.model;

public enum MachineType {
    MINE(80), SMELTER(100), PRESS(120), WAREHOUSE(60);

    private final int price;

    MachineType(int price) { this.price = price; }
    public int getPrice() { return price; }
}
