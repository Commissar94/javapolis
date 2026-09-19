package javapolis.factory.runtime;

import javapolis.factory.model.*;
import javapolis.factory.student.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/** Ready-made local game shell. All entry points share one lock; student rules run sequentially. */
@Component
public final class FactoryEngine {
    private final String mode;
    private final Mining mining = new Mining();
    private final Processing processing = new Processing();
    private final Recipes recipes = new Recipes();
    private final List<Machine> machines = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private final Deque<String> events = new ArrayDeque<>();
    private Contract contract;
    private int tick;
    private int nextId;
    private int budget;
    private int speed = 1;
    private boolean running;
    private String scenario;
    private String error;

    public FactoryEngine(@Value("${factory.mode:student}") String mode) {
        this.mode = mode;
        reset("sandbox");
    }

    public synchronized void reset(String scenario) {
        if (!Set.of("sandbox", "contract").contains(scenario)) throw bad("Неизвестный сценарий");
        this.scenario = scenario;
        tick = 0; nextId = 4; budget = 300; running = false; speed = 1; error = null;
        contract = new Contract(40, 100);
        machines.clear(); links.clear(); events.clear();
        machines.add(new Machine(1, MachineType.MINE, 1, 3));
        machines.add(new Machine(2, MachineType.SMELTER, 4, 3));
        machines.add(new Machine(3, MachineType.WAREHOUSE, 10, 3));
        links.add(new Link(1, 2));
        links.add(new Link(2, 3));
        event("Новая смена. Шахта, плавильня и склад готовы к работе.");
    }

    public synchronized void setRunning(boolean running) {
        if (running && atDeadline()) throw bad("Смена закончена. Сдай готовые пластины или начни заново.");
        this.running = running;
        error = null;
    }

    public synchronized void setSpeed(int speed) {
        if (speed != 1 && speed != 2 && speed != 4) throw bad("Скорость: 1, 2 или 4");
        this.speed = speed;
    }

    @Scheduled(fixedDelay = 500)
    public synchronized void pulse() {
        if (running) advance(speed);
    }

    public synchronized void advance(int steps) {
        if (steps < 1 || steps > 100) throw bad("За раз можно выполнить от 1 до 100 тактов");
        for (int i = 0; i < steps; i++) {
            if (atDeadline() || contract.isComplete()) { running = false; break; }
            try {
                tick++;
                transfer();
                for (Machine machine : machines) {
                    if (machine.getType() == MachineType.MINE) mining.tick(machine);
                    else if (machine.getType() != MachineType.WAREHOUSE)
                        processing.tick(machine, recipes.forMachine(machine.getType()));
                }
                if (atDeadline()) {
                    running = false;
                    event("Смена закончена. Контракт ещё можно сдать на такте " + tick + ".");
                }
            } catch (RuntimeException ex) {
                running = false;
                error = "Ошибка в логике завода: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
                event(error);
                break;
            }
        }
    }

    private boolean atDeadline() { return scenario.equals("contract") && tick >= contract.getDeadline(); }

    private void transfer() {
        for (Link link : links) link.moved = 0;
        for (Machine source : machines) {
            List<Link> outgoing = links.stream().filter(link -> link.from == source.getId()).toList();
            Resource resource = outputResource(source.getType());
            if (outgoing.isEmpty() || resource == null) continue;
            // Rotate the first destination so a second smelter is not starved by list order.
            for (int offset = 0; offset < outgoing.size(); offset++) {
                Link link = outgoing.get((tick + offset) % outgoing.size());
                Inventory destination = machine(link.to).getInput();
                if (source.getOutput().count(resource) > 0 && destination.freeSpace() > 0
                        && source.getOutput().remove(resource, 1)) {
                    if (!destination.add(resource, 1)) {
                        source.getOutput().add(resource, 1);
                        throw new IllegalStateException("Склад отказал в допустимой поставке. Проверь Inventory.add.");
                    }
                    link.moved = 1;
                }
            }
        }
    }

    public synchronized int build(MachineType type, int x, int y) {
        if (type == null || x < 0 || x >= 12 || y < 0 || y >= 7) throw bad("Выбери клетку внутри участка");
        if (machines.size() >= 20) throw bad("На учебном участке помещается 20 зданий");
        if (machines.stream().anyMatch(m -> m.getX() == x && m.getY() == y)) throw bad("Клетка занята");
        if (budget < type.getPrice()) throw bad("Не хватает бюджета на строительство");
        Machine machine = new Machine(nextId++, type, x, y);
        machines.add(machine);
        budget -= type.getPrice();
        event("Построено здание №" + machine.getId() + ". Осталось средств: " + budget + ".");
        return machine.getId();
    }

    public synchronized void connect(int from, int to) {
        Machine source = machine(from), destination = machine(to);
        if (from == to) throw bad("Нельзя соединить здание с самим собой");
        if (links.stream().anyMatch(link -> link.from == from && link.to == to)) throw bad("Конвейер уже существует");
        Resource resource = outputResource(source.getType());
        if (resource == null || !accepts(destination.getType(), resource)) throw bad("Эта пара зданий не образует производственную цепочку");
        links.add(new Link(from, to));
        event("Конвейер: №" + from + " → №" + to + ".");
    }

