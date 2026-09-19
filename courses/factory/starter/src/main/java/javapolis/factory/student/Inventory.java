package javapolis.factory.student;

import javapolis.factory.model.Resource;
import java.util.EnumMap;
import java.util.Map;

/** Stage 1. The capacity is shared by ALL resource types. */
public final class Inventory {
    private final int capacity;
    private final Map<Resource, Integer> amounts = new EnumMap<>(Resource.class);

    public Inventory(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Вместимость должна быть положительной");
        this.capacity = capacity;
    }

    public int count(Resource resource) { return amounts.getOrDefault(resource, 0); }
    public int getCapacity() { return capacity; }

    public int total() {
        int sum = 0;
        for (int amount : amounts.values()) sum += amount;
        return sum;
    }

    public int freeSpace() { return capacity - total(); }
    public Map<Resource, Integer> snapshot() { return Map.copyOf(amounts); }

    public boolean add(Resource resource, int amount) {
        // TODO stage 1: reject null, non-positive amounts and overflow; otherwise put the new balance.
        return false;
    }

    public boolean remove(Resource resource, int amount) {
        // TODO stage 1: reject invalid requests and shortages without changing anything.
        return false;
    }
}
