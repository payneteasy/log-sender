package com.payneteasy.logsender.application;

import com.payneteasy.logsender.api.srvlog.messages.SaveLogsMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogResolverTest {

    @Test
    public void getNext() throws Exception {
        InputStream testInput = new ByteArrayInputStream(("2021.11.02 12:48:18.311+03:00 INFO [main]  t.chronicle.core.Jvm - Chronicle core loaded from file:/.m2/repository/net/openhft/chronicle-core/2.22ea12/chronicle-core-2.22ea12.jar\n" +
                "2021.11.02 12:48:18.318+03:00 WARN [main]  ChronicleQueueBuilder - Failback to readonly tablestore\n" +
                "net.openhft.chronicle.core.io.IORuntimeException: file=/opt/shop/queue/metadata.cq4t\n" +
                "\tat net.openhft.chronicle.queue.impl.table.SingleTableBuilder.build(SingleTableBuilder.java:150)\n" +
                "\tat net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.initializeMetadata(SingleChronicleQueueBuilder.java:450)\n" +
                "\tat net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.preBuild(SingleChronicleQueueBuilder.java:1097)\n" +
                "\tat net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.build(SingleChronicleQueueBuilder.java:327)\n" +
                "2021.11.02 12:48:18.319+03:00 WARN [main]  SingleChronicleQueue - Forcing queue to be readOnly file=/opt/shop/queue\n" +
                "2021.11.02 12:48:18.319+03:00 INFO [main]  er.InternalAnnouncer - Running under Java(TM) SE Runtime Environment 1.8.0_281-b09 with 12 processors reported.\n" +
                "2021.11.02 12:48:18.319+03:00 INFO [main]  er.InternalAnnouncer - Leave your e-mail to get information about the latest releases and patches at https://chronicle.software/release-notes/\n" +
                "2021.11.02 12:48:18.319+03:00 DEBUG [main]  er.InternalAnnouncer - Process id: 6886 :: Chronicle Queue (5.22ea9)").getBytes(StandardCharsets.UTF_8));
        InputStream old = System.in;
        try {
            System.setIn(testInput);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                LogResolver logResolver = new LogResolver("program", 2);
                List<SaveLogsMessage> messages = new ArrayList<>();
                while (messages.size() < 6) {
                    Optional<SaveLogsMessage> logMessage = logResolver.getNext(reader);
                    logMessage.ifPresent(messages::add);
                }
                Assert.assertEquals(6, messages.size());
                SaveLogsMessage target = messages.get(0);
                Assert.assertEquals("program", target.getProgram());
                Assert.assertEquals(Integer.valueOf(2), target.getFacility());
                Assert.assertEquals(Integer.valueOf(6), target.getSeverity());
                Assert.assertEquals(" [main]  t.chronicle.core.Jvm - Chronicle core loaded from file:/.m2/repository/net/openhft/chronicle-core/2.22ea12/chronicle-core-2.22ea12.jar"
                        , target.getMessage());
                Assert.assertEquals(1635846498311L, target.getTime());

                target = messages.get(1);
                Assert.assertEquals("program", target.getProgram());
                Assert.assertEquals(Integer.valueOf(2), target.getFacility());
                Assert.assertEquals(Integer.valueOf(4), target.getSeverity());
                Assert.assertEquals(" [main]  ChronicleQueueBuilder - Failback to readonly tablestore\n" +
                                "net.openhft.chronicle.core.io.IORuntimeException: file=/opt/shop/queue/metadata.cq4t\n" +
                                "at net.openhft.chronicle.queue.impl.table.SingleTableBuilder.build(SingleTableBuilder.java:150)\n" +
                                "at net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.initializeMetadata(SingleChronicleQueueBuilder.java:450)\n" +
                                "at net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.preBuild(SingleChronicleQueueBuilder.java:1097)\n" +
                                "at net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder.build(SingleChronicleQueueBuilder.java:327)"
                        , target.getMessage());
                Assert.assertEquals(1635846498318L, target.getTime());

                target = messages.get(2);
                Assert.assertEquals("program", target.getProgram());
                Assert.assertEquals(Integer.valueOf(2), target.getFacility());
                Assert.assertEquals(Integer.valueOf(4), target.getSeverity());
                Assert.assertEquals(" [main]  SingleChronicleQueue - Forcing queue to be readOnly file=/opt/shop/queue"
                        , target.getMessage());
                Assert.assertEquals(1635846498319L, target.getTime());

                target = messages.get(3);
                Assert.assertEquals("program", target.getProgram());
                Assert.assertEquals(Integer.valueOf(2), target.getFacility());
                Assert.assertEquals(Integer.valueOf(6), target.getSeverity());
                Assert.assertEquals(" [main]  er.InternalAnnouncer - Running under Java(TM) SE Runtime Environment 1.8.0_281-b09 with 12 processors reported."
                        , target.getMessage());
                Assert.assertEquals(1635846498319L, target.getTime());

                target = messages.get(4);
                Assert.assertEquals("program", target.getProgram());
                Assert.assertEquals(Integer.valueOf(2), target.getFacility());
                Assert.assertEquals(Integer.valueOf(6), target.getSeverity());
                Assert.assertEquals(" [main]  er.InternalAnnouncer - Leave your e-mail to get information about the latest releases and patches at https://chronicle.software/release-notes/"
                        , target.getMessage());
                Assert.assertEquals(1635846498319L, target.getTime());

                target = messages.get(5);
                Assert.assertEquals("program", target.getProgram());
                Assert.assertEquals(Integer.valueOf(2), target.getFacility());
                Assert.assertEquals(Integer.valueOf(7), target.getSeverity());
                Assert.assertEquals(" [main]  er.InternalAnnouncer - Process id: 6886 :: Chronicle Queue (5.22ea9)"
                        , target.getMessage());
                Assert.assertEquals(1635846498319L, target.getTime());
            }
        } finally {
            System.setIn(old);
        }


    }
}