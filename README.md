# 🛒 market-platform
Мультимодульный учебный проект, реализующий упрощённую модель тороговой площадки с корзиной, заказами и внешним платёжным сервисом (wallet-app).

---
## 🧩 Архитектура
Проект состоит из двух основных сервисов:
### 1. **market-app**
Основное веб‑приложение маркетплейса.

Функциональность:
* просмотр каталога товаров;
* добавление и изменение товаров в корзине;
* оформление заказа;
* интеграция с `wallet-app` для списания средств;
* HTML‑интерфейс (Thymeleaf);
* централизованная обработка ошибок.

Технические особенности:
* Spring Boot 3 (WebFlux + MVC подход);
* R2DBC + PostgreSQL;
* Valkey для кэширования;
* Liquibase для управления схемой БД;
* WebClient для межсервисного взаимодействия;
* интеграционные тесты (Testcontainers).

### 2. **wallet-app**
Изолированный сервис управления балансом пользователя.

Функциональность:
* получение баланса;
* пополнение баланса;
* списание средств;
* возврат ошибок при недостатке средств.

Технические особенности:
* Spring Boot (REST);
* В качестве условной БД используется ConcurrentHashMap<K,V>
* интеграционные тесты контроллеров

---
## 🗂️ Структура репозитория
```text
.
├── docker-compose.yaml      # Запуск всей платформы
├── market-app               # Основной сервис маркетплейса
├── wallet-app               # Платёжный сервис
├── openapi                  # OpenAPI спецификация wallet API
├── pom.xml                  # Родительский Maven POM
└── README.md
```
Каждый сервис является самостоятельным Spring Boot приложением со своим `Dockerfile` и конфигурацией.

---
## ⚙️ Используемые технологии
* Java 21
* Spring Boot 3.5.8
* OpenAPI (API сервсиа wallet-app и клиент к нему генерируются на основе спецификации [wallet-api.yaml](openapi/wallet-api.yaml))
* Spring WebFlux / MVC
* R2DBC
* PostgreSQL
* Valkey
* Liquibase
* Maven (multi‑module)
* Docker / Docker Compose
* Testcontainers
* Thymeleaf
---
## 📦 Установка и запуск

### Предварительные требования
* Docker + Docker Compose
* JDK 21+
* Maven (или используйте ./mvnw)

### Шаги запуска
1. Клонировать репозиторий:
   ```bash
   git clone https://github.com/swfatumepta/my-market-app.git -b open-api-and-redis
   cd my-market-app
   ```
2. Собрать проект:
   ```bash
   ./mvnw clean build
   ```
   Для запуска интеграционных тестов:
   ```bash
   ./mvnw clean verify
   ```
3. Запустить сервисы:
   ```bash
   docker-compose up --build -d
   ```
После запуска сервисы доступны по url:

* market-app: [http://localhost:8080](http://localhost:8080)
* wallet-app API: [http://localhost:8081](http://localhost:8081)
* SWAGGER (wallet-app): [http://localhost:8081](http://localhost:8081/swagger-ui/index.html) <- можно пополнить кошелек при необходимости

---
## 🧪 Тестирование
Проект содержит:

* модульные тесты;
* интеграционные тесты контроллеров;
* интеграционные тесты БД и кэша;
* Testcontainers для PostgreSQL и Valkey. Тестовая конфигурация вынесена в `application-test.yml`.

