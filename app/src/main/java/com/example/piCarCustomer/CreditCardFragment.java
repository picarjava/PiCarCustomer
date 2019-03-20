package com.example.piCarCustomer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.piCarCustomer.task.CommonTask;
import com.google.gson.JsonObject;

public class CreditCardFragment extends Fragment {
    private final static String TAG = "CreditCardFragment";
    private MainActivity activity;
    TextView textViewCardNumber;
    TextInputLayout textInputLayout;
    EditText editTextCardNumInput;
    Button btnConfirm;
    Button btnSubmit;
    Button btnCancel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_credit_card, container, false);
        Member member = activity.memberCallBack();
        String creditCard = member.getCreditCard();
        textViewCardNumber = view.findViewById(R.id.creditCardNum);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        editTextCardNumInput = view.findViewById(R.id.creditCardNumInput);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnConfirm = view.findViewById(R.id.confirmEdit);
        btnCancel = view.findViewById(R.id.btnCancel);
        if (creditCard != null)
            textViewCardNumber.setText(creditCard);
        else
            textViewCardNumber.setText("xxxx-xxxx-xxxx-xxxx");

        btnConfirm.setOnClickListener(v -> viewToggle());
        editTextCardNumInput.addTextChangedListener(new TextWatcher() {
            private boolean isDelete;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDelete = before != 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // type number can't insert space
                int length = s.length();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(s);
                if (length == 0) {
                    if (creditCard == null)
                        stringBuilder.append("xxxx-xxxx-xxxx-xxxx");
                    else
                        stringBuilder.append(creditCard);
                } else if (length % 5 == 0 && length < 19) {
                    if (isDelete)
                        stringBuilder.deleteCharAt(length - 1);
                    else
                        stringBuilder.insert(length - 1, "-");

                    editTextCardNumInput.setText(stringBuilder.toString());
                    editTextCardNumInput.setSelection(stringBuilder.length());
                }

                textViewCardNumber.setText(stringBuilder.toString());
            }
        });
        btnSubmit.setOnClickListener(v -> {
            String cardNum = editTextCardNumInput.getText().toString();
            if (cardNum.length() == 19) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "updateCreditCard");
                jsonObject.addProperty("creditCard", cardNum);
                jsonObject.addProperty("memID", member.getMemID());
                new CommonTask().execute("/memberApi", jsonObject.toString());
                member.setCreditCard(cardNum);
                viewToggle();
            }
        });
        btnCancel.setOnClickListener(v -> {
            textViewCardNumber.setText(member.getCreditCard());
            viewToggle();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
        return view;
    }

    private void viewToggle() {
        if (btnConfirm.getVisibility() == View.VISIBLE) {
            btnConfirm.setVisibility(View.INVISIBLE);
            textInputLayout.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnConfirm.setVisibility(View.VISIBLE);
            textInputLayout.setVisibility(View.INVISIBLE);
            btnSubmit.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
        }
    }
}
