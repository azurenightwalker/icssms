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

    public long getThreadId() {
        return ThreadId;
    }

    public long getDate() {
        return Date;
    }

    private final String Body;
    private long Id;
    private long ThreadId;
    private long Date;
    private final String[] Addresses;

    public SmsMessage(String body, String address, long date, long id)
    {
        this(body,address,date);
        Id = id;
    }

    public SmsMessage(String body, String address, long date)
    {
        this(body,new String[] {address});
        Date = date;
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

    public void setThreadId(long tid)
    {
        ThreadId = tid;
    }
}
