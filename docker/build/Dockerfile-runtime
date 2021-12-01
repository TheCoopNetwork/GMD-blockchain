FROM debian:stable-slim

RUN mkdir -p /GMD-blockchain/nxt_db
RUN mkdir -p /GMD-blockchain/logs

COPY conf/ /GMD-blockchain/conf/
COPY html/ /GMD-blockchain/html/
COPY lib/ /GMD-blockchain/lib/
COPY jre-for-gmd/ /GMD-blockchain/jre-for-gmd/

ADD ./CoopNetwork.jar /GMD-blockchain/CoopNetwork.jar
ADD ./start.sh /GMD-blockchain/start.sh

EXPOSE 6876 6874

CMD bash -c "cd /GMD-blockchain && . start.sh"