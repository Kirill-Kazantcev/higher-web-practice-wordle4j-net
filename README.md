# Wordle4j-Net – Игра "Слова" с сетевой статистикой

Консольная реализация игры Wordle на Java с возможностью отправки результатов на HTTP-сервер и просмотра рейтинга игроков.


## Структура проекта
```markdown
src/ru/yandex/practicum/
│
├── client/                                                     # КЛИЕНТСКАЯ ЧАСТЬ (ИГРА)
│ ├── Wordle.java                                               # Главный класс клиента (меню, игровой цикл, консоль)
│ ├── WordleConstants.java                                      # Константы клиента (пути, порты, эндпоинты)
│ ├── WordleDictionary.java                                     # Словарь (реализует интерфейс Dictionary)
│ ├── WordleDictionaryLoader.java                               # Загрузчик словаря из файла (UTF-8, фильтрация)
│ └── WordleGame.java                                           # Игровая логика (состояние, ходы, подсказки)
│
├── server/                                                     # СЕРВЕРНАЯ ЧАСТЬ (СТАТИСТИКА)
│ ├── WordleServer.java                                         # HTTP-сервер (эндпоинты: /result, /top, /stats)
│ └── WordleServerStatisticLoader.java                          # Загрузка/сохранение статистики (JSON через Gson)
│
├── net/                                                        # СЕТЕВЫЕ КОМПОНЕНТЫ
│ ├── StatisticsClient.java                                     # Интерфейс клиента статистики
│ └── HttpStatisticsClient.java                                 # HTTP реализация клиента (Java 11+ HttpClient)
│
├── game/                                                       # ИГРОВЫЕ ИНТЕРФЕЙСЫ
│ ├── GameConstants.java                                        # Глобальные константы (WORD_LENGTH=5, MAX_STEPS=6)
│ ├── Dictionary.java                                           # Интерфейс словаря
│ ├── WordMatcher.java                                          # Интерфейс для сравнения слов (+ ^ -)
│ └── WordleWordMatcher.java                                    # Реализация правил Wordle с обработкой повторов
│
├── exception/                                                  # ИЕРАРХИЯ ИСКЛЮЧЕНИЙ
│ ├── game/                                                     # Игровые исключения (checked)
│ │ ├── GameException.java                                      # Базовое игровое исключение
│ │ ├── InvalidAlphabetException.java                           # Недопустимые символы (не русские буквы)
│ │ ├── InvalidWordLengthException.java                         # Неверная длина слова (не 5 букв)
│ │ └── WordNotFoundInDictionaryException.java                  # Слово отсутствует в словаре
│ ├── io/                                                       # Файловые исключения (checked)
│ │ ├── DictionaryEmptyException.java                           # Словарь не содержит подходящих слов
│ │ ├── DictionaryFileNotFoundException.java                    # Файл словаря не найден
│ │ └── LogFileCreationException.java                           # Ошибка создания лог-файла
│ └── system/                                                   # Системные исключения (unchecked)
│   ├── GameStateException.java                                 # Ошибка состояния игры (ход после завершения)
│   └── SystemException.java                                    # Ошибки ввода-вывода, некорректное состояние
│
└── util/                                                       # УТИЛИТНЫЕ КЛАССЫ
    ├── ConfigLoader.java                                       # Загрузка application.properties
    ├── ClientSettings.java                                     # Настройки клиента (с значениями по умолчанию)
    ├── JsonUtils.java                                          # JSON для клиента (парсинг топа и статистики)
    └── ServerJsonUtils.java                                    # JSON для сервера (извлечение значений)



tests/ru/yandex/practicum/
│
├── client/                                                     # ТЕСТЫ КЛИЕНТСКОЙ ЧАСТИ
│   ├── WordleTest.java                                         # Тесты главного класса (main method)
│   ├── WordleGameTest.java                                     # Тесты игровой логики (ходы, победа, поражение, подсказки)
│   ├── WordleDictionaryTest.java                               # Тесты словаря (валидация, нормализация, contains)
│   ├── WordleDictionaryLoaderTest.java                         # Тесты загрузчика (фильтрация, кодировка, обработка ё)
│   ├── WordleWordMatcherTest.java                              # Тесты алгоритма сравнения (+ ^ - с повторами)
│   └── WordleHttpClientTest.java                               # Тесты HTTP-клиента (отправка статистики, получение топа)
│
└── server/                                                     # ТЕСТЫ СЕРВЕРНОЙ ЧАСТИ
    ├── WordleServerTest.java                                   # Тесты HTTP-сервера (эндпоинты, JSON ответы)
    └── WordleServerStatisticLoaderTest.java                    # Тесты загрузчика статистики (чтение/запись, winRate, avgSteps)



application.properties                                          # Конфигурация приложения
statistics.json                                                 # Файл статистики (создаётся сервером, формат JSON)
words_ru.txt                                                    # Файл словаря (5-буквенные русские слова в UTF-8)
stats.txt                                                       # Файл статистики (создаётся сервером автоматически)
wordle.log                                                      # Лог-файл игры (создаётся клиентом автоматически)
wordle-server.log                                               # Лог-файл сервера (опционально)

```

## Архитектурные решения

### Чистая архитектура и разделение ответственности

