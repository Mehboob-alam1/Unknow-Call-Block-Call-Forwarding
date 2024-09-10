package com.example.callblock;

public class CallLog {

    private String callDuration;
    private String incoming;
    private String pushId;
    private String recordingLink;
    private String summary;
    private String timestamp;


    public CallLog() {
    }


    public CallLog(String callDuration, String incoming, String pushId, String recordingLink, String summary, String timestamp) {
        this.callDuration = callDuration;
        this.incoming = incoming;
        this.pushId = pushId;
        this.recordingLink = recordingLink;
        this.summary = summary;
        this.timestamp = timestamp;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public String getIncoming() {
        return incoming;
    }

    public void setIncoming(String incoming) {
        this.incoming = incoming;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getRecordingLink() {
        return recordingLink;
    }

    public void setRecordingLink(String recordingLink) {
        this.recordingLink = recordingLink;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
