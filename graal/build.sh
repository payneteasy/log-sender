#!/usr/bin/env bash

native-image \
  --verbose \
  --allow-incomplete-classpath \
  --no-fallback  \
  --enable-url-protocols=http \
  -jar ../log-sender-application/target/log-sender-application-1.0-1-SNAPSHOT-jar-with-dependencies.jar

#  -H:ConfigurationFileDirectories=META-INF/native-image \

#  --initialize-at-build-time=com.payneteasy.realproxy.server.RealProxyServerApplication,org.slf4j.LoggerFactory,org.slf4j.impl.SimpleLogger,org.slf4j.impl.StaticLoggerBinder \
#  --trace-class-initialization=org.slf4j.impl.StaticLoggerBinder \
