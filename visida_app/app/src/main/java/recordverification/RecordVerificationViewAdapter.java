package recordverification;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.FoodRecordRepository;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.IReviewable;
import ui.RecordReviewAdapter;

/*
We want to display all the food records for each houshold member in the app.

For each household member
    Display Houshold Member (name)
    For each food record
        Display Food Record (Date)
        We dont actually need the eating occaion separation
        For each EatingOccasion
            Display food items.

 */
public class RecordVerificationViewAdapter extends RecyclerView.Adapter<RecordVerificationViewAdapter.ReviewVerificationViewHolder> {

    //List of all the household members
    private List<HouseholdMember> mHousholdMembers;
    //private RecordReviewAdapter mEoAdapter;
    private FoodRecordRepository mFrRepo;
    private Activity owner;

    public RecordVerificationViewAdapter(Activity owner, List<HouseholdMember> hms) {
        this.mHousholdMembers = hms;
        this.mFrRepo = new FoodRecordRepository(owner.getApplication());
        this.owner = owner;
    }

    @NonNull
    @Override
    public ReviewVerificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_recordverification_item, parent, false);

        ReviewVerificationViewHolder vh = new ReviewVerificationViewHolder(item);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewVerificationViewHolder holder, int position) {
        HouseholdMember hm = mHousholdMembers.get(position);
        holder.txthouseholdMemberName.setText(hm.getName());

        //Create adapter and  observe the live data from the Database.
        RecordReviewAdapter eoAdapter =  new RecordReviewAdapter(null, hm.getParticipantHouseholdMemberId(), R.color.colorSeconday);
        mFrRepo.getObservableFoodRecordsForHouseholdMember(hm.getUid()).observe((LifecycleOwner) owner, new Observer<List<FoodRecord>>() {
            @Override
            public void onChanged(@Nullable List<FoodRecord> foodRecords) {
                eoAdapter.setList((List<IReviewable>)(List<?>) foodRecords);
            }
        });

        holder.rvEatingOccasions.setAdapter(eoAdapter);
        holder.rvEatingOccasions.setLayoutManager(new LinearLayoutManager(holder.rvEatingOccasions.getContext()));
    }

    @Override
    public int getItemCount() {
        return mHousholdMembers.size();
    }

    public class ReviewVerificationViewHolder extends RecyclerView.ViewHolder{

        TextView txthouseholdMemberName;
        RecyclerView rvEatingOccasions;
        public ReviewVerificationViewHolder(View itemView) {
            super(itemView);
            this.txthouseholdMemberName = itemView.findViewById(R.id.txtHmName);
            this.rvEatingOccasions = itemView.findViewById(R.id.rvEatingOccasions);
            //Turn off scrolling of the internal eating occasions
            rvEatingOccasions.setNestedScrollingEnabled(false);
        }
    }
}
