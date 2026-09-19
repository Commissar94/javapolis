package javapolis.factory;

import javapolis.factory.model.*;
import javapolis.factory.student.*;
import javapolis.factory.runtime.FactoryEngine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise") @Tag("stage-5")
class Stage5ContractTest {
    @Test void usesTheContractParametersInsteadOfHardcodedScenarioNumbers() {
        Inventory stock = new Inventory(10);
        assertTrue(stock.add(Resource.PLATE, 5));
        Contract contract = new Contract(3, 4);
        assertTrue(contract.submit(stock, 4));
        assertEquals(2, stock.count(Resource.PLATE));
        Contract expired = new Contract(2, 4);
        assertFalse(expired.submit(stock, 5));
        assertEquals(2, stock.count(Resource.PLATE));
    }
    @ParameterizedTest @ValueSource(ints = {0, 99, 100})
    void acceptsDeliveryOnOrBeforeDeadlineExactlyOnce(int tick) {
        Inventory stock = new Inventory(200);
        assertTrue(stock.add(Resource.PLATE, 50));
        Contract contract = new Contract(40, 100);
        assertTrue(contract.submit(stock, tick));
        assertTrue(contract.isComplete());
        assertEquals(10, stock.count(Resource.PLATE));
        assertFalse(contract.submit(stock, tick));
        assertEquals(10, stock.count(Resource.PLATE));
    }

    @ParameterizedTest @ValueSource(ints = {-1, 101, Integer.MAX_VALUE})
    void invalidTimeDoesNotWithdrawAnything(int tick) {
        Inventory stock = new Inventory(200);
        assertTrue(stock.add(Resource.PLATE, 40));
        Contract contract = new Contract(40, 100);
        assertFalse(contract.submit(stock, tick));
        assertEquals(40, stock.total());
        assertFalse(contract.isComplete());
    }

    @Test void shortageAndNullDoNotCompleteTheContract() {
        Contract contract = new Contract(40, 100);
        Inventory stock = new Inventory(200);
        assertTrue(stock.add(Resource.PLATE, 39));
        assertFalse(contract.submit(stock, 10));
        assertFalse(contract.submit(null, 10));
        assertEquals(39, stock.total());
        assertFalse(contract.isComplete());
    }

    @Test void aBalancedFactoryCanWinTheRealScenarioWithinTheBudget() {
        FactoryEngine engine = balancedFactory();
        engine.advance(100);
        assertEquals(100, engine.snapshot().tick());
        assertEquals(80, engine.snapshot().budget());
        assertNull(engine.snapshot().error());
        assertTrue(engine.submit(3), "Две плавильни и пресс должны успеть произвести 40 пластин");
        assertFalse(engine.submit(3));
        assertTrue(engine.snapshot().contract().complete());
    }

    @Test void oneSmelterIsTheBottleneckAndCannotWinIn100Ticks() {
        FactoryEngine engine = new FactoryEngine("solution");
        engine.reset("contract");
        int press = engine.build(MachineType.PRESS, 7, 3);
        engine.disconnect(2, 3);
        engine.connect(2, press);
        engine.connect(press, 3);
        engine.advance(100);
        assertFalse(engine.submit(3));
    }

    @Test void deterministicRunsAgreeAndMaterialIsConserved() {
        FactoryEngine first = balancedFactory(), second = balancedFactory();
        first.advance(100); second.advance(100);
        assertEquals(first.snapshot(), second.snapshot());
        int extracted = first.snapshot().machines().stream().filter(m -> m.type() == MachineType.MINE)
                .mapToInt(FactoryEngine.MachineView::produced).sum();
        int ore = 0, ingots = 0, plates = 0;
        for (var machine : first.snapshot().machines()) {
            ore += machine.input().getOrDefault(Resource.ORE, 0) + machine.output().getOrDefault(Resource.ORE, 0);
            ingots += machine.input().getOrDefault(Resource.INGOT, 0) + machine.output().getOrDefault(Resource.INGOT, 0);
            plates += machine.input().getOrDefault(Resource.PLATE, 0) + machine.output().getOrDefault(Resource.PLATE, 0);
        }
        assertEquals(extracted * 2, (ore + ingots) * 2 + plates * 3,
                "1 ore = 1 ingot; 3 ingots = 2 plates. Ingredients stay in inputs until completion.");
    }

    private FactoryEngine balancedFactory() {
        FactoryEngine engine = new FactoryEngine("solution");
        engine.reset("contract");
        int press = engine.build(MachineType.PRESS, 7, 3);
        int smelter = engine.build(MachineType.SMELTER, 4, 5);
        engine.disconnect(2, 3);
        engine.connect(2, press); engine.connect(press, 3);
        engine.connect(1, smelter); engine.connect(smelter, press);
        return engine;
    }
}
