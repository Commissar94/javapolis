# JUnit. Проверяем без гадания

На экране бегут ресурсы — приятно. Но по картинке трудно понять, верно ли обработан отрицательный запрос и не списывается ли заказ дважды. Для таких вопросов есть автоматические тесты.

JUnit Jupiter позволяет оформить сценарий как Java-метод с `@Test`. Maven запускает его и сообщает, какое ожидание не выполнилось. Зависимости уже настроены в шаблоне.

## Первая собственная проверка

Создай файл `src/test/java/javapolis/factory/MyInventoryTest.java`:

```java
package javapolis.factory;

import javapolis.factory.model.Resource;
import javapolis.factory.student.Inventory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyInventoryTest {
    @Test
    void refusesOverflowWithoutChangingStock() {
        Inventory stock = new Inventory(5);
        assertTrue(stock.add(Resource.ORE, 4));

        boolean accepted = stock.add(Resource.INGOT, 2);

        assertFalse(accepted);
        assertEquals(4, stock.count(Resource.ORE));
        assertEquals(0, stock.count(Resource.INGOT));
    }
}
```

Здесь три части: подготовить объект, выполнить действие, проверить результат. `assertTrue` ожидает `true`, `assertFalse` — `false`. В `assertEquals` первым передаём ожидаемое значение, вторым — фактическое. Это помогает прочитать отчёт: сколько ждали и сколько получили.

Запусти `./mvnw -Dtest=MyInventoryTest test` или `.\mvnw.cmd -Dtest=MyInventoryTest test`. Тест рассчитан на уже реализованный склад. `main` в тестовом классе не нужен.

## Как читать красный тест

Начни с названия метода и строки проверки. Если ожидалось 4, а получилось 6, спроси себя: склад принял запрещённую поставку или неверно посчитал прежнюю? Не меняй ожидаемое число только ради зелёного отчёта. Сначала сверяйся с правилами задачи.

Тест должен проверять наблюдаемое поведение. Ему не нужно знать, используется внутри склада `EnumMap` или другая карта. Это позволит улучшать реализацию, сохраняя договорённость.

Готовые проверки этапов отмечены тегами `exercise` и `stage-N`. Maven-профиль `stage-N` выбирает нужную группу. Профиль `all-stages` включает все задания, а обычный запуск без профиля — готовую оболочку и твои нетегированные тесты. Профиль `solution` проверяет эталон; он не подтверждает правильность твоего кода.

**Твоя маленькая задача:** добавь второй тест — выдача ровно всего запаса должна пройти успешно и оставить ноль. Затем временно сломай условие в `remove`, убедись, что тест покраснел, и верни исправление.

Аннотации, проверки и теги описаны в [JUnit 5 User Guide](https://docs.junit.org/5.12.2/user-guide/). Мы используем Jupiter API, который приходит с тестовой зависимостью Spring Boot.

---

[← Этап 4 Пресс](/university/%D0%97%D0%B0%D0%B2%D0%BE%D0%B4/12_%D0%AD%D1%82%D0%B0%D0%BF%204%20%D0%9F%D1%80%D0%B5%D1%81%D1%81) · [Границы и отрицательные сценарии →](/university/%D0%97%D0%B0%D0%B2%D0%BE%D0%B4/14_%D0%93%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D1%8B%20%D0%B8%20%D0%BE%D1%82%D1%80%D0%B8%D1%86%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D1%8B%D0%B5%20%D1%81%D1%86%D0%B5%D0%BD%D0%B0%D1%80%D0%B8%D0%B8)
