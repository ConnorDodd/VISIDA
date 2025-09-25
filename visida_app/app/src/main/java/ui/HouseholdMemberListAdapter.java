package ui;

import android.app.Activity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionRepository;
import bo.State;
import bo.Utilities;
import bo.db.entity.HouseholdMember;

/**
 * Created by jnc985 on 28-Nov-17.
 * Adapter for binding Eating Occasions to a list view.
 * Provides:
 *  - an ImageView for the users avatar/picture
 *  - Text View for User name (not sure if we are using names for the trial though)
 *  - Secondary Text View for other data. (This is here just as an exmaple of what can be done. Consider swapping for images of food already taken if any)
 */

public class HouseholdMemberListAdapter extends RecyclerView.Adapter<HouseholdMemberListAdapter.HouseholdMemberViewHolder> {
    private static final String TAG = "HMListAdapter";

    private final Activity context;
    private List<HouseholdMember> mHouseholdMembers;
    private HouseholdMemberClickListener mClickListener;


    public class HouseholdMemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView txtName;
        public ImageView image;
        public TextView txtAge;
        public HouseholdMemberClickListener mViewHolderClickListener;

        public HouseholdMemberViewHolder(View v, HouseholdMemberClickListener listener){
            super(v);
            txtName = (TextView) v.findViewById(R.id.txtHmName);
            image = (ImageView) v.findViewById(R.id.imgHmAvatar);
            txtAge = (TextView) v.findViewById(R.id.textView1);
            mViewHolderClickListener = listener;
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Handle clicking a household member.
            mViewHolderClickListener.householdMemberClicked(mHouseholdMembers.get(this.getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            return mViewHolderClickListener.householdMemberLongClicked(mHouseholdMembers.get(this.getAdapterPosition()));
        }
    }

    /**
     * Interface to implement for clicking a household member in the Recycler view.
     * The owning activity/Fragment should implement this and pass it to the adapter
     */
    public interface HouseholdMemberClickListener{
        void householdMemberClicked(HouseholdMember hm);
        boolean householdMemberLongClicked(HouseholdMember hm);
    }

    //TODO Does this neec context? NO, only used for loading dummy images
    public HouseholdMemberListAdapter(Activity context, HouseholdMemberClickListener clickListener){
        this.context = context;
        this.mClickListener = clickListener;
    }

    @Override
    public HouseholdMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_householdmember, parent, false);

        //Create the ViewHolder and pass in the clicklistener(owning activity which implements the interface)
        HouseholdMemberListAdapter.HouseholdMemberViewHolder vh = new HouseholdMemberViewHolder(itemView, mClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(HouseholdMemberViewHolder holder, int pos){
        HouseholdMember hm = mHouseholdMembers.get(pos);
        String hmName = hm.getName();
        //If we are finalizeing check if there are unfinalized EOs and add an asterix if there are.
        if(Utilities.getState(context.getApplicationContext()) == State.FINALIZE) {
            //Check if the HousheoldMember has any unfinalized Eating Occasions
            EatingOccasionRepository eoRepo = new EatingOccasionRepository(context.getApplication());
            if (eoRepo.hasNonFinalizedEatingOccasion(hm.getUid())) {
                hmName += "*";
            }
        }
        holder.txtName.setText(hmName);

        Glide.with(context)
                .load(hm.getAvatar())
                .apply(new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.ic_default_person))
                .into(holder.image);


        int years = hm.getAgeInYears();
        int months = hm.getAgeInMonths();
        // Display their age in years
        String age ="";
        if(years != 0) {
            age = years + " " + context.getResources().getString(R.string.years);
        }
        // Display how many months old they are too
        if(months != 0 ){
            // Add comma separator if required
            if(years != 0) {
                age += ", ";
            }
            age += months + " " + context.getResources().getString(R.string.months);
        }
        holder.txtAge.setText(age);
    }

    @Override
    public int getItemCount(){
        return mHouseholdMembers == null ? 0 : mHouseholdMembers.size();
    }

    public void setHouseholdMemberList(final List<HouseholdMember> hmList){
        if(mHouseholdMembers == null){
            mHouseholdMembers = hmList;
            notifyItemRangeInserted(0, hmList.size());
        }
        else{
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback(){

                @Override
                public int getOldListSize() {
                    return mHouseholdMembers.size();
                }

                @Override
                public int getNewListSize() {
                    return hmList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mHouseholdMembers.get(oldItemPosition).getUid() == hmList.get(newItemPosition).getUid();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mHouseholdMembers.get(oldItemPosition).equals(hmList.get(newItemPosition));
                }
            });
            mHouseholdMembers = hmList;
            result.dispatchUpdatesTo(this);
            notifyDataSetChanged();
        }
    }

    public HouseholdMember getHouseholdMember(int pos){
        return this.mHouseholdMembers.get(pos);
    }
}
