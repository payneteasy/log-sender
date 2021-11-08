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
