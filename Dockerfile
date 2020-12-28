FROM openjdk
WORKDIR /app

COPY . .
RUN /app/gradlew build
CMD java -jar /app/build/libs/discord-bot-1.0-SNAPSHOT.jar 80