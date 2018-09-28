package com.example.testcase.dragview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.example.testcase.R;

import java.util.ArrayList;
import java.util.Random;


public class DragActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = DragActivity.class.getSimpleName();
    static Random random = new Random();
    static String[] words = "地 球 是 我 家 爱 护 靠 大 家".split(" ");
    DragGridView mDragGridView;
    Button mAddBtn, mViewBtn;
    ArrayList<String> poem = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);

        mDragGridView = ((DragGridView) findViewById(R.id.vgv));
        mAddBtn = ((Button) findViewById(R.id.add_item_btn));
        mViewBtn = ((Button) findViewById(R.id.view_poem_item));

        setListeners();
    }

    private void setListeners() {
        mDragGridView.setOnRearrangeListener(new DragGridView.OnRearrangeListener() {
            public void onRearrange(int oldIndex, int newIndex) {
                String word = poem.remove(oldIndex);
                if (oldIndex < newIndex)
                    poem.add(newIndex, word);
                else
                    poem.add(newIndex, word);
            }
        });
        mDragGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mDragGridView.removeViewAt(arg2);
                poem.remove(arg2);
            }
        });
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String word = words[random.nextInt(words.length)];
                ImageView view = new ImageView(DragActivity.this);
                view.setImageBitmap(getThumb(word));
                mDragGridView.addView(view);
                poem.add(word);
            }
        });
        mViewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String finishedPoem = "";
                for (String s : poem)
                    finishedPoem += s + " ";
                new AlertDialog.Builder(DragActivity.this).setTitle("这是你选择的")
                        .setMessage(finishedPoem).show();
            }
        });
    }

    private Bitmap getThumb(String s) {
        Bitmap bmp = Bitmap.createBitmap(150, 150, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        paint.setColor(Color.rgb(random.nextInt(128), random.nextInt(128),
                random.nextInt(128)));
        paint.setTextSize(24);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        canvas.drawRect(new Rect(0, 0, 150, 150), paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(s, 75, 75, paint);

        return bmp;
    }

    @Override
    public void onClick(View v) {

    }
}
