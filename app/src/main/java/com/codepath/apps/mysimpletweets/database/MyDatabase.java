package com.codepath.apps.mysimpletweets.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by knyamagoudar on 3/26/17.
 */

@Database(name = MyDatabase.NAME, version = MyDatabase.VERSION)
public class MyDatabase {
    public static final String NAME = "MyTweetDataBase";

    public static final int VERSION = 1;
}
