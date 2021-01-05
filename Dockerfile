FROM oracle/graalvm-ce:19.2.1

RUN yum install -y zip unzip
RUN gu install native-image

WORKDIR /app

COPY . .
RUN native-image -cp /app/build/libs/discord-bot-1.0-SNAPSHOT.jar -H:Name=/app/run -H:Class=me.plony.bot.MainKt -H:+ReportUnsupportedElementsAtRuntime --allow-incomplete-classpath
CMD /app/run 80