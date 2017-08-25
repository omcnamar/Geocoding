package com.olegsagenadatrytwo.geocoding.view.mapsactivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.olegsagenadatrytwo.geocoding.BasePresenter;
import com.olegsagenadatrytwo.geocoding.BaseView;

/**
 * Created by omcna on 8/25/2017.
 */

public interface MapsActivityContract {

    interface View extends BaseView {

        void mapShowed(boolean isSaved);
        void addressReceivedReadyToUpdate(final LatLng searchedLocation, GoogleMap map);
    }

    interface Presenter extends BasePresenter<View> {

        void showMap(SupportMapFragment mapFragmentIn);
        void submitAddress(String address);
    }
}
