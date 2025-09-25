package au.edu.newcastle.jnc985.visida.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.RecipeListViewModel;
import ui.RecipeListAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

/**
 * A simple {@link DialogFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectRecipeDialog.OnSelectRecipeListener} interface
 * to handle interaction events.
 * Use the {@link SelectRecipeDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectRecipeDialog extends DialogFragment {

    private static final String TAG = "SelectRecipeDialog";
    private static String SELECTED_RECIPES = "selectedrecipes";
    private RecipeListViewModel mRecipeViewModel;
    private OnSelectRecipeListener mListener;
    private RecipeListAdapter mAdapter;

    private long[] mSelectedRecipes;

    public SelectRecipeDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SelectRecipeDialog.
     */
    public static SelectRecipeDialog newInstance(long[] recipeIds) {
        SelectRecipeDialog fragment = new SelectRecipeDialog();
        Bundle args = new Bundle();
        args.putLongArray(SELECTED_RECIPES, recipeIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedRecipes = getArguments().getLongArray(SELECTED_RECIPES);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        //noinspection InflateParams
        final View view = inflater.inflate(R.layout.fragment_select_recipe_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setPositiveButton(R.string.audio_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Long> selectedIds = mAdapter.getSelectedRecipes();
                mListener.onDialogAccept(selectedIds);
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Linked " + selectedIds.size());
            }
        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Create view model for dialog.
        if(mRecipeViewModel == null){
            mRecipeViewModel = new ViewModelProvider(this).get(RecipeListViewModel.class);
        }

        mAdapter = new RecipeListAdapter(getContext(), mRecipeViewModel.getRecipes().getValue(), mSelectedRecipes);
        // Update the adaptor if the recipes change
        mRecipeViewModel.getRecipes().observe(this, recipes -> {
            mAdapter.updateRecipes(recipes, mSelectedRecipes);
        });
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView rv = view.findViewById(R.id.rvRecipeList);
        rv.setAdapter(mAdapter);
        rv.setLayoutManager(llm);

        builder.setView(view);
        return builder.create();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectRecipeListener) {
            mListener = (OnSelectRecipeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSelectRecipeListener {
        //Return a list of all the recipe Id's that have been selected.
        void onDialogAccept(List<Long> recipeIds);
    }
}
