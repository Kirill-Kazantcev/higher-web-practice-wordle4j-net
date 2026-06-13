# Wordle4j-Net – Игра "Слова" с сетевой статистикой

Консольная реализация игры Wordle на Java с возможностью отправки результатов на HTTP-сервер и просмотра рейтинга игроков.


## Структура проекта
```markdown
src/ru/yandex/practicum/
├── game/
│ ├── Dictionary.java                       # интерфейс словаря
│ ├── WordMatcher.java                      # интерфейс констант
│ ├── WordMatcher.java                      # интерфейс анализатора совпадений
│ └── WordleWordMatcher.java                # реализация правил Wordle (+ ^ -)
├── net/
│ ├── StatisticsClient.java                 # интерфейс клиента статистики
│ └── HttpStatisticsClient.java             # HTTP реализация клиента
├── util/
│ ├── ConfigLoader.java                     # загрузка настроек из application.properties
│ └── JsonUtils.java                        # парсинг JSON-ответов сервера
├── exception/
│ ├── GameException.java
│ ├── InvalidWordFormatException.java
│ ├── WordNotFoundInDictionaryException.java
│ └── SystemException.java
├── Wordle.java                             # главный класс игры (клиент)
├── WordleGame.java                         # игровая логика
├── WordleDictionary.java                   # словарь (реализует Dictionary)
├── WordleDictionaryLoader.java             # загрузка словаря из файла
├── WordleServer.java                       # HTTP сервер статистики
└── WordleServerStatisticLoader.java        # сохранение/загрузка статистики в файл


application.properties                      # конфигурация приложения
words_ru.txt                                # файл словаря
stats.txt                                   # создаётся сервером для хранения статистики
wordle.log                                  # лог-файл игры
```

## Архитектурные решения

### Чистая архитектура и разделение ответственности

- Слабая связанность – зависимости передаются через конструкторы
- Интерфейсы Dictionary, WordMatcher, StatisticsClient для подмены реализаций
- Отдельные пакеты game, net, util, exception
- Глобальные константы вынесены в интерфейс GameConstants (WORD_LENGTH=5, MAX_STEPS=6)

### Dependency Injection (Внедрение зависимостей)

Зависимости передаются через конструктор, а не создаются внутри класса

### Работа с файлами и логирование

- try-with-resources для автоматического закрытия ресурсов
- Кодировка UTF-8 для поддержки русского языка
- Логирование через PrintWriter в файл wordle.log
- Ошибки перехватываются в main и записываются в лог

### Обработка исключений

Собственная иерархия:

- GameException (checked) – базовое игровое исключение
    - InvalidWordFormatException – неверная длина слова
    - WordNotFoundInDictionaryException – слова нет в словаре
- SystemException (unchecked) – системные ошибки (IO и т.д.)

### Алгоритм сравнения слов

Правильная обработка повторяющихся букв:

1. Подсчёт частоты букв в загаданном слове
2. Сначала отмечаются точные совпадения (+) и вычитаются из частоты
3. Затем ищутся буквы не на своих местах (^) среди оставшихся букв
4. Оставшиеся позиции отмечаются (-)

### Подсказки компьютера

- Учитывают все предыдущие ходы
- Автоматически исключают неподходящие слова
- Нет повторений – каждое подсказанное слово уникально

### HTTP-сервер статистики

- Эндпоинты: /result (POST), /top (GET), /stats (GET)
- Сохранение в текстовый файл stats.txt
- Формат: nickname:wins:losses:hintsUsed:totalSteps
- Топ-10 сортируется по количеству побед

### Клиент-серверное взаимодействие

Отправка результата после победы и получение топа игроков

---

## Технические детали

| Компонент     | Технология                                |
|:--------------|:------------------------------------------|
| HTTP-сервер   | com.sun.net.httpserver.HttpServer         |
| HTTP-клиент   | java.net.http.HttpClient (Java 11+)       |
| Работа с JSON | ручной парсинг (без внешних зависимостей) |
| Сборка        | ручная компиляция / IDEA                  |
| Тестирование  | JUnit 5 (jupiter)                         |
| Логирование   | PrintWriter + java.util.logging.Logger    |

---

## Обработка ошибок ввода

Многоуровневая валидация:

1. Проверка на пустую строку → выдача подсказки
2. Проверка длины → InvalidWordFormatException
3. Проверка наличия в словаре → WordNotFoundInDictionaryException
4. Проверка окончания игры → IllegalStateException

---

## Логирование

Разделение потоков вывода:

- System.out → для игрока (консоль)
- PrintWriter → для системы (wordle.log)
- Logger → для ошибок (java.util.logging)

---

## Производительность

- Словарь загружается один раз при старте
- Поиск подсказок – линейный перебор с ранним выходом
- Память – все слова хранятся в List<String>
- Сложность – O(N × M × K), где N – размер словаря, M – число ходов, K – длина слова (5)

---

## Тестирование

Покрыты основные сценарии:

- WordleGameTest – логика игры, ходы, победа/поражение
- WordleDictionaryTest – валидация слов
- WordleDictionaryLoaderTest – загрузка из файла
- WordleWordMatcherTest – алгоритм сравнения
- WordleServerStatisticLoaderTest – сохранение/загрузка статистики

## Версия
- Console version – 1.0.0