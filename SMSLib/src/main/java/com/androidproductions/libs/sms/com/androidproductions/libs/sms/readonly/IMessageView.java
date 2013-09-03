package com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly;

import android.graphics.Bitmap;
import android.net.Uri;

public interface IMessageView {
	public long getThreadId();
    public long getId();
    public Uri getUri();

	public String getBody();
	public String getAddress();
	public String getContactName();

	public Bitmap getContactPhoto();
    public Bitmap getConversationContactImage();

    public boolean IsIncoming();

	public boolean isUnread();

	public Long getDate();
    public String GetDateString();
    public CharSequence GetShortDateString();

	public boolean isLocked();

	public IMessageView getPrevious();
	public boolean sendingFailed();
}
