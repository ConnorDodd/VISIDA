package ui;

import android.content.Context;
import android.media.MediaPlayer;
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

public class ImageGridRecyclerViewAdapter extends RecyclerView.Adapter<ImageGridRecyclerViewAdapter.FoodItemImageViewHolder> {
    private static final String TAG = "ImageGridRecyclerViewAdapter";

    private final RequestManager glide;
    private List<? extends ImageAudioProvider> mItems;
    private String mMediaDirectoryBathBase;

    public ImageGridRecyclerViewAdapter(Context c, List<? extends ImageAudioProvider> imageUrls){
        this.mMediaDirectoryBathBase = Utilities.getMediaDirectory(c).getAbsolutePath();
        this.glide = Glide.with(c);
        this.mItems = imageUrls;
    }

    @Override
    public FoodItemImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_image, parent, false);

        //Create the ViewHolder and pass in the clicklistener(owning activity which implements the interface)
        FoodItemImageViewHolder vh = new FoodItemImageViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(FoodItemImageViewHolder holder, int pos){
        //Get the full path url of the food item
        ImageAudioProvider item = mItems.get(pos);
        File f = new File(mMediaDirectoryBathBase, item.getImageName());
        glide.load(f)
                .apply(new RequestOptions()
                .placeholder(R.drawable.ic_audio_file))
                .into(holder.imgFoodItemImage);

        holder.imgFoodItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Play the audio file
                MediaPlayer mediaPlayer = MediaPlayerManager.getInstance();
                //If its not already playing something then start playing.
                if(!mediaPlayer.isPlaying()) {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Playing audio file for image " + item.getImageName());
                    try {
                        String audioFileName = item.getAudioName();
                        if (audioFileName != null) {
                            File dataSource = new File(mMediaDirectoryBathBase, audioFileName);
                            mediaPlayer.setDataSource(dataSource.getAbsolutePath());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        }
                        else if(item.getDescription() != null && !item.getDescription().isEmpty()){
                            Toast.makeText(holder.imgFoodItemImage.getContext(), item.getDescription(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (IOException ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount(){
        return mItems == null ? 0 : mItems.size();
    }


    public class FoodItemImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imgFoodItemImage;

        FoodItemImageViewHolder(View itemView){
            super(itemView);
            imgFoodItemImage = (ImageView) itemView.findViewById(R.id.imgFoodItemImage);
        }
    }
}
