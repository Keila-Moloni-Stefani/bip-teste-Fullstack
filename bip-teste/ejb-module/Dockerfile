FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM quay.io/wildfly/wildfly:32.0.0.Final-jdk17
COPY --from=build /app/target/ejb-module.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
