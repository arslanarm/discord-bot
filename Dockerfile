FROM openjdk
COPY . /app
CMD /app/gradlew run