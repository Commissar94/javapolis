package javapolis.factory.runtime;

import javapolis.factory.model.MachineType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/factory")
public final class FactoryController {
    private final FactoryEngine engine;

    public FactoryController(FactoryEngine engine) { this.engine = engine; }

    @GetMapping
    public FactoryEngine.FactoryView state() { return engine.snapshot(); }

    @PostMapping(consumes = "application/json")
    public FactoryEngine.FactoryView command(@RequestBody Command command) {
        if (command.action() == null) throw new IllegalArgumentException("Команда не указана");
        switch (command.action()) {
            case "play" -> engine.setRunning(true);
            case "pause" -> engine.setRunning(false);
            case "step" -> { engine.setRunning(false); engine.advance(command.amount()); }
            case "speed" -> engine.setSpeed(command.amount());
            case "build" -> engine.build(command.type(), command.x(), command.y());
            case "connect" -> engine.connect(command.id(), command.target());
            case "disconnect" -> engine.disconnect(command.id(), command.target());
            case "toggle" -> engine.toggle(command.id());
            case "remove" -> engine.removeMachine(command.id());
            case "supply" -> engine.supply(command.id());
            case "submit" -> engine.submit(command.id());
            case "reset" -> engine.reset(command.scenario() == null ? "sandbox" : command.scenario());
            default -> throw new IllegalArgumentException("Неизвестная команда");
        }
        return engine.snapshot();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(IllegalArgumentException exception) {
        return Map.of("message", exception.getMessage());
    }

    public record Command(String action, int id, int target, int x, int y, int amount,
                          MachineType type, String scenario) {}
}
