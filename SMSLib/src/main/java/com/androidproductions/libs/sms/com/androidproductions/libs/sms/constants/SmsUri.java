package com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants;

import android.net.Uri;

public final class SmsUri {
    public static final Uri BASE_URI= Uri.parse("content://sms/");
    public static final Uri INBOX_URI = Uri.parse("content://sms/inbox");
    public static final Uri FAILED_URI = Uri.parse("content://sms/failed");
    public static final Uri QUEUED_URI = Uri.parse("content://sms/queued");
    public static final Uri SENT_URI = Uri.parse("content://sms/sent");
    public static final Uri DRAFT_URI = Uri.parse("content://sms/draft");
    public static final Uri OUTBOX_URI = Uri.parse("content://sms/outbox");
    public static final Uri UNDELIVERED_URI = Uri.parse("content://sms/undelivered");
    public static final Uri All_URI = Uri.parse("content://sms/all");
    public static final Uri CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
    public static final Uri SMS_ONLY_CONVERSATIONS_URI = Uri.parse("content://sms/conversations");
    public static final Uri THREAD_URI = Uri.parse("content://mms-sms/threadID");

    private SmsUri() {
    }
}
