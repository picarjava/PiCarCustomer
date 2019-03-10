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

import com.google.gson.JsonObject;

public class PreferenceFragment extends Fragment {
    private MemberCallBack memberCallBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        memberCallBack = (MemberCallBack) context;
    }

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
                boolean havePet = pet.isChecked();
                boolean canSmoke = smoke.isChecked();
                boolean haveBabySeat = babySeat.isChecked();
                Member member = memberCallBack.memberCallBack();
                preferences.edit()
                           .putBoolean("smoke", canSmoke)
                           .putBoolean("pet", havePet)
                           .putBoolean("babySeat", haveBabySeat)
                           .apply();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "updatePreference");
                jsonObject.addProperty("pet", havePet? 1: 0);
                jsonObject.addProperty("smoke", canSmoke? 1: 0);
                jsonObject.addProperty("babySeat", haveBabySeat? 1: 0);
                jsonObject.addProperty("memID", member.getMemID());
                new JsonTask().execute("/memberApi", jsonObject.toString());
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
