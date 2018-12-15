package ru.arlen.androidaidl;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class WriteFragment extends Fragment {
    private IActivityCallbacks mCallbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = ((MainActivity) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Нужно привести Activity к интерфейсу IActivityCallbacks");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write, container, false);

        final TextView textInput = view.findViewById(R.id.textInput);

        View send = view.findViewById(R.id.sendBtn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    mCallbacks.pressSendButton(text);
                }
            }
        });
        return view;
    }
}
