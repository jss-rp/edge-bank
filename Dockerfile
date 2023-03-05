FROM amazoncorretto:17
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY ${JAR_FILE} edge-bank.jar
EXPOSE 8080
CMD java -jar edge-bank.jar
