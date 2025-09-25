package ui;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;

/**
 * Created by jnc985 on 28-Nov-17.
 * Adapter for binding Eating Occasions to a list view.
 * Provides:
 *  - Text View for the name of the condiment
 *  - Button for adding a serving of the condiment
 *  - Button for minusing a serving
 *  - Text View to show the current number of servings
 */

public class CounterItemListAdapter extends RecyclerView.Adapter<CounterItemListAdapter.GuestViewHolder> {
    private static final String TAG = "geustListAdapter";
    private static final String ZERO = "0";


    public class GuestViewHolder extends RecyclerView.ViewHolder{

        public TextView txtItemName;
        public Button btnMinus;
        public TextView txtNumberOfGuests;
        public Button btnPlus;
        public ImageView imgIcon;


        public GuestViewHolder(View v){
            super(v);
            txtItemName = v.findViewById(R.id.txtItemName);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
            imgIcon = v.findViewById(R.id.imgIcon);
            txtNumberOfGuests = v.findViewById(R.id.txtServings);
        }
    }

    private List<String> mItemNames;
    private List<Integer> mIcons;
    private int[] mItemCounts;

    public CounterItemListAdapter(List<String> items, List<Integer> icons){
        this.mItemNames = items;
        this.mItemCounts = new int[items.size()];
        this.mIcons = icons;
    }

    @Override
    public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_checkbox_counter_item, parent, false);

        //Create the ViewHolder and pass in the clicklistener(owning activity which implements the interface)
        GuestViewHolder vh = new GuestViewHolder(itemView);
        return vh;
    }



    @Override
    public void onBindViewHolder(final GuestViewHolder holder, int pos){
        final String item = mItemNames.get(pos);
        holder.txtItemName.setText(item);
        holder.imgIcon.setImageResource(mIcons.get(pos));
        holder.txtNumberOfGuests.setText(ZERO);
        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the number of servings
                String numberOfGuests = String.valueOf(holder.txtNumberOfGuests.getText());
                try{
                    int servings = Integer.parseInt(numberOfGuests);
                    if(servings > 0){
                        //Increment and update txt view
                        servings--;

                        //Convert to Khmer
                        holder.txtNumberOfGuests.setText(String.valueOf(servings));
                        mItemCounts[pos] = servings;
                    }
                }
                catch (NumberFormatException ex){
                    //Something went wrong with the format of the text view. Reset to 0.
                    holder.txtNumberOfGuests.setText(ZERO);
                    mItemCounts[pos] = 0;
                }
            }
        });
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the number of servings
                String numberOfGuests = String.valueOf(holder.txtNumberOfGuests.getText());
                try{
                    int servings = Integer.parseInt(numberOfGuests);
                    servings++;
                    //Format the number
                    holder.txtNumberOfGuests.setText(String.valueOf(servings));
                    mItemCounts[pos] = servings;

                }
                catch (NumberFormatException ex){
                    //Something went wrong with the format of the text view. Reset to 0.
                    holder.txtNumberOfGuests.setText(ZERO);
                    mItemCounts[pos] = 0;
                }
            }
        });
    }

    public List<String> getmItemNames(){
        return mItemNames;
    }
    public int[] getmItemCounts(){
        return mItemCounts;
    }

    @Override
    public int getItemCount(){
        return mItemNames == null ? 0 : mItemNames.size();
    }

    public String getItemAt(int pos){
        return this.mItemNames.get(pos);
    }

}