- Слабая связанность – зависимости передаются через конструкторы
- Интерфейсы Dictionary, WordMatcher, StatisticsClient для подмены реализаций
- Отдельные пакеты:
    - `client/` – клиентская часть игры (логика, словарь, константы)
    - `server/` – HTTP-сервер статистики
    - `net/` – сетевые компоненты (HTTP-клиент)
    - `game/` – игровые интерфейсы и константы
    - `exception/` – иерархия исключений (game, io, system)
    - `util/` – утилитные классы (загрузка конфигурации, JSON парсинг)
- Глобальные константы вынесены в интерфейс GameConstants (WORD_LENGTH=5, MAX_STEPS=6)
- Настройки клиента вынесены в ClientSettings с загрузкой из application.properties

### Dependency Injection (Внедрение зависимостей)

Зависимости передаются через конструктор, а не создаются внутри класса:
- WordleGame получает Dictionary и WordMatcher
- WordleDictionaryLoader получает PrintWriter для логирования
- WordleServer получает PrintWriter для логирования
- HttpStatisticsClient использует WordleConstants через статические методы

### Работа с файлами и логирование

- try-with-resources для автоматического закрытия ресурсов
- Кодировка UTF-8 для поддержки русского языка
- Логирование через PrintWriter в файл wordle.log
- Ошибки перехватываются в main и записываются в лог
- Файл словаря читается с использованием FileReader и BufferedReader

### Обработка исключений

Собственная иерархия с разделением на три подпакета:

**Пакет exception/game/ (игровые исключения, checked):**
- GameException – базовое игровое исключение
- InvalidWordLengthException – неверная длина слова (не 5 букв)
- InvalidAlphabetException – недопустимые символы (не русские буквы)
- WordNotFoundInDictionaryException – слово отсутствует в словаре

**Пакет exception/io/ (файловые исключения, checked):**
- DictionaryEmptyException – словарь не содержит подходящих слов
- DictionaryFileNotFoundException – файл словаря не найден
- LogFileCreationException – ошибка создания лог-файла

**Пакет exception/system/ (системные исключения, unchecked):**
- SystemException – ошибки ввода-вывода, некорректное состояние
- GameStateException – попытка хода после завершения игры

### Алгоритм сравнения слов

Правильная обработка повторяющихся букв в WordleWordMatcher:

1. Подсчёт частоты букв в загаданном слове (HashMap)
2. Сначала отмечаются точные совпадения (+) и вычитаются из частоты
3. Затем ищутся буквы не на своих местах (^) среди оставшихся букв
4. Оставшиеся позиции отмечаются (-) 

### Подсказки компьютера

- Учитывают все предыдущие ходы через метод getPossibleWords()
- Автоматически исключают неподходящие слова
- Нет повторений – каждое подсказанное слово запоминается в suggestedHints
- Выбирается случайное слово из доступных кандидатов
- При запросе подсказки увеличивается счётчик hintsUsed

### HTTP-сервер статистики

- Эндпоинты: `/result` (POST), `/top` (GET), `/stats` (GET)
- Сохранение в JSON-файл `statistics.json` (через Gson)
- Формат: `{"nickname": {"wins": n, "losses": n, "hintsUsed": n, "totalSteps": n}}`
- Топ-10 сортируется по количеству побед
- Поддержка запроса статистики конкретного игрока
- Использование ExecutorService для обработки запросов
- Graceful shutdown через ShutdownHook

### Клиент-серверное взаимодействие

- Отправка результата после победы через HttpStatisticsClient
- Получение топа игроков после сохранения статистики
- Получение статистики конкретного игрока по запросу
- JSON формат обмена данными
- Таймаут соединения 5 секунд

---

## Технические детали

| Компонент     | Технология                                    |
|:--------------|:----------------------------------------------|
| HTTP-сервер   | com.sun.net.httpserver.HttpServer             |
| HTTP-клиент   | java.net.http.HttpClient (Java 11+)           |
| Работа с JSON | Gson 2.8.7 (сервер) + ручной парсинг (клиент) |
| Сборка        | ручная компиляция / IntelliJ IDEA             |
| Тестирование  | JUnit 5 (jupiter)                             |
| Логирование   | PrintWriter + java.util.logging.Logger        |

---

## Обработка ошибок ввода

Многоуровневая валидация:

1. Проверка на пустую строку → выдача подсказки
2. Проверка длины → InvalidWordLengthException
3. Проверка алфавита (только русские буквы) → InvalidAlphabetException
4. Проверка наличия в словаре → WordNotFoundInDictionaryException
5. Проверка окончания игры → GameStateException

---

## Логирование

Разделение потоков вывода:

- `System.out` – для игрока (консоль)
- `PrintWriter` – для системы (wordle.log, wordle-server.log)
- `Logger` – для ошибок (java.util.logging)

---

## Производительность

- Словарь загружается один раз при старте
- Поиск подсказок – линейный перебор с ранним выходом
- Память – все слова хранятся в `List<String>` и `Set<String>`
- Сложность – O(N × M × K), где N – размер словаря, M – число ходов, K – длина слова (5)
- HTTP-клиент использует пул соединений через HttpClient

---

## Тестирование

Покрыты основные сценарии:

- `WordleGameTest` – логика игры, ходы, победа/поражение, подсказки
- `WordleDictionaryTest` – валидация слов, нормализация, contains
- `WordleDictionaryLoaderTest` – загрузка из файла, фильтрация, кодировка
- `WordleWordMatcherTest` – алгоритм сравнения с повторяющимися буквами
- `WordleServerStatisticLoaderTest` – сохранение/загрузка статистики

---

## Версия

- Console version – 1.0.0