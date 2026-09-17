public class Main {
    public static void main(String[] args) {
        String heroName = "Спартак";
        int heroHealth = 100;
        int heroAttack = 18;
        int heroArmor = 3;
        int enemyHealth = 45;
        int enemyAttack = 12;
        int enemyArmor = 5;

        int damage = heroAttack - enemyArmor;
        if (damage < 1) {
            damage = 1;
        }
        enemyHealth = enemyHealth - damage;
        if (enemyHealth < 0) {
            enemyHealth = 0;
        }
        System.out.println(heroName + " наносит урон: " + damage);
        System.out.println("Здоровье противника: " + enemyHealth);

        if (enemyHealth > 0) {
            int enemyDamage = enemyAttack - heroArmor;
            if (enemyDamage < 1) {
                enemyDamage = 1;
            }
            heroHealth = heroHealth - enemyDamage;
            if (heroHealth < 0) {
                heroHealth = 0;
            }
            System.out.println("Ответный урон: " + enemyDamage);
        } else {
            System.out.println("Противник повержен и не отвечает.");
        }
        System.out.println("Здоровье героя: " + heroHealth);
        if (heroHealth == 0) {
            System.out.println("Герой проиграл.");
        }
    }
}
