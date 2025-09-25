package recordverification;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.NavigationBarFragment;
import bo.db.entity.HouseholdMember;

import static bo.AppConstants.NAVBAR;

public class RecordVerificationActivity extends AppCompatActivity {

    private RecordVerificationViewModel mVerificationViewModel;
    private RecordVerificationViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_verification);

        FragmentManager fm = getSupportFragmentManager();
        NavigationBarFragment navBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(navBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        //Create View Mode
        if(mVerificationViewModel == null){
            this.mVerificationViewModel = new ViewModelProvider(this).get(RecordVerificationViewModel.class);
        }

        //set up Recycler View
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        //Create adapter
        List<HouseholdMember> hms = mVerificationViewModel.getHouseholdMembers();
        mAdapter = new RecordVerificationViewAdapter(this, hms);
        RecyclerView rvHouseholdMembers = findViewById(R.id.rvHouseholdMemberFoodRecrods);

        rvHouseholdMembers.setAdapter(mAdapter);
        rvHouseholdMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

}
