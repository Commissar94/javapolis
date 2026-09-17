import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Как зовут гладиатора?");
        if (!scanner.hasNextLine()) {
            System.out.println("Ввод завершён. Турнир не начат.");
            scanner.close();
            return;
        }
        String heroName = scanner.nextLine().trim();
        if (heroName.isEmpty()) {
            heroName = "Спартак";
        }

        final int MAX_HEALTH = 100;
        final int TOTAL_FIGHTS = 3;
        int heroHealth = MAX_HEALTH;
        int heroArmor = 2;
        int potions = 2;
        int wins = 0;
        int turns = 0;
        boolean quit = false;

        for (int fight = 1; fight <= TOTAL_FIGHTS; fight++) {
            int enemyHealth = 30 + fight * 10;
            int enemyArmor = fight;
            System.out.println("Бой: " + fight);

            while (heroHealth > 0 && enemyHealth > 0) {
                System.out.println("Герой: " + heroHealth + ", противник: " + enemyHealth
                        + ", зелий: " + potions);
                String action = readAction(scanner);
                boolean guarding = false;
                int attack = 0;

                if (action.equals("0")) {
                    quit = true;
                    break;
                } else if (action.equals("1")) {
                    attack = randomBetween(12, 18);
                } else if (action.equals("2")) {
                    attack = 6;
                    guarding = true;
                } else if (action.equals("3")) {
                    if (potions == 0 || heroHealth == MAX_HEALTH) {
                        System.out.println("Лечение недоступно. Ход не потрачен.");
                        continue;
                    }
                    heroHealth = restoreHealth(heroHealth, 20, MAX_HEALTH);
                    potions--;
                    System.out.println("Здоровье после лечения: " + heroHealth);
                }

                turns++;
                if (attack > 0) {
                    int damage = damageAfterArmor(attack, enemyArmor);
                    enemyHealth = Math.max(0, enemyHealth - damage);
                    System.out.println("Урон героя: " + damage);
                    System.out.println("Здоровье противника: " + enemyHealth);
                }
                if (enemyHealth == 0) {
                    break;
                }

                int defense = heroArmor;
                if (guarding) {
                    defense = defense + 6;
                }
                int enemyDamage = damageAfterArmor(randomBetween(8, 12), defense);
                heroHealth = Math.max(0, heroHealth - enemyDamage);
                System.out.println("Урон противника: " + enemyDamage);
            }

            if (quit || heroHealth == 0) {
                break;
            }
            wins++;
            System.out.println("Победа в бою: " + fight);
            if (fight < TOTAL_FIGHTS) {
                heroHealth = restoreHealth(heroHealth, 25, MAX_HEALTH);
                System.out.println("Здоровье после отдыха: " + heroHealth);
            }
        }

        if (quit) {
            System.out.println("Выход из турнира.");
        } else if (wins == TOTAL_FIGHTS) {
            System.out.println(heroName + " — чемпион Колизея!");
        } else {
            System.out.println("Поражение. Арена подождёт реванша.");
        }
        System.out.println("Побед: " + wins);
        System.out.println("Ходов: " + turns);
        System.out.println("Итоговое здоровье: " + heroHealth);
        System.out.println("Осталось зелий: " + potions);
        scanner.close();
    }

    static int damageAfterArmor(int attack, int armor) {
        return Math.max(1, attack - armor);
    }

    // Контракт курса: небольшие неотрицательные границы, min <= max.
    static int randomBetween(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    // Контракт курса: 0 <= health <= maxHealth, amount >= 0; небольшие числа.
    static int restoreHealth(int health, int amount, int maxHealth) {
        return Math.min(maxHealth, health + amount);
    }

    static String readAction(Scanner scanner) {
        while (true) {
            System.out.println("1 — атака, 2 — осторожный удар, 3 — лечение, 0 — выход");
            if (!scanner.hasNextLine()) {
                return "0";
            }
            String action = scanner.nextLine().trim();
            if (action.equals("0") || action.equals("1")
                    || action.equals("2") || action.equals("3")) {
                return action;
            }
            System.out.println("Нет такой команды. Ход не потрачен.");
        }
    }
}
