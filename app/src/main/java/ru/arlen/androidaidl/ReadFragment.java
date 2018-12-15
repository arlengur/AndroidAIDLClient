package ru.arlen.androidaidl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static ru.arlen.androidaidl.MainActivity.TEXT_ARG;

public class ReadFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String text = bundle.getString(TEXT_ARG);
            TextView textOutput = view.findViewById(R.id.textOutput);
            textOutput.setText(text);
        }
        return view;
    }
}
