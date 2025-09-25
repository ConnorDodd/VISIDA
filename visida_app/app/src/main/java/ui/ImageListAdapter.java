package ui;

import android.content.Context;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.Utilities;

/**
 * Created by jnc985 on 28-Nov-17.
 * Adapter for binding a list if image names to a recycler view
 * This adapter assumes all of the images are inside the apps
 * media directory and the stringd are simply the names of the
 * images NOT the full path.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {
    private static final String TAG = "imageListAdapter";

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgView;

        public ImageViewHolder(View v){
            super(v);
            imgView = v.findViewById(R.id.imgFoodItemImage);
        }
    }

    private List<String> mImagePaths;
    private Context mContext;
    private RequestManager glide;
    private File mMediaDir;
    private boolean useMediaDirectory;

    public ImageListAdapter(Context context, List<String> imagePaths, boolean useMediaDirectory){
        this.mContext = context;
        glide = Glide.with(context);
        this.mImagePaths = imagePaths;
        //Get media directory once to save time later.
        this.mMediaDir = Utilities.getMediaDirectory(mContext);
        this.useMediaDirectory = useMediaDirectory;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_image_list_item, parent, false);

        //Create the ViewHolder and pass in the clicklistener(owning activity which implements the interface)
        ImageListAdapter.ImageViewHolder vh = new ImageViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int pos){
        //Load image into imgview
        String imagePath = mImagePaths.get(pos);
        // If there is a valid image
        if(imagePath != null && !imagePath.equals("")) {
            File imgFile;
            if (useMediaDirectory) {
                imgFile = new File(mMediaDir, imagePath);
            } else {
                imgFile = new File(imagePath);
            }
            glide.load(imgFile.getAbsolutePath())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_food_placeholder_100))
                    .into(holder.imgView);
        }
        // Otherwise load the default image
        else {
            glide.load(R.drawable.ic_food_placeholder_100).into(holder.imgView);
        }
    }

    public void setImageList(final List<String> imageList) {
        if (mImagePaths == null) {
            mImagePaths = imageList;
            notifyItemRangeInserted(0, imageList.size());
        }
        else{
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mImagePaths.size();
                }

                @Override
                public int getNewListSize() {
                    return imageList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mImagePaths.get(oldItemPosition).equals(imageList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mImagePaths.get(oldItemPosition).equals(imageList.get(newItemPosition));
                }
            });
            mImagePaths = imageList;
            result.dispatchUpdatesTo(this);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount(){
        return mImagePaths == null ? 0 : mImagePaths.size();
    }

}
