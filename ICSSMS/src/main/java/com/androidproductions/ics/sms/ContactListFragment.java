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
import com.androidproductions.logging.LogHelper;

public class ContactListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private ContactsCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LoaderManager loadermanager = getLoaderManager();
        /*Empty adapter that is used to display the loaded data*/
        mAdapter = new ContactsCursorAdapter(this.getActivity());
        setListAdapter(mAdapter);
        loadermanager.initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int i, final Bundle bundle) {
        return new ContactHelper(this.getActivity()).getPhoneCursor();
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        if(mAdapter!=null && cursor!=null)
            mAdapter.swapCursor(cursor);
        else
            LogHelper.getInstance().v("OnLoadFinished: mAdapter is null");
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {
        if(mAdapter!=null)
            mAdapter.swapCursor(null);
        else
            LogHelper.getInstance().v("OnLoadFinished: mAdapter is null");
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        ((ComposeSms)this.getActivity()).updateCompose(convertToString(v));
    }

    String convertToString(final View v) {
        final CharSequence name = ((TextView)v.findViewById(com.androidproductions.ics.sms.R.id.contact_name)).getText();
        final CharSequence number = ((TextView)v.findViewById(com.androidproductions.ics.sms.R.id.contact_number)).getText();
        return name + " (" + number + ")";
    }
}
