package net.wtako.thoughts.data;

import android.content.Context;
import android.support.annotation.StringRes;

import net.wtako.thoughts.R;
import net.wtako.thoughts.interfaces.IHasStringRes;

public enum TimeSpan implements IHasStringRes {
    D(R.string.today),
    W(R.string.this_week),
    M(R.string.this_month),
    Y(R.string.this_year),
    A(R.string.all_time);

    private final int mStringRes;

    TimeSpan(@StringRes int stringRes) {
        mStringRes = stringRes;
    }

    @Override
    public String getString(Context ctx) {
        return ctx.getString(mStringRes);
    }
}