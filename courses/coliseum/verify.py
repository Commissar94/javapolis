#!/usr/bin/env python3
"""Validate the published lessons and execute their Java examples (JDK 17+)."""
import json
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
from urllib.parse import unquote

COURSE = Path(__file__).resolve().parent
ROOT = COURSE.parents[1]
TOPICS = ROOT / 'src/main/resources/topics/Колизей'
META = json.loads((COURSE / 'course.json').read_text(encoding='utf-8'))
JAVA = shutil.which('java')
JAVAC = shutil.which('javac')
COMPILER = [JAVAC] if JAVAC else [JAVA or 'java', '-m', 'jdk.compiler/com.sun.tools.javac.Main']
FENCE = re.compile(r'^```java\n(.*?)^```\s*$', re.M | re.S)
checks = 0


def require(condition, message):
    global checks
    if not condition:
        raise AssertionError(message)
    checks += 1


def command(args, *, stdin='', timeout=30):
    result = subprocess.run(args, input=stdin, text=True, encoding='utf-8',
                            capture_output=True, timeout=timeout)
    if result.returncode:
        raise AssertionError(f'Command failed: {args}\n{result.stdout}\n{result.stderr}')
    return result.stdout.replace('\r\n', '\n').strip()


def compile_source(base, name, source):
    path = base / name
    path.mkdir()
    (path / 'Main.java').write_text(source, encoding='utf-8')
    command(COMPILER + ['--release', '17', '-encoding', 'UTF-8', '-d', str(path), str(path / 'Main.java')])
    return path


def run(path, stdin=''):
    return command([JAVA, '-Dfile.encoding=UTF-8', '-cp', str(path), 'Main'], stdin=stdin, timeout=5)


def value(output, label):
    found = re.findall(r'^' + re.escape(label) + r': (\d+)$', output, re.M)
    require(bool(found), f'Missing {label}:\n{output}')
    return int(found[-1])


def contains(output, *lines):
    for line in lines:
        require(line in output.splitlines(), f'Missing line {line!r}:\n{output}')


def validate_lessons(base):
    require([c['number'] for c in META['chapters']] == list(range(1, 26)), 'Chapter sequence')
    sources = []
    examples = {}
    for chapter in META['chapters']:
        number = chapter['number']
        file = TOPICS / chapter['filename']
        text = file.read_text(encoding='utf-8')
        require(text.startswith('# '), f'Missing heading: {file}')
        require(text.count('```') % 2 == 0, f'Unclosed code block: {file}')
        for target in re.findall(r'\]\((/university/[^)]+)\)', text):
            destination = ROOT / 'src/main/resources/topics' / (unquote(target.removeprefix('/university/')) + '.md')
            require(destination.is_file(), f'Broken navigation: {target}')
        for target in re.findall(r'!\[[^\]]*\]\(([^)]+)\)', text):
            require((file.parent / target).is_file() or (ROOT / 'src/main/resources/images' / target).is_file(),
                    f'Broken image: {target}')
        blocks = list(FENCE.finditer(text))
        if chapter['kind'] == 'practice':
            solution = (COURSE / f'solutions/stage-{chapter["stage"]:02}/Main.java').read_text(encoding='utf-8').strip()
            require(any(b.group(1).strip() == solution for b in blocks), f'Stale embedded solution: {file}')
            require(text.count('{collapsible="true"}') == 2, f'Hint and solution required: {file}')
        for index, block in enumerate(blocks, 1):
            code = block.group(1)
            name = f'Example{number:02}_{index:02}'
            # One deliberately invalid illustration: local variable used before assignment.
            if (number, index) == (4, 2):
                bad = base / 'Invalid.java'
                bad.write_text('class Invalid { public static void main(String[] args) {\n' + code + '\n}}', encoding='utf-8')
                result = subprocess.run(COMPILER + ['-encoding', 'UTF-8', str(bad)], capture_output=True, text=True)
                require(result.returncode != 0, 'The documented uninitialized-variable example must fail')
                continue
            if 'public class Main' in code:
                source = code.replace('public class Main', 'public class ' + name)
            elif re.match(r'\s*static\s+', code):
                source = f'public class {name} {{\n{code}\n}}'
            else:
                source = f'public class {name} {{ public static void main(String[] args) {{\n{code}\n}}}}'
            path = base / (name + '.java')
            path.write_text(source, encoding='utf-8')
            sources.append(str(path))
            examples[(number, index)] = (name, 'public static void main' in source)
    command(COMPILER + ['--release', '17', '-encoding', 'UTF-8', '-d', str(base)] + sources)
    # Verify the concrete outcomes used to teach the language, not just compilation.
    expected = {
        (1, 1): 'Арена открыта!', (2, 1): '100\n100\nC\nСпартак',
        (4, 1): '100\n80', (4, 3): '100', (4, 4): '100\n70',
        (5, 1): 'Гладиатор: Спартак\nЗдоровье: 100\nАрена открыта!',
        (5, 2): 'Урон: 105\nУрон: 15',
        (5, 3): 'Гладиатор по прозвищу "Компилятор"\nПервый бой\nВторой бой',
        (7, 1): '87', (7, 2): '3\n3.4\n3.4', (8, 1): 'true',
        (8, 2): 'true\nfalse\nfalse\ntrue', (9, 1): '1',
        (9, 2): 'Противник повержен.', (9, 3): 'Пора подумать о лечении.', (9, 4): '0',
        (10, 1): '5\n100',
        (12, 1): 'Тренировочный удар: 1\nТренировочный удар: 2\nТренировочный удар: 3',
        (14, 1): 'Противник повержен.\nБой завершён.', (14, 2): '1\n3',
        (17, 1): 'true\nfalse', (17, 2): 'true', (17, 3): 'Спартак',
        (18, 1): '1\n6\n100', (20, 1): 'Бой: 1\nБой: 2\nБой: 3',
        (20, 2): 'Бой 1, раунд 1\nБой 1, раунд 2\nБой 2, раунд 1\nБой 2, раунд 2',
        (21, 1): '13', (21, 2): 'Добро пожаловать на арену!',
        (22, 1): '50\n70', (22, 3): '100\n3',
    }
    for key, wanted in expected.items():
        name, runnable = examples[key]
        require(runnable, f'No main: {key}')
        output = command([JAVA, '-Dfile.encoding=UTF-8', '-cp', str(base), name])
        require(output == wanted, f'Wrong lesson output {key}: {output!r} != {wanted!r}')
    name, _ = examples[(16, 1)]
    output = command([JAVA, '-Dfile.encoding=UTF-8', '-cp', str(base), name], stdin='Спартак Младший\n')
    contains(output, 'На арену выходит: Спартак Младший')
    print(f'Compiled {len(sources)} Java blocks; verified {len(expected) + 1} lesson outputs and the intentional error.')


