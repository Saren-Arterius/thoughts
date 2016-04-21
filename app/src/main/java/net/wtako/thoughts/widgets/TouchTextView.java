package net.wtako.thoughts.widgets;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import net.wtako.thoughts.handlers.ImageGetter;
import net.wtako.thoughts.utils.MiscUtils;

public class TouchTextView extends TextView {

    boolean mLinkHit;

    public TouchTextView(Context context) {
        super(context);
    }

    public TouchTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mLinkHit = false;
        try {
            super.onTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mLinkHit;
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;


        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();

            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else {
                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                    }
                    if (widget instanceof TouchTextView) {
                        ((TouchTextView) widget).mLinkHit = true;
                    }
                    return true;
                }

                ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);

                if (image.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        if (image[0].getDrawable() instanceof ImageGetter.UrlImageDownloader) {
                            MiscUtils.openURL(widget.getContext(),
                                    ((ImageGetter.UrlImageDownloader) image[0].getDrawable()).getSource());
                        }
                    } else {
                        Selection.setSelection(buffer, buffer.getSpanStart(image[0]), buffer.getSpanEnd(image[0]));
                    }
                    if (widget instanceof TouchTextView) {
                        ((TouchTextView) widget).mLinkHit = true;
                    }
                    return true;
                }

                Selection.removeSelection(buffer);
                Touch.onTouchEvent(widget, buffer, event);
                return false;
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }
}