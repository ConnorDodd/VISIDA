package ui;

import android.content.Context;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.db.entity.ImageListProvider;

/**
 * Created by Josh on 11-Jan-18.
 */

public class EatingOccasionListAdapter extends RecyclerView.Adapter<EatingOccasionListAdapter.ImageProviderViewHolder> {


    public class ImageProviderViewHolder extends RecyclerView.ViewHolder{
        Button btnFinalize;
        RecyclerView imageList;

        ImageProviderViewHolder(View itemView){
            super(itemView);
            imageList = itemView.findViewById(R.id.rvImageList);
            btnFinalize = itemView.findViewById(R.id.btnFinalizeItem);
            if(EatingOccasionListAdapter.this.hideButton){
                btnFinalize.setVisibility(View.GONE);
            }
            imageList.setNestedScrollingEnabled(false);
        }

        public void bind(final ImageListProvider item, final onImageProviderClickListener handler){
            //Get the list of image paths
            List<String> imagePaths = item.getImageNames();

            Context context = imageList.getContext();
            //Create List adapter for nested list of images
            ImageListAdapter adapter = new ImageListAdapter(context, imagePaths, true);
            imageList.setAdapter(adapter);
            //Add layout manager for nested list (horizontal)
            LinearLayoutManager llm = new LinearLayoutManager(context);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            imageList.setLayoutManager(llm);

            String btnText = EatingOccasionListAdapter.this.mButtonText;
            if(btnText != null) {
                btnFinalize.setText(btnText);
            }
            else{
                //What do we do in the Cook use case? Set it to the id? Hide the button? Play audio on click?
                btnFinalize.setText("");
            }
            btnFinalize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.onItemClick(item);
                }
            });
            btnFinalize.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return handler.onItemLongClick(item);
                }
            });
        }
    }

    public interface onImageProviderClickListener{
        void onItemClick(ImageListProvider item);
        boolean onItemLongClick(ImageListProvider item);
    }

    private List<? extends ImageListProvider> mItems;
    private onImageProviderClickListener mHandler;
    private String mButtonText;
    private boolean hideButton;

    public EatingOccasionListAdapter(List<? extends ImageListProvider> items, String buttonText, onImageProviderClickListener handler){
        this.mItems = items;
        this.mHandler = handler;
        this.mButtonText = buttonText;
    }

    /**
     * Set whether each row should show the button.
     * Default is false.
     * @param hideButton
     */
    public void setHideButton(boolean hideButton){
        this.hideButton = hideButton;
    }

    @Override
    public ImageProviderViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_eating_occasion, parent, false);


        ImageProviderViewHolder vh = new ImageProviderViewHolder(itemView);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public void onBindViewHolder(ImageProviderViewHolder holder, int pos){
        holder.bind(mItems.get(pos), mHandler);
    }

    public void setList(final List<? extends ImageListProvider> items) {
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
}
