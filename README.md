# my-market-app

## Требования
1) Для прогона тестов необходимо наличие Docker

## 📦 Установка и запуск
1) git clone https://github.com/swfatumepta/my-market-app/tree/spring-web-mvc
2) ./mvn clean build (./mvn clean verify для прогона интеграционных тестов)
3) docker-compose up --build -d

## 🛠 Особенности
1) PostgreSQL свои данные будет хрнаить в корне в директории /[db_data](db_data)
- при первом запуске приложения в БД будут загружены тестовые данные
- если необхродима повторная загрузка и сброс состояния БД, то переключите параметр spring.liquibase.drop-first в true
  (в [application.yml](market-app/src/main/resources/application.yml))
---
2) Для активации remote debug требуется установить значение переменной ENABLE_DEBUG=true в [.env](.env)
- так же, включает дополнительное логирование (какое, можно посомтреть в [application.yml](market-app/src/main/resources/application.yml))
3) Remote DEBUG доступен по localhost:8000

