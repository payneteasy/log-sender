package com.payneteasy.logsender.application;

import com.google.gson.Gson;
import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import com.payneteasy.http.client.impl.HttpClientImpl;
import com.payneteasy.logsender.api.srvlog.ISrvLogRemoteService;
import com.payneteasy.logsender.api.srvlog.SaveLogsException;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsRequest;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsResponse;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * @author rkononenko, 02.11.2021
 */
public class SrvLogRemoteServiceImpl implements ISrvLogRemoteService {

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10_000;
    private static final int DEFAULT_READ_TIMEOUT = 10_000;

    private final Gson gson = new Gson();
    private final String baseUrl;

    private final IHttpClient httpClient;
    private final HttpRequestParameters httpParameters;

    public SrvLogRemoteServiceImpl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new HttpClientImpl();
        this.httpParameters = HttpRequestParameters.builder()
                .timeouts(new HttpTimeouts(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT))
                .build();
    }

    @Override
    public SaveLogsResponse saveLogs(SaveLogsRequest aRequest) throws SaveLogsException {
        String message = gson.toJson(aRequest);
        return saveLogs(message);
    }

    @Override
    public SaveLogsResponse saveLogs(String message) throws SaveLogsException {
        HttpRequest request = HttpRequest.builder().url(baseUrl).method(HttpMethod.POST)
                .body(message.getBytes(StandardCharsets.UTF_8)).build();
        HttpResponse response;
        try {
            response = httpClient.send(request, httpParameters);
        } catch (HttpConnectException | HttpReadException | HttpWriteException e) {
            throw new SaveLogsException(e.getMessage(), e);
        }

        if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
            String msg = String.format("Error response code: %s", response.getStatusCode());
            throw new SaveLogsException(msg);
        }
        return gson.fromJson(new String(response.getBody(), StandardCharsets.UTF_8), SaveLogsResponse.class);
    }
}
