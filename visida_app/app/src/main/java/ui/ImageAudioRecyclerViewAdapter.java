package ui;

import android.content.Context;
import android.media.MediaPlayer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.Utilities;
import bo.db.entity.ImageAudioProvider;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

/**
 * Created by jnc985 on 27-Feb-18.
 */

public class ImageAudioRecyclerViewAdapter extends RecyclerView.Adapter<ImageAudioRecyclerViewAdapter.ImageAudioViewHolder> {
    private static final String TAG = "ImageAudioRecyclerViewAdapter";
    private final RequestManager glide;
    private final RecyclerViewItemClickListener mClickListener;
    private List<? extends ImageAudioProvider> mItems;
    private File mMediaDir;
    private Context mContext;

    public class ImageAudioViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        ImageView imgFoodItemImage;
        ImageView imgAudioImage;
        RecyclerViewItemClickListener mVhClickListener;

        ImageAudioViewHolder(View itemView, RecyclerViewItemClickListener listener){
            super(itemView);
            imgFoodItemImage = (ImageView) itemView.findViewById(R.id.imgIngredientImage);
            imgAudioImage = (ImageView) itemView.findViewById(R.id.imgAudioPlaceholder);
            mVhClickListener = listener;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            //Delete the FoodItem from the view Model
            return mVhClickListener.recyclerViewItemLongClicked(mItems.get(this.getAdapterPosition()));
        }
    }

    /**
     * Interface to implement for clicking a Item member in the Recycler view.
     * The owning activity/Fragment should implement this and pass it to the adapter
     */
    public interface RecyclerViewItemClickListener{
        void recyclerViewItemClicked(ImageAudioProvider item);
        boolean recyclerViewItemLongClicked(ImageAudioProvider item);
    }

    public ImageAudioRecyclerViewAdapter(Context context, RecyclerViewItemClickListener listener){
        this.glide = Glide.with(context);
        this.mMediaDir = Utilities.getMediaDirectory(context);
        this.mContext = context;
        this.mClickListener = listener;
    }

    @Override
    public ImageAudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_image_audio, parent, false);

        //Create the ViewHolder and pass in the clicklistener(owning activity which implements the interface)
        ImageAudioViewHolder vh = new ImageAudioViewHolder(itemView, mClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ImageAudioViewHolder holder, int pos){
        ImageAudioProvider ingredient = mItems.get(pos);
        String imgName = ingredient.getImageName() == null ? "" : ingredient.getImageName();
        File imgFile = new File(mMediaDir, imgName);
        glide.load(imgFile)
                .apply(new RequestOptions()
                .placeholder(R.drawable.ic_food_placeholder_100))
                .into(holder.imgFoodItemImage);
        glide.load(R.drawable.ic_audio_file).into(holder.imgAudioImage);

        holder.imgAudioImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(mItems.get(holder.getAdapterPosition()));
            }
        });
    }

    private void playAudio(ImageAudioProvider ic){
        MediaPlayer mediaPlayer = MediaPlayerManager.getInstance();
        if(!mediaPlayer.isPlaying()) {
            try {
                String audioFileName = ic.getAudioName();
                if(audioFileName != null) {
                    File mediaDirectory = Utilities.getMediaDirectory(mContext);
                    File dataSource = new File(mediaDirectory, audioFileName);
                    mediaPlayer.setDataSource(dataSource.getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Played audio");
                }
                else{
                    String description = ic.getDescription();
                    if (description != null && !description.isEmpty()) {
                        Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    @Override
    public int getItemCount(){
        return mItems == null ? 0 : mItems.size();
    }

    public void setIngredientsList(final List<? extends ImageAudioProvider> itemCapture) {
            if(mItems == null){
                mItems = itemCapture;
                notifyItemRangeInserted(0, itemCapture.size());
            }
            else{
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback(){

                    @Override
                    public int getOldListSize() {
                        return mItems.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return itemCapture.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mItems.get(oldItemPosition).equals(itemCapture.get(newItemPosition));
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        return mItems.get(oldItemPosition).equals(itemCapture.get(newItemPosition));
                    }
                });
                mItems = itemCapture;
                result.dispatchUpdatesTo(this);
                notifyDataSetChanged();
            }
        }
}

