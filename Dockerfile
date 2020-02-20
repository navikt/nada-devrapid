FROM navikt/java:13

RUN apt-get -yy install kafkacat

COPY build/libs/nada-devrapid-all.jar /app/app.jar