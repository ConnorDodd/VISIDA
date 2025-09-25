package ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
import bo.db.entity.IReviewable;
import bo.db.entity.ImageAudioProvider;

import static bo.AppConstants.FRID;
import static bo.AppConstants.PPID;

public class RecordReviewAdapter extends RecyclerView.Adapter<RecordReviewAdapter.RecordReviewViewHolder>{

    private List<IReviewable> mItems;
    private int mHeaderColor = R.color.colorSeconday;
    private String mPpid;

    public RecordReviewAdapter(List<IReviewable> eos) {
        this.mItems = eos;
    }

    public RecordReviewAdapter(List<IReviewable> eos, String ppid, int headerColor) {
        this.mItems = eos;
        this.mHeaderColor = headerColor;
        this.mPpid = ppid;
    }

    @NonNull
    @Override
    public RecordReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_recordreview_item, parent, false);


        RecordReviewViewHolder vh = new RecordReviewViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordReviewViewHolder holder, int position) {
        //Set the time
        IReviewable eo = mItems.get(position);
        Context context = holder.rvFoodItems.getContext();
        String time = eo.getHeaderString();
        holder.txtEoTime.setText(time);

        if(mHeaderColor != R.color.colorPrimary) {
            if (eo.isValid()) {
                setEoHeaderColor(holder.txtEoTime, mHeaderColor);
            }
            else{
                setEoHeaderColor(holder.txtEoTime, R.color.colorAccent);
            }
            holder.txtEoTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, RecordReviewActivity.class);
                    i.putExtra(PPID, mPpid);
                    i.putExtra(FRID, eo.getId());
                    holder.txtEoTime.getContext().startActivity(i);
                }
            });
        }

        List<? extends ImageAudioProvider> foodItems = eo.getReviewItems();
        ImageGridRecyclerViewAdapter adapter = new ImageGridRecyclerViewAdapter(context, foodItems);
        int cols = 2;
        holder.rvFoodItems.setLayoutManager(new GridLayoutManager(context, cols));
        holder.rvFoodItems.setAdapter(adapter);
    }

    private void setEoHeaderColor(TextView txtEoTime, int color) {

        if(color == R.color.colorSeconday) {
            Drawable drawable = txtEoTime.getContext().getResources().getDrawable(R.drawable.rounded_corner_secondary, null);
            txtEoTime.setBackground(drawable);
        }
        else if(color == R.color.colorAccent){
            Drawable drawable = txtEoTime.getContext().getResources().getDrawable(R.drawable.rounded_corner_accent, null);
            txtEoTime.setBackground(drawable);
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public void addRecord(IReviewable eo){
        //Create a new Eating Occasion to capture the forgotten Records
        if(!mItems.contains(eo)){
            mItems.add(eo);
        }

        this.notifyDataSetChanged();
    }

    public void setHeaderColor(int resId) {
        this.mHeaderColor = resId;
    }

    public void setList(final List<IReviewable> items) {
        if(mItems == null){
            mItems = items;
            notifyItemRangeInserted(0, items.size());
        }
        else{
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback(){

                @Override
                public int getOldListSize() {
                    return mItems.size();
                }

                @Override
                public int getNewListSize() {
                    return items.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mItems.get(oldItemPosition).equals(items.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mItems.get(oldItemPosition).equals(items.get(newItemPosition));
                }
            });
            mItems = items;
            result.dispatchUpdatesTo(this);
            notifyDataSetChanged();
        }
    }

    public class RecordReviewViewHolder extends RecyclerView.ViewHolder{

        TextView txtEoTime;
        RecyclerView rvFoodItems;

        RecordReviewViewHolder(View itemView){
            super(itemView);
            this.txtEoTime = itemView.findViewById(R.id.txtEoTime);
            this.rvFoodItems = itemView.findViewById(R.id.rvFoodItems);
        }
    }
}
