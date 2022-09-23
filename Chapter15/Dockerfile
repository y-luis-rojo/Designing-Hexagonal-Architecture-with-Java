FROM adoptopenjdk/openjdk11:x86_64-alpine-jre11u-nightly

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 bootstrap/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 bootstrap/target/quarkus-app/*.jar /deployments/
COPY --chown=185 bootstrap/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 bootstrap/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080

USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

WORKDIR /deployments
ENTRYPOINT ["sh", "-c"]

ENV QUARKUS_LAUNCH_DEVMODE=true

CMD ["java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar $JAVA_APP_JAR"]
