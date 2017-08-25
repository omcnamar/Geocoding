package com.olegsagenadatrytwo.geocoding;

import android.content.Context;

/**
 * Created by omcna on 8/25/2017.
 */

public interface BasePresenter<V extends BaseView> {
    void attachView(V view);
    void removeView();
    void setContext(Context context);
}
