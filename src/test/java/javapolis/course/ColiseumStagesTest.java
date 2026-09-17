package javapolis.course;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static javapolis.course.CourseJava.*;
import static org.junit.jupiter.api.Assertions.*;

class ColiseumStagesTest {
    @TempDir
    static Path temporaryDirectory;

    private static CourseJava java;
    private static final Map<Integer, String> SOURCES = new HashMap<>();
    private static final Map<Integer, Path> STAGES = new HashMap<>();
    private static final Map<Integer, String> FIXED_SOURCES = new HashMap<>();
    private static final Map<Integer, Path> FIXED_STAGES = new HashMap<>();

    @BeforeAll
    static void compileSolutionsAndDeterministicCopies() throws Exception {
        java = new CourseJava(temporaryDirectory);
        for (int stage = 1; stage <= 5; stage++) {
            String source = java.solution(stage);
            SOURCES.put(stage, source);
            STAGES.put(stage, java.compile(source));
            if (stage >= 3) {
                // Only temporary copies change: students still get the published random game.
                String fixed = stage == 5
                        ? replace(source, "return min + (int) (Math.random() * (max - min + 1));", "return min;")
                        : replace(replace(source, "12 + (int) (Math.random() * 7)", "12"),
                                "8 + (int) (Math.random() * 5)", "8");
                FIXED_SOURCES.put(stage, fixed);
                FIXED_STAGES.put(stage, java.compile(fixed));
            }
        }
    }

    @Test
    void stageOnePrintsTheGladiatorPassport() throws Exception {
        containsLines(java.run(STAGES.get(1), ""), "Гладиатор: Спартак", "Здоровье: 100",
                "Атака: 18", "Броня: 5", "Золото: 0", "Ранг: C", "Чемпион: false");
    }

    @Test
    void stageTwoExchangesOneAttack() throws Exception {
        containsLines(java.run(STAGES.get(2), ""), "Спартак наносит урон: 13", "Здоровье противника: 32",
                "Ответный урон: 9", "Здоровье героя: 91");
    }

    @ParameterizedTest(name = "Stage 2: {0}")
    @MethodSource("singleAttackBoundaries")
    void stageTwoHandlesArmorDeathAndOverkill(String scenario, String before, String after,
                                             List<String> expected, boolean enemyDead) throws Exception {
        String output = java.run(java.compile(replace(SOURCES.get(2), before, after)), "");
        containsLines(output, expected.toArray(String[]::new));
        if (enemyDead) {
            assertEquals(0, countLines(output, "Ответный урон:"));
        }
    }

    static Stream<Arguments> singleAttackBoundaries() {
        return Stream.of(
                Arguments.of("enemy armor", "int enemyArmor = 5;", "int enemyArmor = 99;",
                        List.of("Здоровье противника: 44"), false),
                Arguments.of("exact damage", "int enemyHealth = 45;", "int enemyHealth = 13;",
                        List.of("Здоровье противника: 0", "Здоровье героя: 100"), true),
                Arguments.of("overkill", "int enemyHealth = 45;", "int enemyHealth = 5;",
                        List.of("Здоровье противника: 0", "Здоровье героя: 100"), true),
                Arguments.of("hero death", "int heroHealth = 100;", "int heroHealth = 4;",
                        List.of("Здоровье героя: 0", "Герой проиграл."), false),
                Arguments.of("hero armor", "int heroArmor = 3;", "int heroArmor = 99;",
                        List.of("Ответный урон: 1", "Здоровье героя: 99"), false));
    }

    @Test
    void stageThreeStopsBeforeADeadEnemyCanReply() throws Exception {
        String output = java.run(FIXED_STAGES.get(3), "");
        containsLines(output, "Победа!", "Раундов: 5", "Итоговое здоровье: 76");
        assertEquals(4, countLines(output, "Урон противника:"));
    }

