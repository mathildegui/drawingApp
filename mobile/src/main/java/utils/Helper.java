package utils;

import android.app.Activity;

import butterknife.ButterKnife;

/**
 * Created by mathilde on 09/11/2015.
 */
public class Helper {

    /**
     *
     * @param a
     */
    public static void init(Activity a) {
        ButterKnife.bind(a);
    }
}
