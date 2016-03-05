package com.github.vankain.dbflowdemo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.vankain.dbflowdemo.R;
import com.github.vankain.dbflowdemo.db.models_gettingstart.Ant;
import com.github.vankain.dbflowdemo.db.models_gettingstart.Colony;
import com.github.vankain.dbflowdemo.db.models_gettingstart.Queen;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/3/4.
 */
public class GettingStartActivity extends Activity {
    public static final String TAG = "GettingStartActivity";
    public static void launch(Context context) {
        Intent intent = new Intent(context, GettingStartActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_start);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_insert_colony, R.id.btn_read_add_delete_colony,
            R.id.btn_insert_queen, R.id.btn_read_queen, R.id.btn_insert_ants,
    R.id.btn_read_ants})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.btn_insert_colony:
                insertColony();
                break;
            case R.id.btn_read_add_delete_colony:
                readColony();
                break;
            case R.id.btn_insert_queen:
                insertQueen();
                break;
            case R.id.btn_read_queen:
                readQueen();
                break;
            case R.id.btn_insert_ants:
                addInsertAdds();
                break;
            case R.id.btn_read_ants:
                readAntsByQueen();
                break;
        }
    }

    private void insertColony() {
        Colony colony = new Colony();
        colony.name = "colony1";
        colony.insert();
    }

    private void readColony() {
        Colony colony = SQLite.select().from(Colony.class).querySingle();
        Toast.makeText(GettingStartActivity.this, "id:"
                + colony.id + "\nname:"
                + colony.name, Toast.LENGTH_LONG)
                .show();
    }

    private void insertQueen() {
        Colony colony = new Colony();
        colony.name = "queen's colony";
        colony.insert();
        Queen quean = new Queen();
        quean.name = "queen1";
        quean.colony = colony;
        quean.insert();
    }

    private void readQueen() {
        Queen queen = SQLite.select().from(Queen.class).querySingle();
        Toast.makeText(GettingStartActivity.this,
                queen.toString(),
                Toast.LENGTH_LONG).show();
    }



    private void addInsertAdds(){
        Toast.makeText(GettingStartActivity.this, "inserting", Toast.LENGTH_SHORT).show();
        long startTime = System.currentTimeMillis();
        Queen queen = SQLite.select().from(Queen.class).querySingle();
        for(int i=0; i<1000; i++){
            Ant ant = new Ant();
            ant.type = "other";
            ant.isMale = true;
            ant.associateQueen(queen);
            ant.insert();
        }
        long endTime = System.currentTimeMillis();
        Toast.makeText(GettingStartActivity.this, "insertsuccess", Toast.LENGTH_SHORT).show();
        Log.i("TAG", (endTime-startTime) + "");
    }

    private void readAntsByQueen(){
        long startTime = System.currentTimeMillis();
        Queen queen = SQLite.select().from(Queen.class).querySingle();
        List<Ant> ants = queen.getMyAnts();
        long endTime = System.currentTimeMillis();
        Log.i("TAG", (endTime-startTime) + "");
        String s = "";
        for(int i=0; i<ants.size(); i++){
            s += ants.get(i).toString();
        }
        Toast.makeText(GettingStartActivity.this, s, Toast.LENGTH_SHORT).show();

    }
}
