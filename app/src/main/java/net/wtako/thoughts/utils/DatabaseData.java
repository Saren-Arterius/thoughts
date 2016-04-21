package net.wtako.thoughts.utils;

import android.content.Context;

import net.wtako.thoughts.Thoughts;
import net.wtako.thoughts.interfaces.IHasID;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseData<T> {

    private static final long SAVE_INTERVAL = 15 * 1000L;
    private final Map<Integer, Integer> mIdIndexMap = new HashMap<>();
    private final String mKey;
    private final Type mType;
    private final Context mCtx;
    private List<T> mSavedData;
    private boolean mIdIndexValid;
    private long mLastSave;

    public DatabaseData(Context ctx, String key, Type type) {
        mCtx = ctx;
        mKey = key;
        mType = type;
    }

    public void idIndexInvalidate() {
        mIdIndexValid = false;
    }

    public boolean hasIntegerID(T item) {
        return item instanceof IHasID || item instanceof Integer;
    }

    public int getIntegerID(T item) {
        if (item instanceof Integer) {
            return (Integer) item;
        }
        if (item instanceof IHasID) {
            return ((IHasID) item).getID();
        }
        return -1;
    }

    public synchronized T getSavedItem(T item) {
        if (item == null) {
            return null;
        }
        if (mSavedData == null) {
            mSavedData = getSavedData();
        }

        try {
            if (!(hasIntegerID(item) && mIdIndexValid)) {
                mIdIndexMap.clear();
                int size = mSavedData.size();
                T orig = item;
                for (int i = 0; i < size; i++) {
                    try {
                        T savedItem = mSavedData.get(i);
                        if (savedItem == null) {
                            mSavedData.remove(i--);
                            continue;
                        }
                        if (hasIntegerID(item)) {
                            mIdIndexMap.put(getIntegerID(savedItem), i);
                        } else if (savedItem.equals(item)) {
                            item = savedItem;
                            break;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return getSavedItem(orig);
                    }
                }
                mIdIndexValid = true;
            }

            if (hasIntegerID(item)) {
                Integer index = mIdIndexMap.get(getIntegerID(item));
                if (index != null) {
                    try {
                        item = mSavedData.get(index);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        idIndexInvalidate();
                        return getSavedItem(item);
                    }
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
        return item;
    }

    public synchronized List<T> getSavedData() {
        if (mSavedData != null) {
            return mSavedData;
        }
        String json = Thoughts.getSP(mCtx).getString(mKey, null);
        if (json != null) {
            mSavedData = Thoughts.sGson.fromJson(json, mType);
        } else {
            mSavedData = new ArrayList<>();
        }
        mSavedData = Collections.synchronizedList(mSavedData);
        return mSavedData;
    }

    public synchronized void save(boolean force) {
        if (mSavedData != null && (force || System.currentTimeMillis() - mLastSave > SAVE_INTERVAL)) {
            doSave();
        }
    }

    private synchronized void doSave() {
        if (mSavedData == null) {
            mSavedData = getSavedData();
        }
        try {
            /*
            if (dataSizeLimit != null) {
                mSavedData = mSavedData.subList(0, mSavedData.size() < dataSizeLimit ? mSavedData.size() : dataSizeLimit);
            }
            */
            String json = Thoughts.sGson.toJson(new ArrayList<>(mSavedData));
            Thoughts.getSP(mCtx).edit().putString(mKey, json).apply();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        mLastSave = System.currentTimeMillis();
    }

    public synchronized boolean contains(T item) {
        if (item == null || item instanceof Integer && (Integer) item < 0) {
            return false;
        }
        if (hasIntegerID(item)) {
            return getSavedItem(item) != item;
        }
        return getSavedData().contains(item);
    }

    public synchronized DatabaseData<T> addUnique(T item) {
        try {
            if (mSavedData == null) {
                mSavedData = getSavedData();
            }
            if (!contains(item)) {
                if (hasIntegerID(item) && mIdIndexValid) {
                    mIdIndexMap.put(getIntegerID(item), mSavedData.size());
                }
                mSavedData.add(item);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public synchronized DatabaseData<T> update(T item) {
        try {
            if (mSavedData == null) {
                mSavedData = getSavedData();
            }
            boolean invalidate = true;
            if (hasIntegerID(item) && mIdIndexValid) {
                Integer index = mIdIndexMap.get(getIntegerID(item));
                if (index != null) {
                    T savedItem = mSavedData.get(index);
                    if (savedItem.equals(item)) {
                        mSavedData.set(index, item);
                        invalidate = false;
                    }
                } else {
                    invalidate = false;
                }
            }
            if (invalidate) {
                mIdIndexMap.clear();
                int size = mSavedData.size();
                for (int i = 0; i < size; i++) {
                    try {
                        T savedItem = mSavedData.get(i);
                        if (savedItem == null) {
                            mSavedData.remove(i--);
                            idIndexInvalidate();
                            continue;
                        }
                        if (hasIntegerID(item)) {
                            mIdIndexMap.put(getIntegerID(item), i);
                        }
                        if (savedItem.equals(item)) {
                            mSavedData.set(i, item);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                mIdIndexValid = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public synchronized DatabaseData<T> equalObjectPrependOrBringToFront(T item) {
        try {
            if (mSavedData == null) {
                mSavedData = getSavedData();
            }
            T saved = getSavedItem(item);
            mSavedData.remove(item);
            mSavedData.add(0, saved);
            idIndexInvalidate();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public synchronized DatabaseData<T> prependOrBringToFront(T item) {
        try {
            if (mSavedData == null) {
                mSavedData = getSavedData();
            }
            mSavedData.remove(item);
            mSavedData.add(0, item);
            idIndexInvalidate();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

}