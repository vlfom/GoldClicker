package com.dreamempire.goldclicker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class GameActivity extends Activity {

    private Handler handler1 = new Handler(), handler2 = new Handler(), handlerPickaxe1 = new Handler(), handlerPickaxe2 = new Handler() ;
    private ArrayList < ImageView > mCoins = new ArrayList < ImageView > () ;
    private ArrayList < Long > mCoinsTime = new ArrayList < Long > () ;
    private long lastClearMemory = 0, coinCounter = 0, totalProduction = 0 ;
    private ArrayList < Long > itemCount, itemCost, itemProduction ;
    private ArrayList < Double > itemMultiplier ;
    private int screenWidth, screenHeight, columnCount = 0, mCoinSound, mMineSound, mFactorySound, mBankSound ;
    public static int itemsCount = 5 ;
    private int[] itemSound ;
    private SoundPool soundPool ;
    private AssetManager assetManager ;
    private TextView gameScoreText, textAddCoins ;
    private TextView[] itemTextCost, itemTextCount ;
    MyAdapter myAdapter ;
    RelativeLayout gameLayout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadInfo() ;
        loadSounds() ;
        resizeScreen() ;
        prepareHlCoin() ;
        preparePickaxes() ;
        mainThread.run() ;
        prepareMenu() ;
    }

    private boolean halfSecond = false ;
    private int saveCounter = 0 ;
    private Runnable mainThread = new Runnable()
    {
        public void run()
        {
            if( !halfSecond ) {
                coinCounter += totalProduction ;
                gameScoreText.setText( "" + coinCounter ) ;
            }
            halfSecond = !halfSecond ;

            ++saveCounter ;
            if( saveCounter > 10 ) {
                saveInfo();
                saveCounter = 0;
            }

            fallingCoins() ;
            handler1.postDelayed(this, 500);
        }
    } ;

    private void fallingCoins()
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT) ;
        params.leftMargin = (int) Math.round( Math.random() * screenWidth/3 ) + columnCount*( screenWidth/3 ) ; columnCount = ( columnCount + 1 ) % 3 ;

        Animation fall = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.coin_fall) ;

        ImageView mCoin = new ImageView(getApplicationContext()) ;
        mCoin.setLayoutParams( params ) ;
        mCoin.setImageResource( R.drawable.coin ) ;
        mCoin.setScaleType( ImageView.ScaleType.FIT_CENTER );
        mCoin.startAnimation(fall) ;

        gameLayout.addView(mCoin) ;

        long cTime = System.currentTimeMillis() ;

        mCoins.add( mCoin ) ;
        mCoinsTime.add( cTime ) ;

        if( cTime - mCoinsTime.get(0) > 2000 ) {
            gameLayout.removeView(mCoins.get(0)) ;
            mCoins.remove(0) ;
            mCoinsTime.remove(0) ;
        }

        if( cTime - lastClearMemory > 30 * 1000 ) { //clear memory
            System.gc() ;
            lastClearMemory = cTime ;
        }

        reAddMainCoin() ;
    }

    ImageView pickaxe1, pickaxe2 ; RelativeLayout.LayoutParams pickaxeParams1Top, pickaxeParams1Middle, pickaxeParams1Bottom, pickaxeParams2Top, pickaxeParams2Middle, pickaxeParams2Bottom ;
    private void preparePickaxes() {
        pickaxeParams1Top = new RelativeLayout.LayoutParams(70, 160) ;
        pickaxeParams1Top.addRule( RelativeLayout.CENTER_VERTICAL ) ; pickaxeParams1Top.addRule( RelativeLayout.ALIGN_PARENT_RIGHT ) ;
        pickaxeParams1Middle = new RelativeLayout.LayoutParams(70, 70) ;
        pickaxeParams1Middle.addRule( RelativeLayout.CENTER_VERTICAL ) ; pickaxeParams1Middle.addRule(RelativeLayout.ALIGN_PARENT_RIGHT ) ;
        pickaxeParams1Bottom = new RelativeLayout.LayoutParams(70, 160) ;
        pickaxeParams1Bottom.addRule( RelativeLayout.CENTER_VERTICAL ) ; pickaxeParams1Bottom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT ) ;

        pickaxe1 = new ImageView(getApplicationContext()) ;
        pickaxe1.setId(R.id.pickaxe1) ;
        pickaxe1.setImageResource( R.drawable.pickaxe1 ) ;
        pickaxe1.setVisibility(View.INVISIBLE) ;
        ( (RelativeLayout) findViewById(R.id.pickaxe1Layout) ).addView( pickaxe1 ) ;


        pickaxeParams2Top = new RelativeLayout.LayoutParams(70, 160) ;
        pickaxeParams2Top.addRule( RelativeLayout.CENTER_VERTICAL ) ; pickaxeParams2Top.addRule( RelativeLayout.ALIGN_PARENT_LEFT ) ;
        pickaxeParams2Middle = new RelativeLayout.LayoutParams(70, 70) ;
        pickaxeParams2Middle.addRule( RelativeLayout.CENTER_VERTICAL ) ; pickaxeParams2Middle.addRule(RelativeLayout.ALIGN_PARENT_LEFT ) ;
        pickaxeParams2Bottom = new RelativeLayout.LayoutParams(70, 160) ;
        pickaxeParams2Bottom.addRule( RelativeLayout.CENTER_VERTICAL ) ; pickaxeParams2Bottom.addRule(RelativeLayout.ALIGN_PARENT_LEFT ) ;

        pickaxe2 = new ImageView(getApplicationContext()) ;
        pickaxe2.setId( R.id.pickaxe2 ) ;
        pickaxe2.setImageResource( R.drawable.pickaxe2 ) ;
        pickaxe2.setVisibility(View.INVISIBLE) ;
        ( (RelativeLayout) findViewById(R.id.pickaxe2Layout) ).addView( pickaxe2 ) ;
    }

    private int whichPickaxe = 1, heightPickaxe = 0 ; private boolean pickaxe1Shown = false, pickaxe2Shown = false ;
    private Runnable handlePickaxe1 = new Runnable() {
        public void run() {
            pickaxe1.clearAnimation() ;
            pickaxe1Shown = false ;
            handlerPickaxe1.removeCallbacks(this) ;
        }
    } ;
    private Runnable handlePickaxe2 = new Runnable() {
        public void run() {
            pickaxe2.clearAnimation() ;
            pickaxe2Shown = false ;
            handlerPickaxe2.removeCallbacks(this) ;
        }
    } ;

    private void switchPickaxes() {
        heightPickaxe = ( heightPickaxe + 1 ) % 3 ;
        whichPickaxe = ( whichPickaxe + 1 ) % 2 ;
        if( whichPickaxe == 0 ) {
            final RotateAnimation rotateAnim ;

            double pickaxeHeight = Math.random() ;
            if( pickaxeHeight < 0.33 ) {
                pickaxeParams1Top.setMargins(0, 0, (int) Math.round(Math.random() * 20), 0 ) ;
                pickaxe1.setLayoutParams( pickaxeParams1Top ) ;
                pickaxe1.setPadding( 0, 0, 0, 100 ) ;

                rotateAnim = new RotateAnimation((float)(-30+Math.random()*-10), -10+(float)(Math.random()*10),
                        RotateAnimation.RELATIVE_TO_SELF, 0f,
                        RotateAnimation.RELATIVE_TO_SELF, 1f) ;
            }
            else if( pickaxeHeight < 0.66 ) {
                pickaxeParams1Middle.setMargins(0, 0, 50 + (int) Math.round(Math.random() * 20), 0 ) ;
                pickaxe1.setLayoutParams( pickaxeParams1Middle ) ;
                pickaxe1.setPadding( 0, 0, 0, 0 ) ;

                rotateAnim = new RotateAnimation((float)(-50+Math.random()*-10), (float)(Math.random()*10),
                        RotateAnimation.RELATIVE_TO_SELF, 0f,
                        RotateAnimation.RELATIVE_TO_SELF, 1f) ;
            }
            else {
                pickaxeParams1Bottom.setMargins(0, 0, 20 + (int) Math.round(Math.random() * 20), 0 ) ;
                pickaxe1.setLayoutParams( pickaxeParams1Bottom ) ;
                pickaxe1.setPadding( 0, 100, 0, 0 ) ;

                rotateAnim = new RotateAnimation((float)(-70+Math.random()*-10), (float)(Math.random()*10),
                        RotateAnimation.RELATIVE_TO_SELF, 0f,
                        RotateAnimation.RELATIVE_TO_SELF, 1f) ;
            }

            rotateAnim.setDuration(200) ;
            rotateAnim.setFillAfter(false) ;
            pickaxe1.startAnimation(rotateAnim) ;

            if( pickaxe1Shown )
                handlerPickaxe1.removeCallbacks(handlePickaxe1) ;
            pickaxe1Shown = true ;
            handlerPickaxe1.postDelayed(handlePickaxe1, 200) ;
        }
        else {
            final RotateAnimation rotateAnim ;

            double pickaxeHeight = Math.random() ;
            if( pickaxeHeight < 0.33 ) {
                pickaxeParams2Top.setMargins( (int) Math.round(Math.random() * 20), 0, 0, 0 ) ;
                pickaxe2.setLayoutParams( pickaxeParams2Top ) ;
                pickaxe2.setPadding( 0, 0, 0, 100 ) ;

                rotateAnim = new RotateAnimation((float)(30+Math.random()*10), 10+(float)(Math.random()*-10),
                        RotateAnimation.RELATIVE_TO_SELF, 1f,
                        RotateAnimation.RELATIVE_TO_SELF, 1f) ;
            }
            else if( pickaxeHeight < 0.66 ) {
                pickaxeParams2Middle.setMargins( 50 + (int) Math.round(Math.random() * 20), 0, 0, 0 ) ;
                pickaxe2.setLayoutParams( pickaxeParams2Middle ) ;
                pickaxe2.setPadding( 0, 0, 0, 0 ) ;

                rotateAnim = new RotateAnimation((float)(50+Math.random()*10), (float)(Math.random()*-10),
                        RotateAnimation.RELATIVE_TO_SELF, 1f,
                        RotateAnimation.RELATIVE_TO_SELF, 1f) ;
            }
            else {
                pickaxeParams2Bottom.setMargins( 20 + (int) Math.round(Math.random() * 20), 0, 0, 0 ) ;
                pickaxe2.setLayoutParams( pickaxeParams2Bottom ) ;
                pickaxe2.setPadding( 0, 100, 0, 0 ) ;

                rotateAnim = new RotateAnimation((float)(70+Math.random()*10), (float)(Math.random()*-10),
                        RotateAnimation.RELATIVE_TO_SELF, 1f,
                        RotateAnimation.RELATIVE_TO_SELF, 1f) ;
            }

            rotateAnim.setDuration(200) ;
            rotateAnim.setFillAfter(false) ;
            pickaxe2.startAnimation(rotateAnim) ;

            if( pickaxe2Shown )
                handlerPickaxe2.removeCallbacks(handlePickaxe2) ;
            pickaxe2Shown = true ;
            handlerPickaxe2.postDelayed(handlePickaxe2, 200) ;
        }
    }

    private boolean hlVisible = false ; ImageView hlCoin ;

    private Runnable makeCoinInvisible = new Runnable() {
        public void run() {
            hlCoin.setVisibility( View.INVISIBLE ) ;
            hlVisible = false ;
            handler2.removeCallbacks(this) ;
        }
    } ;

    private void reAddMainCoin() {
        RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(450, 450) ;
        mParams.addRule( RelativeLayout.CENTER_HORIZONTAL ) ; mParams.addRule( RelativeLayout.CENTER_VERTICAL ) ;

        ImageView bCoin = new ImageView(getApplicationContext()) ;
        bCoin.setId(R.id.imageCoin) ;
        bCoin.setLayoutParams(mParams) ;
        bCoin.setImageResource( R.drawable.ore_pic3 ) ;
        bCoin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    soundPool.play( mCoinSound, 1, 1, 1, 0, 1 ) ;

                    //HIGHLIGHTING//
                    if( hlVisible )
                        handler2.removeCallbacks(makeCoinInvisible) ;
                    hlCoin.setVisibility( View.VISIBLE ) ; hlVisible = true ;
                    handler2.postDelayed(makeCoinInvisible, 100) ;

                    //CHANGING SCORE//
                    ++coinCounter ;
                    gameScoreText.setText( "" + coinCounter ) ;

                    //ADDING PICKAXE//
                    switchPickaxes() ;
                }
                return true ;
            }
        }) ;

        gameLayout.removeView( findViewById(R.id.imageCoin) ) ;
        gameLayout.addView(bCoin) ;
    }

    private void resizeScreen() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth =  displayMetrics.widthPixels + 100 ;
        screenHeight =  displayMetrics.heightPixels ;

        FrameLayout gameFrameLayout = (FrameLayout) findViewById(R.id.gameFrameLayout) ;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) gameFrameLayout.getLayoutParams();
        layoutParams.width = screenWidth ; layoutParams.leftMargin = -50 ;
        gameFrameLayout.setLayoutParams(layoutParams);
    }

    private void prepareHlCoin() {
        RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(450, 450) ;
        mParams.addRule( RelativeLayout.CENTER_HORIZONTAL ) ; mParams.addRule( RelativeLayout.CENTER_VERTICAL ) ;

        hlCoin = new ImageView(getApplicationContext()) ;
        hlCoin.setId( R.id.imageHlCoin ) ;
        hlCoin.setLayoutParams( mParams ) ;
        hlCoin.setImageResource( R.drawable.hl_ore_pic3 ) ;
        hlCoin.setVisibility( View.INVISIBLE ) ;

        gameLayout.removeView( findViewById(R.id.imageHlCoin) ) ;
        gameLayout.addView(hlCoin) ;
    }

    @Override
    public void onDestroy() {
        saveInfo() ;

        super.onDestroy();
        android.os.Debug.stopMethodTracing();
    }

    @Override
    public void onPause() {
        saveInfo() ;

        super.onPause();  // Always call the superclass method first
    }

    private void loadInfo() {
        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "GoldClicker.files");
            if (!newFolder.exists())
                newFolder.mkdir();
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream( newFolder + "/myItems.list" )) ;
            itemCount = (ArrayList) objectInputStream.readObject() ;
            itemCost = (ArrayList) objectInputStream.readObject() ;
            itemProduction = (ArrayList) objectInputStream.readObject() ;
            itemMultiplier = (ArrayList) objectInputStream.readObject() ;
            coinCounter = (long) objectInputStream.readObject() ;
        } catch (Exception ex) {
            setDefault() ;
        }
        saveInfo() ;

        gameScoreText = (TextView) findViewById(R.id.gameScore) ;
        gameScoreText.setText( "" + coinCounter ) ;
        totalProduction = 0 ;
        for( int i = 0 ; i < itemsCount ; ++i )
            totalProduction += itemProduction.get(i) * itemCount.get(i) ;
        textAddCoins = (TextView) findViewById(R.id.textAddCoins) ;
        textAddCoins.setText( "+" + totalProduction ) ;

        ((Button)findViewById(R.id.buttonShop)).setOnTouchListener(new myTouchListener()) ;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build() ; //использую Android-Universal-Image-Loader
        ImageLoader.getInstance().init(config);

        gameLayout = (RelativeLayout) findViewById(R.id.gameLayout) ;
    }

    private void saveInfo() {
        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "GoldClicker.files");
            if (!newFolder.exists())
                newFolder.mkdir();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream( newFolder + "/myItems.list" )) ;
            objectOutputStream.writeObject( itemCount ) ;
            objectOutputStream.writeObject( itemCost ) ;
            objectOutputStream.writeObject( itemProduction ) ;
            objectOutputStream.writeObject( itemMultiplier ) ;
            objectOutputStream.writeObject( coinCounter ) ;
            objectOutputStream.flush();
        } catch (Exception ex) {
        }
    }

    private void setDefault() {
        itemCount = MainActivity.itemCount ;
        itemCost  = MainActivity.itemCost ;
        itemProduction = MainActivity.itemProduction ;
        itemMultiplier = MainActivity.itemMultiplier ;
        coinCounter = 0 ;
    }

    private void loadSounds() {
        soundPool = new SoundPool( 3, AudioManager.STREAM_MUSIC, 0 ) ;
        assetManager = getAssets() ;

        itemSound = new int[itemsCount+1] ;
        for( int i = 0 ; i < itemsCount ; ++i )
            itemSound[i] = loadSound( i + "_sound.mp3" ) ;

        mCoinSound = loadSound( "coin_sound.mp3" ) ;
    }

    private int loadSound( String fileName ) {
        AssetFileDescriptor assetFileDescriptor = null ;
        try {
            assetFileDescriptor = assetManager.openFd( fileName ) ;
        } catch ( IOException e ) {
            e.printStackTrace() ;
            Toast.makeText( this, "Can't load " + fileName, Toast.LENGTH_SHORT ).show() ;
            return -1 ;
        }
        return soundPool.load( assetFileDescriptor, 1 ) ;
    }

    LayoutInflater layoutInflater ;
    View popupView ;
    RelativeLayout relativeLayout ;
    RelativeLayout.LayoutParams layoutParams ;

    View popupMenuView ; PopupWindow popupWindow ; TextView popupTextTitle, popupTextDescription ; ImageView popupImage ;

    private void prepareMenu() {
        popupMenuView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.popup_layout, ((RelativeLayout) this.findViewById(R.id.popup_layout))) ;
        popupWindow = new PopupWindow(popupMenuView) ;

        myAdapter = new MyAdapter(popupMenuView.getContext()) ;
        ((ListView) popupMenuView.findViewById(R.id.popupListView)).setAdapter(myAdapter) ;
    }

    private boolean show = true ;
    private class myTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if( view.getId() == R.id.buttonShop ) {
                    if (show) {
                        popupWindow.showAtLocation(popupMenuView, Gravity.TOP, 0, 10) ;
                        popupWindow.update((int)Math.round( screenWidth * 0.8 ), (int)Math.round( screenHeight * 0.85 ));
                    } else
                        popupWindow.dismiss();
                    show = !show;
                }
            }
            return true ;
        }
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater ;

        public MyAdapter( Context context ) {
            mLayoutInflater = LayoutInflater.from( context ) ;
        }

        public int getCount() { return itemsCount ; }

        public Object getItem( int position ) {
            return position ;
        }

        public long getItemId( int position ) {
            return position ;
        }

        public String getString( int position ) {
            return "" + position ;
        }

        public View getView( final int position, View convertView, ViewGroup parent ) {

            if( convertView == null )
                convertView = mLayoutInflater.inflate( R.layout.popup_list_item, null ) ;

            ((Button) convertView.findViewById( R.id.popupItemButton )).setOnTouchListener( new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (coinCounter >= Math.round(itemCost.get(position) * Math.pow(itemMultiplier.get(position), itemCount.get(position)))) {
                        soundPool.play(itemSound[position], 1, 1, 1, 0, 1) ;
                        coinCounter -= Math.round(itemCost.get(position) * Math.pow(itemMultiplier.get(position), itemCount.get(position))) ;
                        gameScoreText.setText("" + coinCounter) ;
                        itemCount.set(position,itemCount.get(position)+1) ;
                        totalProduction += itemProduction.get(position) ;
                        textAddCoins.setText( "+" + totalProduction ) ;
                        myAdapter.notifyDataSetChanged() ;
                    }
                    return true;
                }
            });

            ((TextView) convertView.findViewById( R.id.popupItemCost )).setText("Cost: " + Math.round(itemCost.get(position) * Math.pow(itemMultiplier.get(position), itemCount.get(position)))) ;
            ((TextView) convertView.findViewById( R.id.popupItemProduction )).setText( "+"+itemProduction.get(position) ) ;
            ((TextView) convertView.findViewById( R.id.popupItemCount )).setText( "x"+itemCount.get(position) ) ;

            ImageLoader.getInstance().displayImage( "assets://item" + position + ".png", (ImageView) convertView.findViewById( R.id.popupItemIcon ) );

            return convertView ;
        }
    }
}
