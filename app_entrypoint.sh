#!/bin/sh

if [ "$ENABLE_DEBUG" = "true" ]; then
  exec java -agentlib:jdwp=transport=dt_socket,server=y,address=*:8000,suspend=n -jar app.jar --spring.profiles.active=debug
else
  exec java -jar app.jar
fi

# КОММЕНТАРИИ ПО JVM И JDWP:
#
# JDWP-агент (Java Debug Wire Protocol):
# - Активирует механизм удаленной отладки в JVM
# - Превращает JVM в debug-сервер, готовый принимать подключения

# Параметры JDWP:
#
# transport=dt_socket - использует TCP/IP сокеты для связи
#                       (вместо shared memory, который не работает в Docker)
# server=y            - JVM работает как СЕРВЕР отладки
#                       (принимает входящие подключения от IDE)
# address=*:8000      - слушает на всех сетевых интерфейсах (*)
#                       на порту 8000
#                       * - критично важно для работы в Docker!
# suspend=n           - НЕ приостанавливать выполнение при старте
#                       Приложение запускается сразу, не дожидаясь debugger'а
#                       (suspend=y - ждет подключения отладчика перед запуском main)
