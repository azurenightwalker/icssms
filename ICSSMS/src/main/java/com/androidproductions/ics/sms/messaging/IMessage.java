package com.androidproductions.ics.sms.messaging;

import android.graphics.Bitmap;

public interface IMessage {
	public Long getThreadId();
    public long getId();

	public String getText();
	public String getAddress();
	public String getContactName();
    public CharSequence getSummaryHeader();

	public Bitmap getContactPhoto();
    public Bitmap getConversationContactImage();

    public boolean IsIncoming();

	public boolean isUnread();
    public void markAsRead();
    public void markAsUnread();

	public Long getDate();
    public String GetDateString();
    public CharSequence GetShortDateString();

	public boolean isLocked();
    public void lockMessage();
    public void unlockMessage();

	public IMessage getPrevious();
	public boolean sendingFailed();

    public boolean deleteMessage();
}
