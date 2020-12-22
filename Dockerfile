FROM openjdk
COPY . /app
CMD ./gradlew run