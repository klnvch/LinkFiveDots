package by.klnvch.link5dots.online;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import by.klnvch.link5dots.R;

public class OnlineWaitingFragment extends Fragment {

    public static final String TAG = "OnlineWaitingFragment";


    public OnlineWaitingFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online_waiting, container, false);
    }

}
