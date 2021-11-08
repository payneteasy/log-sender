### Requirements
- Java 8

### Installation
```bash
mvn clean install
```

### Run command example
```text
java -jar log-sender-application-1.0-SNAPSHOT-jar-with-dependencies.jar -qdir /opt/app/queue -url https://example.com/srv-log -prg example -fac 12
```

### Arguments
* -qdir(--queue-dir) - path to directory where Chronicle queue will store files
* -url(--srvlog-base-url) - srvlog server URL
* -prg(--program) - 'program' value in srvlog format
* -fac(--facility) - 'facility' value in srvlog format
* -del(--delete-rolled) (optional) - delete Chronicle queue files after they got rolled
* -cycle(--roll-cycle) (optional) - Chronicle queue rolling cycle (MINUTELY, HOURLY, DAILY, e.t.c.)

### Log format
```text
%date{ISO8601} %level %thread  %logger %mdc %message%n
```

### Log format example
```text
2021.11.02 12:48:18.311+03:00 INFO [main]  t.chronicle.core.Jvm - Chronicle core loaded from file:/.m2/repository/net/openhft/chronicle-core/2.22ea12/chronicle-core-2.22ea12.jar
2021.11.02 12:48:18.318+03:00 WARN [main]  ChronicleQueueBuilder - Failback to readonly tablestore
    net.openhft.chronicle.core.io.IORuntimeException: file=/opt/shop/queue/metadata.cq4t
        tat net.openhft.chronicle.queue.impl.table.SingleTableBuilder.build(SingleTableBuilder.java:150)
        tat net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.initializeMetadata(SingleChronicleQueueBuilder.java:450)
        tat net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.preBuild(SingleChronicleQueueBuilder.java:1097)
        tat net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.build(SingleChronicleQueueBuilder.java:327)
2021.11.02 12:48:18.319+03:00 WARN [main]  SingleChronicleQueue - Forcing queue to be readOnly file=/opt/shop/queue
2021.11.02 12:48:18.319+03:00 INFO [main]  er.InternalAnnouncer - Running under Java(TM) SE Runtime Environment 1.8.0_281-b09 with 12 processors reported.
2021.11.02 12:48:18.319+03:00 INFO [main]  er.InternalAnnouncer - Leave your e-mail to get information about the latest releases and patches at https://chronicle.software/release-notes/
2021.11.02 12:48:18.319+03:00 DEBUG [main]  er.InternalAnnouncer - Process id: 6886 :: Chronicle Queue (5.22ea9)
```

### Logback configuration pattern
```xml
<encoder>
    <pattern>%d{yyyy.MM.dd HH:mm:ss.SSSXXX} %level [%thread]  %logger %mdc - %msg%n</pattern>
</encoder>
```