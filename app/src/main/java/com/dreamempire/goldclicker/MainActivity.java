package com.dreamempire.goldclicker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity {

    public static ArrayList < Long > itemCount, itemCost, itemProduction ;
    public static ArrayList < Double > itemMultiplier ;

    private class myTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (view.getId() == R.id.btnNew) {
                    resetGame() ;
                    startActivity(new Intent(MainActivity.this, GameActivity.class)) ;
                }
                else if (view.getId() == R.id.btnContinue)
                    startActivity(new Intent(MainActivity.this, GameActivity.class)) ;
                else if (view.getId() == R.id.btnAbout)
                    startActivity(new Intent(MainActivity.this, AboutActivity.class)) ;
            }
            return true ;
        }
    }

    private void resetGame() {
        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "GoldClicker.files");
            if (!newFolder.exists())
                newFolder.mkdir();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream( newFolder + "/myItems.list" )) ;
            objectOutputStream.writeObject( itemCount ) ;
            objectOutputStream.writeObject( itemCost ) ;
            objectOutputStream.writeObject( itemProduction ) ;
            objectOutputStream.writeObject( itemMultiplier ) ;
            objectOutputStream.writeObject( (long)0 ) ;
            objectOutputStream.flush();
        } catch (Exception ex) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState ) ;
        setContentView( R.layout.activity_main ) ;

        ((Button) findViewById(R.id.btnNew)).setOnTouchListener( new myTouchListener() ) ;
        ((Button) findViewById(R.id.btnContinue)).setOnTouchListener( new myTouchListener() ) ;
        ((Button) findViewById(R.id.btnAbout)).setOnTouchListener( new myTouchListener() ) ;

        itemCount = new ArrayList < Long > ( Arrays.asList((long)0, (long)0, (long)0, (long)0, (long)0) ) ;
        itemCost  = new ArrayList < Long > ( Arrays.asList((long)50, (long)200, (long)1000, (long)5000, (long)20000) ) ;
        itemProduction = new ArrayList < Long > ( Arrays.asList((long)2, (long)4, (long)10, (long)20, (long)50) )  ;
        itemMultiplier = new ArrayList < Double > ( Arrays.asList(1.6, 1.55, 1.5, 1.45, 1.4) ) ;
    }

}
