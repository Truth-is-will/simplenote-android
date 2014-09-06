package com.automattic.simplenote.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.automattic.simplenote.NoteListFragment.NotesCursorAdapter;
import com.automattic.simplenote.NotesActivity;
import com.automattic.simplenote.Simplenote;
import com.automattic.simplenote.models.Note;
import com.automattic.simplenote.models.Tag;
import com.automattic.simplenote.utils.SearchTokenizer;
import com.automattic.simplenote.utils.TagsAdapter;
import com.automattic.simplenote.utils.TagsAdapter.TagMenuItem;
import com.simperium.client.Bucket;
import com.simperium.client.Query;

/**
 * Created by richard on 8/30/14.
 */
public class SimpleNoteWidgetProvider extends AppWidgetProvider{

    public static final String PREF_WIDGET_NOTE = "PREF_WIDGET_NOTE";

    private static final String TAG = "WidgetProvider";
    protected Bucket<Note> mNotesBucket;
    protected Bucket<Tag> mTagsBucket;
    private TagsAdapter.TagMenuItem mAllNotesItem;
    private TagsAdapter mTagsAdapter;
    private Bucket.ObjectCursor<Note> mNoteCursor;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive: intent " + intent.getAction().toString());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "onUpdate");


        Simplenote currentApp = (Simplenote) context.getApplicationContext();

        if (mNotesBucket == null) {
            mNotesBucket = currentApp.getNotesBucket();
        }

        if (mNotesBucket.count() == 0){
            // do nothing.
            Log.i(TAG, "No notes available.");
            return;
        }

        if (mTagsBucket == null) {
            mTagsBucket = currentApp.getTagsBucket();
        }

        mTagsAdapter = new TagsAdapter(context, mNotesBucket);

        mNoteCursor = mNotesBucket.allObjects();

        // see if there's a note saved
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String currentNoteKey = prefs.getString(PREF_WIDGET_NOTE, null);


        if (currentNoteKey == null){

           selectFirstNote(prefs);

        } else {

            Log.i(TAG, "Found stored key " + currentNoteKey);
            mNoteCursor.moveToFirst();
            boolean found = false;
            while (!mNoteCursor.isAfterLast()){
                if (mNoteCursor.getObject().getSimperiumKey().equals(currentNoteKey)){
                    found = true;
                    break;
                }
                mNoteCursor.moveToNext();
            }

            if (!found){

                selectFirstNote(prefs);
                Log.i(TAG, currentNoteKey + " not found. set to first: "
                        + mNoteCursor.getObject().getSimperiumKey());
            } else {
                Log.i(TAG, "Note set to " + currentNoteKey);
            }


        }

        Log.i(TAG, "Found " + mTagsAdapter.getCount() + " tags items.");
        if (mTagsAdapter.getCount() > 0){

            TagsAdapter.TagMenuItem tmi = mTagsAdapter.getDefaultItem();
            Log.i(TAG, "Default tag item is : '" + tmi.name + "'");

        }

        Log.i(TAG, "Found " + mNotesBucket.count() + " notes");


    }

    /**
     * Selects the first note and saves its key to SharedPreferences.
     * @param prefs the shared preferences to save to.
     */
    private void selectFirstNote(SharedPreferences prefs){

        mNoteCursor.moveToFirst();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_WIDGET_NOTE, mNoteCursor.getObject().getSimperiumKey());
        editor.commit();

        Log.i(TAG, "Fetched first note: " + mNoteCursor.getObject().getSimperiumKey());
    }




    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i(TAG, "onDeleted");

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(TAG, "onEnabled");

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(TAG, "onDisabled");

    }



}
