package com.example.callblock;

public class Settings {

    private String completePrompt;
    private String forwardingNumber;
    private String greeting;
    private String midPrompt;


    public Settings() {
    }

    public Settings(String completePrompt, String forwardingNumber, String greeting, String midPrompt) {
        this.completePrompt = completePrompt;
        this.forwardingNumber = forwardingNumber;
        this.greeting = greeting;
        this.midPrompt = midPrompt;
    }

    public String getCompletePrompt() {
        return completePrompt;
    }



    public void setCompletePrompt(String completePrompt) {
        this.completePrompt = completePrompt;
    }

    public String getForwardingNumber() {
        return forwardingNumber;
    }

    public void setForwardingNumber(String forwardingNumber) {
        this.forwardingNumber = forwardingNumber;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getMidPrompt() {
        return midPrompt;
    }

    public void setMidPrompt(String midPrompt) {
        this.midPrompt = midPrompt;
    }
}
