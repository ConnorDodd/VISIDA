package au.edu.newcastle.jnc985.visida.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.RecipeListViewModel;
import bo.Utilities;
import bo.db.entity.ImageListProvider;
import bo.db.entity.Recipe;
import ui.EatingOccasionListAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.NAVBAR;

public class ListRecipesActivity extends AppCompatActivity {

    private static final String RECIPEID = "recipeId";
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int AUDIO_REQUEST_CODE = 2;
    private static final String TAG = "ListRecipeActivity";
    private NavigationBarFragment mNavBar;

    private RecipeListViewModel mRecipeViewModel;
    private EatingOccasionListAdapter mAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_add_recipe);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": List Recipes Activity Created");
        setContentView(R.layout.activity_list_recipe);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        if(mRecipeViewModel == null){
            mRecipeViewModel = new ViewModelProvider(this).get(RecipeListViewModel.class);
        }

        setUpList();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecipe();
            }
        });
    }

    private void addRecipe() {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked New Recipe");
        Intent i = new Intent(this, CreateRecipeActivity.class);
        startActivity(i);
    }

    private void setUpList(){
        //Create Adapter/ Send this as context and Click listener implementation

        String edit = getResources().getString(R.string.edit);
        mAdaptor = new EatingOccasionListAdapter(null, edit, new EatingOccasionListAdapter.onImageProviderClickListener() {
            @Override
            public void onItemClick(ImageListProvider item) {
                //Create intent to move to Create REcipe Activity with filled recipe.
                Recipe recipe = (Recipe) item;
                if(!recipe.isLocked()) {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Recipe id " + recipe.getRecipeId());
                    Intent i = new Intent(ListRecipesActivity.this, CreateRecipeActivity.class);
                    i.putExtra(RECIPEID, recipe.getId());
                    startActivity(i);
                }
                else{
                    Toast.makeText(ListRecipesActivity.this, R.string.recipe_locked, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public boolean onItemLongClick(ImageListProvider item) {
                //Ask to confirm deleting the item
                new AlertDialog.Builder(ListRecipesActivity.this)
                        .setTitle(R.string.delete_item)
                        .setMessage(R.string.delete_confirmation)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.i(ACTIVITY_LOG_TAG, TAG + ": Deleted Recipe " + item.getId());
                                Toast.makeText(ListRecipesActivity.this, R.string.recipe_deleted, Toast.LENGTH_SHORT).show();
                                //Delete the HM
                                Recipe recipe = (Recipe) item;
                                ListRecipesActivity.this.mRecipeViewModel.deleteRecipe(recipe);

                            }})
                        .setNegativeButton(R.string.no, null).show();
                return true;
            }
        });

        mRecipeViewModel.getObservableRecipes().observe(this,  new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                mAdaptor.setList(recipes);
            }
        });



        //Get Recycler View
        RecyclerView list = (RecyclerView) findViewById(R.id.rvRecipes);
        list.setAdapter(mAdaptor);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }


}
