FROM oracle/graalvm-ce

RUN yum install -y zip unzip
RUN gu install native-image

WORKDIR /app

COPY . .
RUN /app/gradlew build
RUN native-image -cp /app/build/libs/discord-bot-1.0-SNAPSHOT.jar -H:Name=/app/run -H:Class=me.plony.bot.MainKt -H:+ReportUnsupportedElementsAtRuntime --allow-incomplete-classpath
CMD /app/run 80