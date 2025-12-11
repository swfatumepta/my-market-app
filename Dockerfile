FROM eclipse-temurin:21-jre-alpine-3.22

COPY target/my-market-app.jar /app/app.jar
COPY app_entrypoint.sh /app/app_entrypoint.sh

RUN addgroup -g 1234 -S app_user_group \
    && adduser -u 1234 -S app_user -G app_user_group
#   Создать группу app_user_group -> addgroup -g 1234 -S app_user_group

#   addgroup - утилита Alpine Linux для добавления групп
#   -g 1234 - явно задаёт GID (Group ID) = 1234 (полезно для согласованности, особенно в k8s или при монтировании томов)
#   -S - "system group" (системная группа не предназначена для входа в систему, а только для изоляции прав)
#   app_user_group - имя создаваемой группы
#   ---
#   Создать пользователя app_user, добавить его в группу app_user_group -> adduser -u 1234 -S app_user -G app_user_group
#
#   adduser - утилита Alpine для добавления пользователей
#   -u 1234 - явно задаёт UID (User ID) = 1234
#   -S - "system group" (пользователь не имеет домашней директории по умолчанию, не может входить в систему (как root))
#   app_user - имя пользователя
#   -G app_user_group - добавить пользователя в группу app_user_group
RUN chmod +x /app/app_entrypoint.sh \
    && chown app_user:app_user_group /app/app.jar /app/app_entrypoint.sh
#   chmod +x - даёт право на выполнение (x = execute) скрипту
#   app_user:app_user_group - пользователь:группа

EXPOSE 8080 8000

WORKDIR /app
USER app_user

ENTRYPOINT ["sh", "./app_entrypoint.sh"]