    @RepeatedTest(8)
    void stageThreeRandomFightStaysWithinItsBounds() throws Exception {
        String output = java.run(STAGES.get(3), "");
        int rounds = value(output, "Раундов");
        int health = value(output, "Итоговое здоровье");
        assertTrue(rounds >= 3 && rounds <= 5, output);
        assertTrue(health >= 60 && health <= 88, output);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0\n", "меч\n\n0\n", "3\n0\n", ""})
    void stageFourDoesNotSpendATurnOnExitInvalidInputOrUnavailableHealing(String input) throws Exception {
        containsLines(java.run(FIXED_STAGES.get(4), input), "Выход из боя.", "Ходов: 0",
                "Итоговое здоровье: 100", "Осталось зелий: 2");
    }

    @ParameterizedTest(name = "Stage 4: {0}")
    @MethodSource("playerActions")
    void stageFourResolvesPlayerActions(String scenario, String input, List<String> expected) throws Exception {
        containsLines(java.run(FIXED_STAGES.get(4), input), expected.toArray(String[]::new));
    }

    static Stream<Arguments> playerActions() {
        return Stream.of(
                Arguments.of("trim command", " 1 \n0\n", List.of("Ходов: 1", "Итоговое здоровье: 94")),
                Arguments.of("guard", "2\n0\n", List.of("Здоровье противника: 57", "Итоговое здоровье: 99")),
                Arguments.of("guard resets", "2\n1\n0\n", List.of("Ходов: 2", "Итоговое здоровье: 93")),
                Arguments.of("heal then receive reply", "1\n3\n0\n",
                        List.of("Ходов: 2", "Итоговое здоровье: 94", "Осталось зелий: 1")));
    }

    @Test
    void stageFourCannotHealWithoutPotionsAndHealingDoesNotAttack() throws Exception {
        String output = java.run(FIXED_STAGES.get(4), "1\n3\n3\n3\n0\n");
        containsLines(output, "Ходов: 3", "Осталось зелий: 0", "Лечение недоступно. Ход не потрачен.");
        assertEquals(1, countLines(output, "Урон героя:"));
    }

    @Test
    void stageFourEndsAfterVictoryWithoutAnotherEnemyAttack() throws Exception {
        String output = java.run(FIXED_STAGES.get(4), "1\n".repeat(20));
        containsLines(output, "Победа!", "Ходов: 7", "Итоговое здоровье: 64");
        assertEquals(6, countLines(output, "Урон противника:"));
    }

    @Test
    void stageFourEndsAfterHeroDeath() throws Exception {
        String source = replace(FIXED_SOURCES.get(4), "int heroHealth = maxHealth;", "int heroHealth = 1;");
        containsLines(java.run(java.compile(source), "1\n"), "Поражение.", "Итоговое здоровье: 0");
    }

    @Test
    void tournamentDoesNotStartWithoutANameLine() throws Exception {
        containsLines(java.run(FIXED_STAGES.get(5), ""), "Ввод завершён. Турнир не начат.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Рокки\n0\n", "Рокки\nмеч\n0\n", "Рокки\n"})
    void tournamentHandlesQuitInvalidInputAndEndOfInput(String input) throws Exception {
        containsLines(java.run(FIXED_STAGES.get(5), input), "Выход из турнира.", "Побед: 0",
                "Ходов: 0", "Итоговое здоровье: 100");
    }

    @Test
    void quittingSecondFightExitsTheWholeTournament() throws Exception {
        String output = java.run(FIXED_STAGES.get(5), "Рокки\n" + "1\n".repeat(4) + "0\n");
        containsLines(output, "Побед: 1", "Здоровье после отдыха: 100", "Бой: 2");
        assertFalse(output.contains("Бой: 3"), output);
    }

    @Test
    void tournamentHasThreeFightsTwoRestsAndADefaultName() throws Exception {
        String output = java.run(FIXED_STAGES.get(5), "   \n" + "1\n".repeat(16));
        containsLines(output, "Спартак — чемпион Колизея!", "Побед: 3", "Ходов: 16",
                "Итоговое здоровье: 64", "Осталось зелий: 2");
        assertEquals(3, countLines(output, "Бой: "));
        assertEquals(2, countLines(output, "Здоровье после отдыха:"));
        assertEquals(13, countLines(output, "Урон противника:"));
    }

    @Test
    void tournamentSharesPotionsAcrossFights() throws Exception {
        String output = java.run(FIXED_STAGES.get(5), "Рокки\n1\n3\n" + "1\n".repeat(3) + "1\n3\n3\n0\n");
        containsLines(output, "Бой: 2", "Осталось зелий: 0", "Лечение недоступно. Ход не потрачен.");
    }

    @Test
    void heroDeathExitsTheWholeTournament() throws Exception {
        String source = replace(FIXED_SOURCES.get(5), "int heroHealth = MAX_HEALTH;", "int heroHealth = 1;");
        String output = java.run(java.compile(source), "Рокки\n1\n");
        containsLines(output, "Поражение. Арена подождёт реванша.", "Побед: 0", "Итоговое здоровье: 0");
        assertFalse(output.contains("Бой: 2"), output);
    }

    @RepeatedTest(8)
    void publishedRandomTournamentIsWinnableAndStopsAfterThreeFights() throws Exception {
        String output = java.run(STAGES.get(5), "Рокки\n" + "1\n".repeat(30));
        containsLines(output, "Рокки — чемпион Колизея!", "Побед: 3");
        int health = value(output, "Итоговое здоровье");
        assertTrue(health > 0 && health <= 100, output);
        assertFalse(output.contains("Бой: 4"), output);
    }

    @Test
    void finalStageMethodsRespectDamageHealingAndRandomBounds() throws Exception {
        // Compile alongside Main so the probe can call the real package-private methods.
        String probe = """
                public class ContractCheck {
                    public static void main(String[] args) {
                        if (Main.damageAfterArmor(18, 5) != 13 || Main.damageAfterArmor(2, 9) != 1)
                            throw new AssertionError("damage");
                        if (Main.restoreHealth(95, 20, 100) != 100 || Main.restoreHealth(50, 20, 100) != 70
                                || Main.restoreHealth(100, 0, 100) != 100)
                            throw new AssertionError("healing");
                        for (int i = 0; i < 10000; i++) {
                            int attack = Main.randomBetween(12, 18);
                            if (attack < 12 || attack > 18 || Main.randomBetween(5, 5) != 5)
                                throw new AssertionError("random bounds");
                        }
                        System.out.println("contracts OK");
                    }
                }
                """;
        Path directory = java.compile(Map.of("Main", SOURCES.get(5), "ContractCheck", probe));
        assertEquals("contracts OK", java.run(directory, "ContractCheck", ""));
    }
}
