# Build the application using the OpenJDK development image
FROM registry.suse.com/bci/openjdk-devel:21

# Get the TabletopGames project sources
ADD . /TabletopGames
WORKDIR /TabletopGames

# Start from scratch
RUN mvn clean
# Compile TAG
RUN mvn compile
# Create JARs TAG
RUN mvn package

# Bundle the application into OpenJDK runtime image
FROM registry.suse.com/bci/openjdk:21

# Copy over only the project (with generated JARs now)
COPY --from=0 /TabletopGames/json /json
COPY --from=0 /TabletopGames/data /data
COPY --from=0 /TabletopGames/target/RunGames-jar-with-dependencies.jar /RunGames-jar-with-dependencies.jar

ENTRYPOINT ["java", "-jar", "/RunGames-jar-with-dependencies.jar"]