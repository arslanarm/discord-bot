FROM openjdk
WORKDIR /app
COPY . .
CMD /app/gradlew run