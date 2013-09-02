package com.androidproductions.ics.sms.messaging;

import android.graphics.Bitmap;

public interface IMessage {
	public Long getThreadId();
	public String getText();
	public boolean hasAttachments();
	public String getAddress();
	public String getContactName();
	public Bitmap getContactPhoto();
	public boolean isUnread();
	public Long getDate();
	public boolean IsIncoming();
	public CharSequence GetShortDateString();
	public CharSequence getSummaryHeader();
	public Bitmap getConversationContactImage();
	public void markAsRead();
	public boolean isLocked();
	public long getId();
	public boolean deleteMessage();
	public String GetDateString();
	public void lockMessage();
	public void unlockMessage();
	public void markAsUnread();
	public IMessage getPrevious();
	public boolean sendingFailed();
}
