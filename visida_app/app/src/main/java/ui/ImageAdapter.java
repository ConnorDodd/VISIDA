package ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    List<String> images;

    public ImageAdapter(Context c, List<String> images) {
        mContext = c;
        this.images = images;
    }

    public int getCount() {
        return images == null ? 0 : images.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Glide.with(parent.getContext())
                .load(images.get(position))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_audio_file))
                .into(imageView);
        //imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }
}