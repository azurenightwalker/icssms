package com.androidproductions.libs.sms;

/*
* SmsMessageView:
*       Base class to represent an SMS message, containing the minimum information
*       required to perform all transaction-based tasks.
* */
public class SmsMessage {
    protected final String Body;
    protected long Id;
    protected long ThreadId;
    protected final long Date;
    protected final String[] Addresses;

    public SmsMessage(final String body, final String address, final long date, final long id)
    {
        this(body,address,date);
        Id = id;
    }

    public SmsMessage(final String body, final String[] addresses, final long date, final long id)
    {
        this(body,addresses,date);
        Id = id;
    }

    public SmsMessage(final String body, final String address, final long date)
    {
        this(body,new String[] {address},date);
    }

    public SmsMessage(final String body, final String[] addresses, final long date)
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

    public Long getDate() {
        return Date;
    }

    public long getId() {
        return Id;
    }

    public long getThreadId() {
        return ThreadId;
    }

    public void setThreadId(final long tid)
    {
        ThreadId = tid;
    }
}
