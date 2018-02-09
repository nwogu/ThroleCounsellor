package org.throle.throlecounsellor;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by TEST on 11/12/2017.
 */

public class PrefMan {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        Context _context;

        // shared pref mode
        int PRIVATE_MODE = 0;

        // Shared preferences file name
        private static final String PREF_NAME = "throle-username";

        private static final String IS_SESSION_START_USERNAME = "";

        public PrefMan(Context context) {
            this._context = context;
            pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = pref.edit();
        }

        public void setUSerNAme(String Username) {
            editor.putString(IS_SESSION_START_USERNAME, Username);
            editor.commit();
        }


        public String getUserName() {
            return pref.getString(IS_SESSION_START_USERNAME, "");
        }
}
