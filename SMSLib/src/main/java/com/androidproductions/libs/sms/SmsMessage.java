package com.androidproductions.libs.sms;

public class SmsMessage {
    public String getBody() {
        return Body;
    }

    public String[] getAddresses() {
        return Addresses;
    }

    public long getId() {
        return Id;
    }

    private final String Body;
    private long Id;
    private final String[] Addresses;

    public SmsMessage(String body, String address, long id)
    {
        this(body,new String[] {address});
        Id = id;
    }

    public SmsMessage(String body, String address)
    {
        this(body,new String[] {address});
    }

    public SmsMessage(String body, String[] addresses)
    {
        Body = body;
        Addresses = addresses;
    }
}