    public synchronized void disconnect(int from, int to) {
        if (!links.removeIf(link -> link.from == from && link.to == to)) throw bad("Конвейер не найден");
    }

    public synchronized void toggle(int id) {
        Machine machine = machine(id);
        machine.setEnabled(!machine.isEnabled());
        if (!machine.isEnabled()) machine.setStatus(Status.PAUSED);
        else machine.setStatus(Status.READY);
    }

    public synchronized void removeMachine(int id) {
        Machine machine = machine(id);
        if (machine.getInput().total() + machine.getOutput().total() > 0 || machine.getProgress() > 0)
            throw bad("Сначала опустоши здание и заверши текущую партию");
        if (id <= 3) throw bad("Стартовые здания остаются на участке");
        machines.remove(machine);
        links.removeIf(link -> link.from == id || link.to == id);
        budget += machine.getType().getPrice();
        event("Здание демонтировано, стоимость возвращена.");
    }

    public synchronized void supply(int id) {
        if (!scenario.equals("sandbox")) throw bad("В контракте нет учебных поставок");
        Machine machine = machine(id);
        if (machine.getType() != MachineType.SMELTER && machine.getType() != MachineType.WAREHOUSE)
            throw bad("Поставка руды доступна плавильне или складу");
        boolean added = machine.getInput().add(Resource.ORE, 3);
        event(added ? "Учебная поставка: +3 руды в здание №" + id + "."
                : "Поставка отклонена: склад заполнен или Inventory.add пока не реализован.");
    }

    public synchronized boolean submit(int id) {
        Machine warehouse = machine(id);
        if (id != 3 || warehouse.getType() != MachineType.WAREHOUSE) throw bad("Заказ сдаётся со стартового склада №3");
        boolean accepted = contract.submit(warehouse.getInput(), tick);
        if (accepted) { running = false; event("Контракт выполнен! Город получил 40 железных пластин."); }
        else event("Заказ не принят. Проверь запас пластин, срок и реализацию Contract.submit.");
        return accepted;
    }

    public synchronized FactoryView snapshot() {
        List<MachineView> buildings = machines.stream().map(m -> {
            Recipe recipe = recipes.forMachine(m.getType());
            return new MachineView(m.getId(), m.getType(), m.getX(), m.getY(), m.isEnabled(), m.getStatus(),
                    m.getProgress(), recipe == null ? 1 : recipe.getDuration(), m.getProduced(),
                    m.getInput().snapshot(), m.getOutput().snapshot(), m.getInput().getCapacity(),
                    m.getOutput().getCapacity(), recipe == null ? null : new RecipeView(recipe.getInput(),
                    recipe.getInputAmount(), recipe.getOutput(), recipe.getOutputAmount(), recipe.getDuration()));
        }).toList();
        return new FactoryView(tick, running, speed, budget, mode, scenario, error,
                new ContractView(contract.getTarget(), contract.getDeadline(), contract.isComplete()), buildings,
                links.stream().map(l -> new LinkView(l.from, l.to, l.moved)).toList(), List.copyOf(events));
    }

    private Machine machine(int id) {
        return machines.stream().filter(machine -> machine.getId() == id).findFirst()
                .orElseThrow(() -> bad("Здание не найдено"));
    }

    private static Resource outputResource(MachineType type) {
        return switch (type) { case MINE -> Resource.ORE; case SMELTER -> Resource.INGOT;
            case PRESS -> Resource.PLATE; case WAREHOUSE -> null; };
    }

    private static boolean accepts(MachineType type, Resource resource) {
        return type == MachineType.WAREHOUSE || type == MachineType.SMELTER && resource == Resource.ORE
                || type == MachineType.PRESS && resource == Resource.INGOT;
    }

    private void event(String text) {
        events.addFirst("[" + tick + "] " + text);
        while (events.size() > 8) events.removeLast();
    }

    private static IllegalArgumentException bad(String message) { return new IllegalArgumentException(message); }

    private static final class Link {
        private final int from, to;
        private int moved;
        private Link(int from, int to) { this.from = from; this.to = to; }
    }

    public record FactoryView(int tick, boolean running, int speed, int budget, String mode, String scenario,
                              String error, ContractView contract, List<MachineView> machines,
                              List<LinkView> links, List<String> events) {}
    public record ContractView(int target, int deadline, boolean complete) {}
    public record RecipeView(Resource input, int inputAmount, Resource output, int outputAmount, int duration) {}
    public record MachineView(int id, MachineType type, int x, int y, boolean enabled, Status status,
                              int progress, int duration, int produced, Map<Resource, Integer> input,
                              Map<Resource, Integer> output, int inputCapacity, int outputCapacity, RecipeView recipe) {}
    public record LinkView(int from, int to, int moved) {}
}
