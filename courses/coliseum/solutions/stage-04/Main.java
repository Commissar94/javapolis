import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int maxHealth = 100;
        int heroHealth = maxHealth;
        int heroArmor = 2;
        int enemyHealth = 60;
        int enemyArmor = 3;
        int potions = 2;
        int turns = 0;
        boolean quit = false;

        while (heroHealth > 0 && enemyHealth > 0) {
            System.out.println("Герой: " + heroHealth + ", противник: " + enemyHealth
                    + ", зелий: " + potions);
            System.out.println("1 — атака, 2 — осторожный удар, 3 — лечение, 0 — выход");
            if (!scanner.hasNextLine()) {
                quit = true;
                break;
            }
            String action = scanner.nextLine().trim();
            boolean guarding = false;
            int attack = 0;

            if (action.equals("0")) {
                quit = true;
                break;
            } else if (action.equals("1")) {
                attack = 12 + (int) (Math.random() * 7);
            } else if (action.equals("2")) {
                attack = 6;
                guarding = true;
            } else if (action.equals("3")) {
                if (potions == 0 || heroHealth == maxHealth) {
                    System.out.println("Лечение недоступно. Ход не потрачен.");
                    continue;
                }
                heroHealth = Math.min(maxHealth, heroHealth + 20);
                potions--;
                System.out.println("Здоровье после лечения: " + heroHealth);
            } else {
                System.out.println("Нет такой команды. Ход не потрачен.");
                continue;
            }

            turns++;
            if (attack > 0) {
                int damage = Math.max(1, attack - enemyArmor);
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
            int enemyAttack = 8 + (int) (Math.random() * 5);
            int enemyDamage = Math.max(1, enemyAttack - defense);
            heroHealth = Math.max(0, heroHealth - enemyDamage);
            System.out.println("Урон противника: " + enemyDamage);
        }

        if (quit) {
            System.out.println("Выход из боя.");
        } else if (heroHealth == 0) {
            System.out.println("Поражение.");
        } else {
            System.out.println("Победа!");
        }
        System.out.println("Ходов: " + turns);
        System.out.println("Итоговое здоровье: " + heroHealth);
        System.out.println("Осталось зелий: " + potions);
        scanner.close();
    }
}
