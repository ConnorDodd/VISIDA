package ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.Utilities;
import bo.db.entity.Recipe;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> {
    private static final String TAG = "recipelistadapter";

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        //Check box for selected Recipe
        public CheckBox isSelected;
        public ImageView imgRecipeImage;
        public ImageView imgRecipeName;

        public RecipeViewHolder(View v){
            super(v);
            isSelected = v.findViewById(R.id.chkIsSelected);
            imgRecipeImage = v.findViewById(R.id.imgRecipeImage);
            imgRecipeName = v.findViewById(R.id.imgRecipeName);

        }


    }

    private final RequestManager glide;
    private Context mContext;
    private List<Recipe> mRecipes;
    private List<Boolean> mSelected;

    public RecipeListAdapter(Context context, List<Recipe> recipes, long[] mSelectedRecipes){
        this.mRecipes = recipes;
        this.mSelected = new ArrayList<Boolean>();
        for(int i = 0; i < mRecipes.size(); i++){
            mSelected.add(false);
            for(int j = 0; j < mSelectedRecipes.length; j++){
                if(mRecipes.get(i).getRecipeId() == mSelectedRecipes[j]){
                    mSelected.set(i, true);
                }
            }
        }
        this.mContext = context;
        this.glide = Glide.with(context);
    }

    public void updateRecipes(List<Recipe> recipes, long[] mSelectedRecipes) {
        this.mRecipes = recipes;
        for(int i = 0; i < mRecipes.size(); i++){
            mSelected.add(false);
            for(int j = 0; j < mSelectedRecipes.length; j++){
                if(mRecipes.get(i).getRecipeId() == mSelectedRecipes[j]){
                    mSelected.set(i, true);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //Inflate the Individual item layout.
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_recipe_list_item, parent, false);

        //Create View holder
        RecipeViewHolder vh = new RecipeViewHolder(item);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecipeViewHolder holder, int pos){
        Recipe r = mRecipes.get(pos);
        File imgFile = null;
        String imgUrl = r.getFinalImageUrl();
        if(imgUrl != null) {
            imgFile = new File(Utilities.getMediaDirectory(mContext), imgUrl );
        }
        glide.load(imgFile)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_food_placeholder_100))
                .into(holder.imgRecipeImage);

        holder.imgRecipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(r);
            }
        });

        holder.isSelected.setChecked(mSelected.get(pos));
        holder.isSelected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int clickPos = holder.getAdapterPosition();
                boolean selected = mSelected.get(clickPos);
                mSelected.set(clickPos, !selected);
                holder.isSelected.setChecked(mSelected.get(clickPos));
            }
        });
    }

    /**
     * Get the recipes which have been selected. And return them as
     * a list of their Id's
     * @return
     */
    public List<Long> getSelectedRecipes() {
        List<Long> selectedRecipes = new ArrayList<>();
        //Loop thorugh the booleans and add the ones that are true to the list.
        for(int i = 0; i < mRecipes.size(); i++){
            if(mSelected.get(i)){
                selectedRecipes.add(mRecipes.get(i).getRecipeId());
            }
        }
        return selectedRecipes;
    }

    @Override
    public int getItemCount() {return mRecipes == null ? 0 : mRecipes.size();}

    private void playAudio(Recipe recipe){
        MediaPlayer mediaPlayer = MediaPlayerManager.getInstance();
        if(!mediaPlayer.isPlaying()) {
            try {
                if(recipe.getRecipeNameAudioUrl() != null) {
                    File mediaDirectory = Utilities.getMediaDirectory(mContext);
                    File dataSource = new File(mediaDirectory, recipe.getRecipeNameAudioUrl());
                    mediaPlayer.setDataSource(dataSource.getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                else{
                    String description = recipe.getRecipeNameText();
                    if (description != null && !description.isEmpty()) {
                        Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }


}
