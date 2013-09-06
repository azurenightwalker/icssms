package com.androidproductions.libs.sms;

import android.content.ContentValues;
import android.content.Context;

import com.androidproductions.libs.sms.com.androidproductions.libs.sms.TextUtilities;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.MessageType;

/*
* SmsMessageView:
*       Base class to represent an SMS message, containing the minimum information
*       required to perform all transaction-based tasks.
* */
public class SmsMessage {
    private final String Body;
    protected long Id;
    private long ThreadId;
    protected final long Date;
    protected final String[] Addresses;
    private int Type;
    private int Protocol;
    private String Subject;
    private int ReplyPathPresent;
    private String ServiceCentre;

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

    private SmsMessage(final String body, final String[] addresses, final long date)
    {
        Date = date;
        Body = body;
        Addresses = addresses;
    }

    public SmsMessage(final Object[] messages)
    {
        final android.telephony.SmsMessage[] msgs = new android.telephony.SmsMessage[messages.length];
        for (int n = 0; n < messages.length; n++) {
            msgs[n] = android.telephony.SmsMessage.createFromPdu((byte[]) messages[n]);
        }
        final android.telephony.SmsMessage smsa = msgs[0];
        final int pduCount = msgs.length;
        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            Body = smsa.getDisplayMessageBody();
        } else {
            // Build up the body from the parts.
            final StringBuilder bdy = new StringBuilder();
            for (final android.telephony.SmsMessage smsb : msgs) {
                bdy.append(smsb.getDisplayMessageBody());
            }
            Body = TextUtilities.replaceFormFeeds(bdy.toString());
        }
        Addresses = new String[] {smsa.getOriginatingAddress()};
        Type = MessageType.INBOX;
        Date = System.currentTimeMillis();
        Protocol =  smsa.getProtocolIdentifier();
        int read = 0;
        Subject = "";
        if (!smsa.getPseudoSubject().isEmpty())
            Subject = smsa.getPseudoSubject();
        ReplyPathPresent = smsa.isReplyPathPresent() ? 1 : 0;
        ServiceCentre = smsa.getServiceCenterAddress();
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

    ContentValues buildContentValues(final Context context) {
        // Store the message in the content provider.
        final ContentValues values = new ContentValues();

        values.put("body", Body);
        values.put("address", Addresses[0]);

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        // Check to make sure the system is giving us a non-bogus time.
        //Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
        //Calendar nowDate = new GregorianCalendar();
        //long now = System.currentTimeMillis();
        //nowDate.setTimeInMillis(now);
        values.put("date", Date);
        //values.put(Inbox.DATE_SENT, DateSent);
        values.put("protocol", Protocol);
        values.put("read", 0);
        values.put("seen", 0);
        values.put("type", Type);
        values.put("subject", Subject);
        values.put("reply_path_present", ReplyPathPresent);
        values.put("service_center", ServiceCentre);

        if (Addresses != null) {
            final Long threadId = new Transaction(context).getOrCreateThreadId(Addresses[0]);
            values.put("thread_id", threadId);
        }

        return values;
    }
}
