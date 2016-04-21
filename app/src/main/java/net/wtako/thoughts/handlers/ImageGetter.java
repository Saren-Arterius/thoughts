package net.wtako.thoughts.handlers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import in.uncod.android.bypass.Bypass;

public class ImageGetter implements Bypass.ImageGetter {

    private final Context mCtx;
    private final TextView mContent;
    private final Handler mHandler = new Handler();

    public ImageGetter(TextView content) {
        mCtx = content.getContext();
        mContent = content;
    }

    @Override
    public Drawable getDrawable(String source) {
        final UrlImageDownloader downloader = new UrlImageDownloader(mCtx.getResources(), source);
        Glide.with(mCtx).load(source).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(final GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                mContent.post(new Runnable() {
                    @Override
                    public void run() {
                        int wBound = resource.getIntrinsicWidth();
                        int hBound = resource.getIntrinsicHeight();
                        int maxWidth = mContent.getWidth();
                        if (wBound > maxWidth) {
                            float ratio = (float) maxWidth / (float) wBound;
                            wBound = maxWidth;
                            hBound *= ratio;
                        }
                        resource.setBounds(0, 0, wBound, hBound);
                        downloader.setBounds(resource.getBounds());
                        downloader.drawable = resource;
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContent.setText(mContent.getText());
                            }
                        }, 50L);
                    }
                });
            }
        });
        return downloader;
    }

    public static class UrlImageDownloader extends BitmapDrawable {

        private final String mSource;
        public Drawable drawable;

        public UrlImageDownloader(Resources res, String source) {
            super(res);
            mSource = source;
        }

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            try {
                if (drawable != null) {
                    drawable.draw(canvas);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        public String getSource() {
            return mSource;
        }
    }
}
