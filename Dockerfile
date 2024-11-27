# Build the application using the OpenJDK development image
FROM registry.suse.com/bci/openjdk-devel:21

# Get the TabletopGames project sources
ADD . /TabletopGames
WORKDIR /TabletopGames

# Create JARs TAG
RUN mvn package -Dassembly.skipAssembly=true
# Download dependencies
RUN mvn dependency:copy-dependencies

# Bundle the application into OpenJDK runtime image
FROM registry.suse.com/bci/openjdk:21

# Copy over only the project (with generated JARs now)
COPY --from=0 /TabletopGames/json /json
COPY --from=0 /TabletopGames/data /data
COPY --from=0 /TabletopGames/metrics /metrics
COPY --from=0 /TabletopGames/target/dependency /jars
COPY --from=0 /TabletopGames/target/ModernBoardGame*.jar /jars/ModernBoardGame.jar

ENTRYPOINT ["java", "-cp", "/jars/*", "evaluation.RunGames"]
