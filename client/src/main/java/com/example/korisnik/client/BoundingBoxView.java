package com.example.korisnik.camera2_primer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Korisnik on 2017-09-26.
 */
public class BoundingBoxView extends View {

    private Paint paint;
    private Boolean rezultati_gotovi = false;
    private ArrayList<KategorijaLokacija> Rezultati = new ArrayList<>();

    public BoundingBoxView( final Context context, final AttributeSet set){

        super(context,set);

    }

    public ArrayList<KategorijaLokacija> getRezultati(){
        return  Rezultati;
    }

    public void setResults(final String rezultat) {

        String[] rez = rezultat.split(":");

        //Toast.makeText(getApplicationContext(),"rez.length " + String.valueOf(rez.length), Toast.LENGTH_LONG).show();
       // Log.i("getPicture_fnc_bb", "setresults");

        for (String elem : rez) {
            String[] rezultat_klase = elem.split(",");
            if(rezultat_klase.length == 5) {
                // ubaciti proveru da neki string iz rezultata nije definisan
                Rezultati.add(new KategorijaLokacija(rezultat_klase[0], Integer.parseInt(rezultat_klase[1].trim()), Integer.parseInt(rezultat_klase[2].trim()),
                        Integer.parseInt(rezultat_klase[3].trim()), Integer.parseInt(rezultat_klase[4].trim())));
            }
        }

       // Log.i("getPicture_fnc_bb", "rez.length " + String.valueOf(rez.length));

        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {

        float view_height_temp = (float) this.getHeight();
        float view_width_temp = (float) this.getWidth();


       // float view_height = Math.max(view_height_temp, view_width_temp);
       // float view_width = Math.min(view_height_temp, view_width_temp);

        /*String prediction_string = "width: " + Float.toString(view_width) +
                " height: " + Float.toString(view_height);
        Log.v("BoundingBox", prediction_string);*/
       /* String prediction_string = "width: " + Float.toString(view_width_temp) +
                " height: " + Float.toString(view_height_temp);
        Log.v("BoundingBox", prediction_string);  radi w = 656, h = 958*/

        //velicina poslate slike 240x320 ,definisati konstante

        float x_scale = view_width_temp/240;
        float y_scale = view_height_temp/320;


        paint = new Paint();
        paint.setColor(0xff00ff00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        if (Rezultati.size() > 0) {
            for (KategorijaLokacija elem : Rezultati) {

                RectF boundingBox = new RectF(elem.left*x_scale, elem.top*y_scale, elem.right*x_scale, elem.bot*y_scale);
                //if(rezultati_gotovi){
                canvas.drawRect(boundingBox, paint);
                // }
            }
        }
    }
}
