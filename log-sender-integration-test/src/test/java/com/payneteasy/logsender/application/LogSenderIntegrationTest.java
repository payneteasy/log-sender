package com.payneteasy.logsender.application;

import com.google.gson.Gson;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsMessage;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.RequestDefinition;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author rkononenko, 03.11.2021
 */
public class LogSenderIntegrationTest {

    private final Gson gson = new Gson();

    private final int port = 9891;
    private ClientAndServer server;

    @Before
    public void startServer() {
        server = startClientAndServer(port);
        new MockServerClient("127.0.0.1", port)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/srv-log")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("{\"status\":\"SUCCESS\"}")
                );
    }

    @After
    public void stopServer() {
        server.stop();
    }

    @Test
    public void test() throws IOException {
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

        LogSender logSender = new LogSender("target", "http://localhost:" + port + "/srv-log",
                false, null, "program", 2);
        logSender.start();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(testInput))) {
            RequestDefinition[] recordedRequests = null;
            while (recordedRequests == null || recordedRequests.length < 6) {
                logSender.waitAndProcessNextRecord(reader);
                recordedRequests = new MockServerClient("localhost", port)
                        .retrieveRecordedRequests(
                                request()
                                        .withMethod("POST")
                                        .withPath("/srv-log")
                        );
            }
            HttpRequest httpRequest = (HttpRequest) recordedRequests[0];
            SaveLogsRequest request = gson.fromJson(httpRequest.getBodyAsJsonOrXmlString(), SaveLogsRequest.class);
            Assert.assertNotNull(request);
            Assert.assertEquals(1, request.getMessages().size());
            SaveLogsMessage target = request.getMessages().get(0);
            Assert.assertEquals("program", target.getProgram());
            Assert.assertEquals(Integer.valueOf(2), target.getFacility());
            Assert.assertEquals(Integer.valueOf(6), target.getSeverity());
            Assert.assertEquals(" [main]  t.chronicle.core.Jvm - Chronicle core loaded from file:/.m2/repository/net/openhft/chronicle-core/2.22ea12/chronicle-core-2.22ea12.jar"
                    , target.getMessage());
            Assert.assertEquals(1635846498311L, target.getTime());

            httpRequest = (HttpRequest) recordedRequests[1];
            request = gson.fromJson(httpRequest.getBodyAsJsonOrXmlString(), SaveLogsRequest.class);
            Assert.assertNotNull(request);
            target = request.getMessages().get(0);
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

            httpRequest = (HttpRequest) recordedRequests[2];
            request = gson.fromJson(httpRequest.getBodyAsJsonOrXmlString(), SaveLogsRequest.class);
            Assert.assertNotNull(request);
            Assert.assertEquals(1, request.getMessages().size());
            target = request.getMessages().get(0);
            Assert.assertEquals("program", target.getProgram());
            Assert.assertEquals(Integer.valueOf(2), target.getFacility());
            Assert.assertEquals(Integer.valueOf(4), target.getSeverity());
            Assert.assertEquals(" [main]  SingleChronicleQueue - Forcing queue to be readOnly file=/opt/shop/queue"
                    , target.getMessage());
            Assert.assertEquals(1635846498319L, target.getTime());

            httpRequest = (HttpRequest) recordedRequests[3];
            request = gson.fromJson(httpRequest.getBodyAsJsonOrXmlString(), SaveLogsRequest.class);
            Assert.assertNotNull(request);
            target = request.getMessages().get(0);
            Assert.assertEquals("program", target.getProgram());
            Assert.assertEquals(Integer.valueOf(2), target.getFacility());
            Assert.assertEquals(Integer.valueOf(6), target.getSeverity());
            Assert.assertEquals(" [main]  er.InternalAnnouncer - Running under Java(TM) SE Runtime Environment 1.8.0_281-b09 with 12 processors reported."
                    , target.getMessage());
            Assert.assertEquals(1635846498319L, target.getTime());

            httpRequest = (HttpRequest) recordedRequests[4];
            request = gson.fromJson(httpRequest.getBodyAsJsonOrXmlString(), SaveLogsRequest.class);
            Assert.assertNotNull(request);
            target = request.getMessages().get(0);
            Assert.assertEquals("program", target.getProgram());
            Assert.assertEquals(Integer.valueOf(2), target.getFacility());
            Assert.assertEquals(Integer.valueOf(6), target.getSeverity());
            Assert.assertEquals(" [main]  er.InternalAnnouncer - Leave your e-mail to get information about the latest releases and patches at https://chronicle.software/release-notes/"
                    , target.getMessage());
            Assert.assertEquals(1635846498319L, target.getTime());

            httpRequest = (HttpRequest) recordedRequests[5];
            request = gson.fromJson(httpRequest.getBodyAsJsonOrXmlString(), SaveLogsRequest.class);
            Assert.assertNotNull(request);
            target = request.getMessages().get(0);
            Assert.assertEquals("program", target.getProgram());
            Assert.assertEquals(Integer.valueOf(2), target.getFacility());
            Assert.assertEquals(Integer.valueOf(7), target.getSeverity());
            Assert.assertEquals(" [main]  er.InternalAnnouncer - Process id: 6886 :: Chronicle Queue (5.22ea9)"
                    , target.getMessage());
            Assert.assertEquals(1635846498319L, target.getTime());
        }
    }

}
