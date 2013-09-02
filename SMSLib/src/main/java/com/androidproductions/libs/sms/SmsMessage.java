package com.androidproductions.libs.sms;

/*
* SmsMessage:
*       Base class to represent an SMS message, containing the minimum information
*       required to perform all transaction-based tasks.
* */
public class SmsMessage {
    private final String Body;
    private long Id;
    private long ThreadId;
    private final long Date;
    private final String[] Addresses;

    public SmsMessage(String body, String address, long date, long id)
    {
        this(body,address,date);
        Id = id;
    }

    public SmsMessage(String body, String[] addresses, long date, long id)
    {
        this(body,addresses,date);
        Id = id;
    }

    public SmsMessage(String body, String address, long date)
    {
        this(body,new String[] {address},date);
    }

    public SmsMessage(String body, String[] addresses, long date)
    {
        Date = date;
        Body = body;
        Addresses = addresses;
    }

    public String getBody() {
        return Body;
    }

    public String[] getAddresses() {
        return Addresses;
    }

    public long getDate() {
        return Date;
    }

    public long getId() {
        return Id;
    }

    public long getThreadId() {
        return ThreadId;
    }

    public void setThreadId(long tid)
    {
        ThreadId = tid;
    }
}
