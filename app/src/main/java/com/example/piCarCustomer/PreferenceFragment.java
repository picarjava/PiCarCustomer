package com.example.piCarCustomer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

public class PreferenceFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_preference, container, false);
        final SharedPreferences preferences = getContext().getSharedPreferences(Util.preference, Context.MODE_PRIVATE);
        final Switch smoke = view.findViewById(R.id.smoke);
        final Switch pet = view.findViewById(R.id.pet);
        final Switch babySeat = view.findViewById(R.id.babySeat);
        smoke.setChecked(preferences.getBoolean("smoke", false));
        pet.setChecked(preferences.getBoolean("pet", false));
        babySeat.setChecked(preferences.getBoolean("babySeat", false));
        Button btnSubmit = view.findViewById(R.id.prefSubmit);
        Button btnReset = view.findViewById(R.id.prefReset);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit()
                        .putBoolean("smoke", smoke.isChecked())
                        .putBoolean("pet", pet.isChecked())
                        .putBoolean("babySeat", babySeat.isChecked())
                        .apply();
                getActivity().getSupportFragmentManager()
                             .popBackStack();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit()
                           .putBoolean("smoke", false)
                           .putBoolean("pet", false)
                           .putBoolean("babySeat", false)
                           .apply();
                smoke.setChecked(false);
                pet.setChecked(false);
                babySeat.setChecked(false);
            }
        });

        return view;
    }
}
