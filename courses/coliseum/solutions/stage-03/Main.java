public class Main {
    public static void main(String[] args) {
        int heroHealth = 100;
        int heroArmor = 2;
        int enemyHealth = 45;
        int enemyArmor = 3;
        int round = 0;

        while (heroHealth > 0 && enemyHealth > 0) {
            round++;
            System.out.println("Раунд: " + round);
            int attack = 12 + (int) (Math.random() * 7);
            int damage = attack - enemyArmor;
            if (damage < 1) {
                damage = 1;
            }
            enemyHealth = enemyHealth - damage;
            if (enemyHealth < 0) {
                enemyHealth = 0;
            }
            System.out.println("Урон героя: " + damage);
            System.out.println("Здоровье противника: " + enemyHealth);
            if (enemyHealth == 0) {
                break;
            }

            int enemyAttack = 8 + (int) (Math.random() * 5);
            int enemyDamage = enemyAttack - heroArmor;
            if (enemyDamage < 1) {
                enemyDamage = 1;
            }
            heroHealth = heroHealth - enemyDamage;
            if (heroHealth < 0) {
                heroHealth = 0;
            }
            System.out.println("Урон противника: " + enemyDamage);
            System.out.println("Здоровье героя: " + heroHealth);
        }

        if (heroHealth > 0) {
            System.out.println("Победа!");
        } else {
            System.out.println("Поражение.");
        }
        System.out.println("Раундов: " + round);
        System.out.println("Итоговое здоровье: " + heroHealth);
    }
}
