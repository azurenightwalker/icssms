//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations.
//


package com.androidproductions.ics.sms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import com.androidproductions.ics.sms.R.id;
import com.androidproductions.ics.sms.R.layout;
import com.androidproductions.ics.sms.R.string;
import com.googlecode.androidannotations.api.SdkVersionHelper;

public final class ComposeSms_
    extends ComposeSms
{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(layout.sms_compose);
    }

    private void init_(Bundle savedInstanceState) {
        Resources resources_ = this.getResources();
        textFormat = resources_.getString(string.characterCount);
    }

    private void afterSetContentView_() {
        textCount = ((TextView) findViewById(id.textCount));
        phoneNumber = ((AutoCompleteTextView) findViewById(id.phoneNumber));
        textView = ((EditText) findViewById(id.text));
        {
            final TextView view = ((TextView) findViewById(id.text));
            if (view!= null) {
                view.addTextChangedListener(new TextWatcher() {


                    @Override
                    public void afterTextChanged(Editable s) {
                        ComposeSms_.this.afterTextChanged(s);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                }
                );
            }
        }
        redrawView();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        afterSetContentView_();
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        afterSetContentView_();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        afterSetContentView_();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((SdkVersionHelper.getSdkInt()< 5)&&(keyCode == KeyEvent.KEYCODE_BACK))&&(event.getRepeatCount() == 0)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static ComposeSms_.IntentBuilder_ intent(Context context) {
        return new ComposeSms_.IntentBuilder_(context);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(com.androidproductions.ics.sms.R.menu.compose_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, ComposeSms_.class);
        }

        public Intent get() {
            return intent_;
        }

        public ComposeSms_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startActivity(intent_);
        }

        public void startForResult(int requestCode) {
            if (context_ instanceof Activity) {
                ((Activity) context_).startActivityForResult(intent_, requestCode);
            } else {
                context_.startActivity(intent_);
            }
        }

    }

}
