package net.example.apstudent.roboticsexplorer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This is a general utility class for any piece of code I found useful and might want to use in
 * various places throughout the project
 **/
public final class Utils {
    private Utils() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Utils cannot be instantiated.");
        // I don't quite know how you'd get here but I also don't want you to try
    }

    /**
     * Checks for connection in a given context
     *
     * @param context The context this is being run in; usually `this`
     * @return true if connection is established, else false.
     */
    public static boolean hasConnection(Context context) {
        // Stolen from https://developer.android.com/training/monitoring-device-state/connectivity-status-type#DetermineConnection
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
