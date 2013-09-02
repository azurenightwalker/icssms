package com.androidproductions.ics.sms.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import java.util.ArrayList;

public class KeyboardDetectorScrollView extends ScrollView {

	public interface IKeyboardChanged {
        void onKeyboardShown();
        void onKeyboardHidden();
    }

	private boolean KeyboardShown;

    private final ArrayList<IKeyboardChanged> keyboardListener = new ArrayList<IKeyboardChanged>();

    public KeyboardDetectorScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardDetectorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardDetectorScrollView(Context context) {
        super(context);
    }

    public void addKeyboardStateChangedListener(IKeyboardChanged listener) {
        keyboardListener.add(listener);
    }

    public void removeKeyboardStateChangedListener(IKeyboardChanged listener) {
        keyboardListener.remove(listener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedheight && !KeyboardShown) {
            notifyKeyboardShown();
        } else if (actualHeight < proposedheight && KeyboardShown) {
            notifyKeyboardHidden();
        }
    }

    private void notifyKeyboardHidden() {
    	KeyboardShown = false;
        for (IKeyboardChanged listener : keyboardListener) {
            listener.onKeyboardHidden();
        }
    }

    private void notifyKeyboardShown() {
    	KeyboardShown = true;
        for (IKeyboardChanged listener : keyboardListener) {
            listener.onKeyboardShown();
        }
    }
}