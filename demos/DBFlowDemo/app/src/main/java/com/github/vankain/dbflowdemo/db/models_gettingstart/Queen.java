package com.github.vankain.dbflowdemo.db.models_gettingstart;

import com.github.vankain.dbflowdemo.db.ColonyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Created by Administrator on 2016/3/4.
 */
@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String name;

    @Column
    @ForeignKey(saveForeignKeyModel = false)
    public Colony colony;

    // needs to be accessible for DELETE
    List<Ant> ants;

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "ants")
    public List<Ant> getMyAnts() {
        if (ants == null || ants.isEmpty()) {
            ants = SQLite.select()
                    .from(Ant.class)
                    .where(Ant_Table.queenForeignKeyContainer_id.eq(id))
                    .queryList();
        }
        return ants;
    }

    @Override
    public String toString() {
        return "queen.id:" + id +
                "\nqueen.name:" + name +
                "\nqueen.colony.id:" + colony.id +
                "\nqueen.colony.name:" + colony.name;
    }
}
