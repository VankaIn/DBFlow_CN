package com.github.vankain.dbflowdemo.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Administrator on 2016/3/4.
 */
@Database(name = ColonyDatabase.NAME, version = ColonyDatabase.VERSION)
public class ColonyDatabase {

    public static final String NAME = "Colonies";

    public static final int VERSION = 1;
}
