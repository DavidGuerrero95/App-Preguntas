FROM openjdk:12
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} Preguntas.jar
ENTRYPOINT ["java","-jar","/Preguntas.jar"]