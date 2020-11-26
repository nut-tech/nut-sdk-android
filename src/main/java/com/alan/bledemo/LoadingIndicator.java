package com.alan.bledemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hanbing on 15/7/15.
 */
public class LoadingIndicator extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap bmpBg;

    private Bitmap bmpFg;

    private int clipH;

    private int outsetValue = 30;

    private int processValue = outsetValue;

    public LoadingIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        bmpBg = BitmapFactory.decodeResource(getResources(), R.drawable.img_loading_nut);
        bmpFg = BitmapFactory.decodeResource(getResources(), R.drawable.img_loading_nut_top);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.drawBitmap(bmpBg, -bmpBg.getWidth() / 2, -bmpBg.getHeight() / 2, paint);
        canvas.clipRect(-bmpFg.getWidth() / 2, bmpFg.getHeight() / 2 - clipH, bmpFg.getWidth() / 2,
                bmpFg.getHeight() / 2);

        canvas.drawBitmap(bmpFg, -bmpFg.getWidth() / 2, -bmpFg.getHeight() / 2, paint);
        clipH = processValue * bmpFg.getHeight() / 100;
        processValue++;
        if (processValue > 70) {
            processValue = outsetValue;
        }
        postInvalidateDelayed(25);
    }
}
