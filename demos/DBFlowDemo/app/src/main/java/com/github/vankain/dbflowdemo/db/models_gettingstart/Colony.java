package com.github.vankain.dbflowdemo.db.models_gettingstart;

import com.github.vankain.dbflowdemo.db.ColonyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Administrator on 2016/3/4.
 */
@Table(database = ColonyDatabase.class)
public class Colony extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String name;

    @Override
    public String toString() {
        return "id:"
                + id
                + "\nname:"
                + name;
    }
}