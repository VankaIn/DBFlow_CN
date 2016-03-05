package com.github.vankain.dbflowdemo.db.models_gettingstart;

import com.github.vankain.dbflowdemo.db.ColonyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

/**
 * Created by Administrator on 2016/3/4.
 */
@Table(database = ColonyDatabase.class)
public class Ant extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String type;

    @Column
    public boolean isMale;

    @ForeignKey(saveForeignKeyModel = false)
    public ForeignKeyContainer<Queen> queenForeignKeyContainer;

    /**
     * Example of setting the model for the queen.
     */
    public void associateQueen(Queen queen) {
        queenForeignKeyContainer = FlowManager.getContainerAdapter(Queen.class).toForeignKeyContainer(queen);
    }

    @Override
    public String toString() {
        return "id:" + id + ";";
    }
}