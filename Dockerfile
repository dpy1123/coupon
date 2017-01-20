FROM java:8
MAINTAINER dpy1123

ENV MONGO_URL "mongodb://172.16.6.112:32633"

RUN echo "Asia/Shanghai" > /etc/timezone

COPY target/Coupon-0.2.jar /data/app/crawler/
COPY target/lib /data/app/crawler/lib/

WORKDIR /data/app/crawler/
CMD ["/bin/bash"]
ENTRYPOINT java -jar Coupon-0.2.jar $MONGO_URL

EXPOSE 8877
EXPOSE 8899
EXPOSE 8898

#docker build -t dd-crawler:v0.2 .
#docker run -d --name dd-coupon -e "MONGO_URL=mongodb://localhost:27017" -P dd-crawler:v0.2
