FROM openjdk:8-jre

COPY ianzpoc-1.0-SNAPSHOT.zip ianzpoc.zip

RUN unzip ianzpoc.zip && rm -fR ianzpoc.zip

EXPOSE 9000

WORKDIR /ianzpoc-1.0-SNAPSHOT

ENTRYPOINT ["bin/ianzpoc"]
