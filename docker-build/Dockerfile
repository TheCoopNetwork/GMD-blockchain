FROM openjdk:16-bullseye

RUN apt-get update && apt-get install -y \
  docker.io \
  && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /output/
RUN mkdir -p /scripts/
RUN mkdir -p /srcgmd/
RUN mkdir -p /output-docker-image/

COPY ./ /srcgmd/
COPY docker-build/docker-image /docker-image

ADD ./docker-build/build.sh /scripts/build.sh
CMD bash /scripts/build.sh