def validate_stages(base):
    code = {i: (COURSE / f'solutions/stage-{i:02}/Main.java').read_text(encoding='utf-8') for i in range(1, 6)}
    stages = {i: compile_source(base, f'stage-{i}', code[i]) for i in range(1, 6)}
    contains(run(stages[1]), 'Гладиатор: Спартак', 'Здоровье: 100', 'Атака: 18', 'Броня: 5',
             'Золото: 0', 'Ранг: C', 'Чемпион: false')
    contains(run(stages[2]), 'Спартак наносит урон: 13', 'Здоровье противника: 32',
             'Ответный урон: 9', 'Здоровье героя: 91')
    variants = [
        ('armor', 'int enemyArmor = 5;', 'int enemyArmor = 99;', ['Здоровье противника: 44']),
        ('exact', 'int enemyHealth = 45;', 'int enemyHealth = 13;', ['Здоровье противника: 0', 'Здоровье героя: 100']),
        ('overkill', 'int enemyHealth = 45;', 'int enemyHealth = 5;', ['Здоровье противника: 0', 'Здоровье героя: 100']),
        ('death', 'int heroHealth = 100;', 'int heroHealth = 4;', ['Здоровье героя: 0', 'Герой проиграл.']),
        ('hero-armor', 'int heroArmor = 3;', 'int heroArmor = 99;', ['Ответный урон: 1', 'Здоровье героя: 99']),
    ]
    for name, old, new, wanted in variants:
        output = run(compile_source(base, name, code[2].replace(old, new)))
        contains(output, *wanted)
        if name in ('exact', 'overkill'):
            require('Ответный урон:' not in output, name)
    fixed3 = code[3].replace('12 + (int) (Math.random() * 7)', '12').replace('8 + (int) (Math.random() * 5)', '8')
    output = run(compile_source(base, 'fixed3', fixed3))
    contains(output, 'Победа!', 'Раундов: 5', 'Итоговое здоровье: 76')
    require(output.count('Урон противника:') == 4, 'No retaliation after death in stage 3')
    for _ in range(8):
        output = run(stages[3])
        require(3 <= value(output, 'Раундов') <= 5, 'Random round bounds')
        require(60 <= value(output, 'Итоговое здоровье') <= 88, 'Random health bounds')

    fixed4 = code[4].replace('12 + (int) (Math.random() * 7)', '12').replace('8 + (int) (Math.random() * 5)', '8')
    stage4 = compile_source(base, 'fixed4', fixed4)
    for stdin in ('0\n', 'меч\n\n0\n', '3\n0\n', ''):
        contains(run(stage4, stdin), 'Выход из боя.', 'Ходов: 0', 'Итоговое здоровье: 100', 'Осталось зелий: 2')
    contains(run(stage4, ' 1 \n0\n'), 'Ходов: 1', 'Итоговое здоровье: 94')
    contains(run(stage4, '2\n0\n'), 'Здоровье противника: 57', 'Итоговое здоровье: 99')
    contains(run(stage4, '2\n1\n0\n'), 'Ходов: 2', 'Итоговое здоровье: 93')
    contains(run(stage4, '1\n3\n0\n'), 'Ходов: 2', 'Итоговое здоровье: 94', 'Осталось зелий: 1')
    output = run(stage4, '1\n3\n3\n3\n0\n')
    contains(output, 'Ходов: 3', 'Осталось зелий: 0', 'Лечение недоступно. Ход не потрачен.')
    require(output.count('Урон героя:') == 1, 'Healing must not damage the opponent')
    output = run(stage4, '1\n' * 20)
    contains(output, 'Победа!', 'Ходов: 7', 'Итоговое здоровье: 64')
    require(output.count('Урон противника:') == 6, 'No retaliation after death in stage 4')
    death4 = fixed4.replace('int heroHealth = maxHealth;', 'int heroHealth = 1;')
    contains(run(compile_source(base, 'death4', death4), '1\n'), 'Поражение.', 'Итоговое здоровье: 0')

    fixed5 = code[5].replace('return min + (int) (Math.random() * (max - min + 1));', 'return min;')
    stage5 = compile_source(base, 'fixed5', fixed5)
    contains(run(stage5, ''), 'Ввод завершён. Турнир не начат.')
    for stdin in ('Рокки\n0\n', 'Рокки\nмеч\n0\n', 'Рокки\n'):
        contains(run(stage5, stdin), 'Выход из турнира.', 'Побед: 0', 'Ходов: 0', 'Итоговое здоровье: 100')
    output = run(stage5, 'Рокки\n' + '1\n' * 4 + '0\n')
    contains(output, 'Побед: 1', 'Здоровье после отдыха: 100', 'Бой: 2')
    require('Бой: 3' not in output, 'Quit must terminate the outer loop')
    output = run(stage5, '   \n' + '1\n' * 16)
    contains(output, 'Спартак — чемпион Колизея!', 'Побед: 3', 'Ходов: 16',
             'Итоговое здоровье: 64', 'Осталось зелий: 2')
    require(output.count('Бой: ') == 3, 'Exactly three fights')
    require(output.count('Здоровье после отдыха:') == 2, 'No rest after final fight')
    require(output.count('Урон противника:') == 13, 'Exactly thirteen replies in the fixed tournament')
    output = run(stage5, 'Рокки\n1\n3\n' + '1\n' * 3 + '1\n3\n3\n0\n')
    contains(output, 'Бой: 2', 'Осталось зелий: 0', 'Лечение недоступно. Ход не потрачен.')
    death5 = fixed5.replace('int heroHealth = MAX_HEALTH;', 'int heroHealth = 1;')
    output = run(compile_source(base, 'death5', death5), 'Рокки\n1\n')
    contains(output, 'Поражение. Арена подождёт реванша.', 'Побед: 0', 'Итоговое здоровье: 0')
    require('Бой: 2' not in output, 'Death must terminate the outer loop')
    for _ in range(8):
        output = run(stages[5], 'Рокки\n' + '1\n' * 30)
        contains(output, 'Рокки — чемпион Колизея!', 'Побед: 3')
        require(0 < value(output, 'Итоговое здоровье') <= 100, 'Live-random tournament health')
        require('Бой: 4' not in output, 'No fourth fight')

    # Exercise the actual final-stage methods on boundary inputs.
    harness = stages[5] / 'ContractCheck.java'
    harness.write_text('''
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
''', encoding='utf-8')
    command(COMPILER + ['--release', '17', '-cp', str(stages[5]), '-d', str(stages[5]), str(harness)])
    require(command([JAVA, '-cp', str(stages[5]), 'ContractCheck']) == 'contracts OK', 'Method contracts')
    print('Verified all five stages: normal play, bounds, death, exit, EOF, invalid input, potions and tournament state.')


if __name__ == '__main__':
    if not JAVA:
        raise SystemExit('Install JDK 17 or newer (course target: JDK 21).')
    with tempfile.TemporaryDirectory(prefix='coliseum-check-') as temp:
        base = Path(temp)
        validate_lessons(base)
        validate_stages(base)
    print(f'OK: {checks} checks.')
