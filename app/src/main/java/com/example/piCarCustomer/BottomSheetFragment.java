package com.example.piCarCustomer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
        TextView plateNum = view.findViewById(R.id.plate_num);
        TextView carType = view.findViewById(R.id.car_type);
        TextView driverName = view.findViewById(R.id.driver_name);
        Bundle bundle = getArguments();
        assert bundle != null;
        if ("Success".equals(bundle.getString("state", "Failed"))) {
            driverName.setText(bundle.getString("driverName"));
            plateNum.setText(bundle.getString("plateNum"));
            carType.setText(bundle.getString("carType"));
        } else {
            driverName.setText("");
            plateNum.setText("");
            carType.setText("目前無符合司機請稍後在試");
        }
        return view;
    }
}
