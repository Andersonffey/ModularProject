package com.Anderson.example.games.tanc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Bacon-Anderson on 11/12/2016.
 */

public class ListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    String mExplanation = "";
    int mScore = 0;
    boolean mShowSignIn = false;
    private ListView mListView;
    private String[] m_Filename;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> m_JobList;
    private ArrayAdapter<String> adapter2;
    private ArrayList<String> m_JobList2;
    private ArrayList<String> m_LevelNames;

    public interface Listener {
        public void onWinScreenDismissed();
        public void onWinScreenSignInClicked();
        public void onQuestionaryCall(String s);
        public void onButtonClick();
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Log.d("I", m_JobList.get(position));
    }





    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        ListView LV = (ListView) v.findViewById(R.id.list_questions);
        mListView = LV;
        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("I", m_Filename[i]);
                mListener.onButtonClick();
                mListener.onQuestionaryCall(m_LevelNames.get(i));
            }
        });
        m_JobList = new ArrayList<String>();
        m_JobList2 = new ArrayList<String>();
        m_LevelNames = new ArrayList<String>();
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, m_JobList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(m_JobList.get(position));
                text2.setText(m_JobList2.get(position));
                return view;
            }

        };

        try {
            m_Filename = getActivity().getAssets().list("texts");

            if (m_Filename != null && (null != m_JobList)) {

                for (int i = 0; i < m_Filename.length; i++) {


                    String[] temp = m_Filename[i].split("-");
                    if(temp.length == 4) {
                        m_LevelNames.add(m_Filename[i]);
                        m_JobList.add(temp[0]);
                        m_JobList2.add("Author: " + temp[1] + " Topic: " + temp[2] + " Mode: " + temp[3].substring(0, temp[3].length() - 5));
                    }
                }

                if ((m_Filename.length == 0) || (null == m_Filename)) {
                    Log.d("I", "3");
                    m_JobList.add(0, "No sheets available");
                }
            }
            if (adapter != null) {
                if (LV != null) {
                    if (m_JobList != null) {
                        Log.d("I", "4");
                        LV.setAdapter(adapter);
                        Log.d("I", "5");
                        adapter.notifyDataSetChanged();
                        Log.d("I", "6");
                    }
                }

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return v;
    }

    public void setFinalScore(int i) {
        mScore = i;
    }

    public void setExplanation(String s) {
        mExplanation = s;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.win_screen_sign_in_button) {

        }

    }
}