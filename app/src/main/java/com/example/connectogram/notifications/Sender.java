package com.example.connectogram.notifications;

public class Sender {
    private  Data data;
    private  String to;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Sender() {
    }

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
}
