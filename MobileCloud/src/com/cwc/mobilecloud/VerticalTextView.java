package com.cwc.mobilecloud;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;


public class VerticalTextView extends TextView {
	private static final String TAG = "LazyAdapter";
	final boolean topDown;

	public VerticalTextView (Context context) {
	    super(context);
	    final int gravity = getGravity();
	    if(Gravity.isVertical(gravity) && (gravity&Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
	       setGravity((gravity&Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.TOP);
	       topDown = false;
	    }else
	       topDown = true;
	}

	   public VerticalTextView(Context context, AttributeSet attrs){
	      super(context, attrs);
	      final int gravity = getGravity();
	      if(Gravity.isVertical(gravity) && (gravity&Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
	         setGravity((gravity&Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.TOP);
	         topDown = false;
	      }else
	         topDown = true;
	   }

	   @Override
	   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
	      super.onMeasure(heightMeasureSpec, widthMeasureSpec);
	      setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	      //setMeasuredDimension(getMeasuredWidth() + getMeasuredWidth()/2, getMeasuredHeight()+ getMeasuredHeight()/2);
	   }

	   @Override
	   protected void onDraw(Canvas canvas){
	      TextPaint textPaint = getPaint(); 
	      textPaint.setColor(getCurrentTextColor());
	      textPaint.drawableState = getDrawableState();

	      canvas.save();

	      if(topDown){
	    	  canvas.translate(0, getHeight());
	         //canvas.translate(getWidth(), 0);
	         canvas.rotate(-90);
	    	  
	         //Log.d(TAG, "topDown");
	      }else {
	         canvas.translate(0, getHeight());
	         canvas.rotate(-90);
	         //Log.d(TAG, "No topDown");
	      }

	      canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

	      getLayout().draw(canvas);
	      canvas.restore();
	  }
}
