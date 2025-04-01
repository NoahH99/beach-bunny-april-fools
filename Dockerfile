FROM eclipse-temurin:21-jre

ENV APP_NAME="beach-bunny-bot"
ENV LOG_LEVEL="INFO"
ENV USE_OPENAI_GRADING="true"
ENV BOT_TOKEN=""
ENV OPENAI_API_KEY=""
ENV SERVER_ID=""
ENV ANNOUNCEMENT_CHANNEL_ID=""

WORKDIR /app

VOLUME ["/app/data", "/data/logs"]

COPY build/libs/beach-bunny-april-fools-1.0.0-rc.1.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
