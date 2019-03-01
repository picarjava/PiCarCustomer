package com.example.piCarCustomer;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.compat.AutocompleteFilter;
import com.google.android.libraries.places.compat.AutocompletePrediction;
import com.google.android.libraries.places.compat.PlaceBufferResponse;
import com.google.android.libraries.places.compat.Places;

public class LocationInputFragment extends Fragment {
    private final static String TAG = "LocationFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_loction_input, container, false);

        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                                                                      .setCountry("TW")
                                                                      .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT | AutocompleteFilter.TYPE_FILTER_ADDRESS)
                                                                      .build();
        final PlaceAutocompleteAdapter adapter = new PlaceAutocompleteAdapter(getActivity(), Places.getGeoDataClient(getActivity()), null, autocompleteFilter);
        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final AutocompletePrediction item = adapter.getItem(position);
                final String placeId = String.valueOf(item.getPlaceId());
                Log.i(TAG, "Autocomplete item selected: " + item.getFullText(new StyleSpan(Typeface.BOLD)));
                Places.getGeoDataClient(getActivity()).getPlaceById(placeId)
                                                      .addOnSuccessListener(new OnSuccessListener<PlaceBufferResponse>() {
                            @Override
                            public void onSuccess(PlaceBufferResponse places) {
                                // close keyboard
                                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                                // call back
                                MapFragment mapFragment = (MapFragment) getActivity().getSupportFragmentManager()
                                                                                     .findFragmentByTag("Map");
                                if (mapFragment != null)
                                    mapFragment.onPlaceInputCallBack(places.get(0));
                                // pop LocationInputFragment from backStack
                                getActivity().getSupportFragmentManager()
                                             .popBackStack();
                            }
                        });

                Log.i(TAG, "Called getPlaceById to get Place details for " + item.getPlaceId());
            }
        });
        return view;
    }


}
