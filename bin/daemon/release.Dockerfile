FROM ubuntu:20.04

COPY ./target/app.upx.native /opt/app

ENTRYPOINT ["/opt/app"]