package com.payneteasy.logsender.api.srvlog;

import com.payneteasy.logsender.api.srvlog.messages.SaveLogsRequest;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsResponse;

public interface ISrvLogRemoteService {

    SaveLogsResponse saveLogs(SaveLogsRequest aRequest) throws SaveLogsException;

    SaveLogsResponse saveLogs(String message) throws SaveLogsException;

}
