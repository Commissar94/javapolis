# Коллекции в Java

Коллекции - это структуры данных, которые позволяют хранить и управлять группами объектов. Они являются одной из самых важных частей Java.

## Что такое коллекции?

Коллекции - это контейнеры для хранения объектов. В отличие от массивов, коллекции могут динамически изменять свой размер и предоставляют множество полезных методов для работы с данными.

## Основные типы коллекций

### List (Список)
Список - это упорядоченная коллекция, которая может содержать дублирующиеся элементы.

```java
import java.util.ArrayList;
import java.util.List;

List<String> names = new ArrayList<>();
names.add("Анна");
names.add("Борис");
names.add("Вера");

// Доступ по индексу
String firstName = names.get(0); // "Анна"

// Размер списка
int size = names.size(); // 3

// Проверка на существование элемента
boolean hasAnna = names.contains("Анна"); // true
```

### Set (Множество)
Множество - это коллекция, которая не может содержать дублирующиеся элементы.

```java
import java.util.HashSet;
import java.util.Set;

Set<Integer> numbers = new HashSet<>();
numbers.add(1);
numbers.add(2);
numbers.add(2); // Дубликат не добавится
numbers.add(3);

System.out.println(numbers); // [1, 2, 3]
```

### Map (Карта)
Карта - это коллекция пар "ключ-значение", где каждый ключ уникален.

```java
import java.util.HashMap;
import java.util.Map;

Map<String, Integer> ages = new HashMap<>();
ages.put("Анна", 25);
ages.put("Борис", 30);
ages.put("Вера", 28);

// Получение значения по ключу
int annaAge = ages.get("Анна"); // 25

// Проверка существования ключа
boolean hasBoris = ages.containsKey("Борис"); // true
```

## Итерация по коллекциям

### For-each цикл
```java
List<String> fruits = List.of("яблоко", "банан", "апельсин");

for (String fruit : fruits) {
    System.out.println(fruit);
}
```

### Stream API (Java 8+)
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

// Фильтрация четных чисел
numbers.stream()
    .filter(n -> n % 2 == 0)
    .forEach(System.out::println);

// Сумма всех чисел
int sum = numbers.stream()
    .mapToInt(Integer::intValue)
    .sum();
```

## Практический пример

```java
import java.util.*;

public class CollectionsExample {
    public static void main(String[] args) {
        // Создаем список студентов
        List<Student> students = new ArrayList<>();
        
        students.add(new Student("Анна", 85));
        students.add(new Student("Борис", 92));
        students.add(new Student("Вера", 78));
        students.add(new Student("Григорий", 95));
        
        // Сортируем по оценкам
        students.sort((s1, s2) -> Integer.compare(s2.getGrade(), s1.getGrade()));
        
        // Выводим топ-3 студента
        System.out.println("Топ-3 студента:");
        students.stream()
            .limit(3)
            .forEach(s -> System.out.println(s.getName() + ": " + s.getGrade()));
        
        // Средняя оценка
        double averageGrade = students.stream()
            .mapToInt(Student::getGrade)
            .average()
            .orElse(0.0);
            
        System.out.println("Средняя оценка: " + averageGrade);
    }
}

class Student {
    private String name;
    private int grade;
    
    public Student(String name, int grade) {
        this.name = name;
        this.grade = grade;
    }
    
    public String getName() { return name; }
    public int getGrade() { return grade; }
}
```

{style="tip"}
Используйте коллекции вместо массивов, когда вам нужна гибкость в работе с данными!
{/style}

## Выбор правильной коллекции

- **ArrayList** - для частого доступа по индексу
- **LinkedList** - для частого добавления/удаления элементов
- **HashSet** - для быстрого поиска уникальных элементов
- **TreeSet** - для отсортированных уникальных элементов
- **HashMap** - для быстрого поиска по ключу
- **TreeMap** - для отсортированных пар ключ-значение

{style="warning"}
Не забывайте импортировать нужные классы коллекций в начале файла!
{/style}

## Заключение

Коллекции делают Java мощным языком для работы с данными. Изучив их, вы сможете создавать эффективные и гибкие программы.

{collapsible="true"}
Дополнительные ресурсы
{/collapsible}
- [Официальная документация Java Collections](https://docs.oracle.com/javase/tutorial/collections/)
- [Java Collections Framework](https://docs.oracle.com/javase/8/docs/api/java/util/package-summary.html)
- Практические задания по работе с коллекциями
{/collapsible}
