package au.edu.newcastle.jnc985.visida.activity;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import instruction.InstructionHandler;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static bo.AppConstants.ACTIVITY_LOG_TAG;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NavigationBarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NavigationBarFragment extends Fragment {

    private static final String TAG = "Navbar";
    private MediaPlayer mMediaPlayer;
    private Button btnInstruction;
    private Button btnHome;
    private Button btnBack;
    public NavigationBarFragment() {
        // Required empty public constructor
    }

    public static NavigationBarFragment newInstance() {
        NavigationBarFragment fragment = new NavigationBarFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili) {
            view = inflater.inflate(R.layout.fragment_navigation_bar_audio, container, false);        }
        else{
            view = inflater.inflate(R.layout.fragment_navigation_bar, container, false);
        }
        btnHome = view.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickHome(v);
            }
        });

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBack(v);
            }
        });

        btnInstruction = view.findViewById(R.id.btnInstruction);
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        btnInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInstructionClick(v, activity);
            }
        });

        return view;
    }

    protected void onClickBack(View view){
        //Return to the parent screen (Main Menu)
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Back");
        Activity a = getActivity();
        if(!(a instanceof MainActivity)){
            a.finish();
        }
    }

    protected void onClickHome(View view){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Home");
        //If not already on main menu return to main menu.
        Activity parentActivity = getActivity();
        if(!(parentActivity instanceof MainActivity)){
            Intent intent = new Intent(getContext(), MainActivity.class);
            //Clear the stack of all activities until reach MainActivity.
            intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Activity stack cleared");
            startActivity(intent);
        }
    }

    protected void onInstructionClick(View view, AppCompatActivity a) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Instruction");
        InstructionHandler.playInstruction(a);
    }
}
