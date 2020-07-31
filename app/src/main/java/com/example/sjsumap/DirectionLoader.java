package com.example.sjsumap;
import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.HashMap;
import java.util.List;
/**

public class DirectionLoader


 * Loads a list of earthquakes by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class DirectionLoader extends AsyncTaskLoader<Direction> {
    /** Tag for log messages */
    private static final String LOG_TAG = DirectionLoader.class.getName();
    /** Query URL */
    private String mUrl;    /**
     * Constructs a new {@link DirectionLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public DirectionLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
    /**
     * This is on a background thread.
     */

    @Override
    public Direction loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        // Perform the network request, parse the response, and extract a Direction object.
        Direction directions = QueryUtilsDirection.fetchDirectionData(mUrl);
        return directions;
    }
}
