package com.grundfos.pump.replace.model;

import org.slf4j.LoggerFactory;

public class MetadataOutput {
    private String logData;
    public MetadataOutput() {}
    public MetadataOutput(String logData) {
        this.logData = logData;
    }
    public String getLogData() {
        return logData;
    }
    public void setLogData(String logData) {
        this.logData = logData;
    }

    @Override
    public String toString() {
        return "MetadataOutput{" +
                "logData='" + logData + '\'' +
                '}';
    }
}
