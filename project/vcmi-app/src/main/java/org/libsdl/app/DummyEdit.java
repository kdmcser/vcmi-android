package org.libsdl.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.Log;
import eu.vcmi.vcmi.util.Utils;

class DummyEdit extends LinearLayout
{
    final DummyEditText mEditText;

    public DummyEdit(Context context)
    {
        super(context);

        mEditText = new DummyEditText(context);
        setBackgroundResource(R.drawable.overlay_edittext_background);
        mEditText.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mEditText);
    }

    @Override
    public boolean requestFocus(final int direction, final Rect previouslyFocusedRect)
    {
        Log.d(this, "Requesting focus");
        return mEditText.requestFocus(direction, previouslyFocusedRect);
    }

    public void notifyContentChanged(final String textContext)
    {
        mEditText.setText(textContext);
    }

    class DummyEditText extends android.support.v7.widget.AppCompatEditText implements View.OnKeyListener, View.OnTouchListener
    {
        private final Paint mPaint;
        private float mCloseXCenter = 0;
        private float mCloseYCenter = 0;
        InputConnection mInputConnection;

        public DummyEditText(final Context context)
        {
            super(context);
            setOnKeyListener(this);
            setOnTouchListener(this);
            mPaint = new Paint();
            mPaint.setColor(ContextCompat.getColor(context, R.color.accent));
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(5);
        }

        @Override
        public boolean onCheckIsTextEditor()
        {
            return false;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            Log.d(this, "xx# " + keyCode + "; " + event.getAction());
            // This handles the hardware keyboard input
            if (event.isPrintingKey() || keyCode == KeyEvent.KEYCODE_SPACE)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    mInputConnection.commitText(String.valueOf((char) event.getUnicodeChar()), 1);
                }
                return false;
            }

            return SDLActivity.mHolder.surface().onKey(v, keyCode, event);
        }

        //
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event)
        {
            Log.d(this, "xx#pre " + keyCode + "; " + event.getAction());
            // As seen on StackOverflow: http://stackoverflow.com/questions/7634346/keyboard-hide-event
            // FIXME: Discussion at http://bugzilla.libsdl.org/show_bug.cgi?id=1639
            // FIXME: This is not a 100% effective solution to the problem of detecting if the keyboard is showing or not
            // FIXME: A more effective solution would be to assume our Layout to be RelativeLayout or LinearLayout
            // FIXME: And determine the keyboard presence doing this: http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
            // FIXME: An even more effective way would be if Android provided this out of the box, but where would the fun be in that :)
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK)
            {
                final View edit = SDLActivity.mHolder.edit();
                if (edit != null && edit.getVisibility() == View.VISIBLE)
                {
                    SDLActivity.onNativeKeyboardFocusLost();
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs)
        {
            mInputConnection = new SDLInputConnection(this, true);

            outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                                  | 33554432 /* API 11: EditorInfo.IME_FLAG_NO_FULLSCREEN */;

            return mInputConnection;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event)
        {
            if (event.getX() >= mCloseXCenter - Utils.convertDpToPx(getContext(), 30f))
            {
                DummyEdit.this.setVisibility(View.GONE);
            } else {
                final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(this, 0);
            }
            return false;
        }

        @Override
        protected void onDraw(final Canvas canvas)
        {
            super.onDraw(canvas);
            mCloseXCenter = getWidth() - Utils.convertDpToPx(getContext(), 30f);
            mCloseYCenter = getHeight() / 2.0f;
            canvas.translate(mCloseXCenter , mCloseYCenter);
            final int count = 4;
            final int angle = (int) (360f / count);
            canvas.save();
            canvas.rotate(45);

            for (int i = 0; i < count; i++) { canvas.rotate(angle * i);
                canvas.drawLine(0, 0, Utils.convertDpToPx(getContext(), 10f), 0, mPaint);
            }
            canvas.restore();
        }

    }
}
