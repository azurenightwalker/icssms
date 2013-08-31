package com.androidproductions.ics.sms;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.androidproductions.ics.sms.data.ContactHelper;
import com.androidproductions.ics.sms.data.adapters.ContactsCursorAdapter;
import com.androidproductions.ics.sms.utils.LogHelper;

public class ContactListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private ContactsCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoaderManager loadermanager = getLoaderManager();
        /*Empty adapter that is used to display the loaded data*/
        mAdapter = new ContactsCursorAdapter(this.getActivity());
        setListAdapter(mAdapter);
        loadermanager.initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new ContactHelper(this.getActivity()).getPhoneCursor();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(mAdapter!=null && cursor!=null)
            mAdapter.swapCursor(cursor);
        else
            LogHelper.v("OnLoadFinished: mAdapter is null");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if(mAdapter!=null)
            mAdapter.swapCursor(null);
        else
            LogHelper.v("OnLoadFinished: mAdapter is null");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ((ComposeSms)this.getActivity()).updateCompose(convertToString(v));
    }

    String convertToString(View v) {
        CharSequence name = ((TextView)v.findViewById(com.androidproductions.ics.sms.R.id.contact_name)).getText();
        CharSequence number = ((TextView)v.findViewById(com.androidproductions.ics.sms.R.id.contact_number)).getText();
        return name + " (" + number + ")";
    }
}
