package instruction;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import bo.Utilities;

public class InstructionFragmentAdapter extends FragmentStatePagerAdapter {

    private String[] mInstructionTexts;
    private int[] mInstructionMediaIds;
    private int[] mInstructionAudioIds;
    private Context mContext;

    public InstructionFragmentAdapter(Context context, FragmentManager fm, Instruction instruction){
        super(fm);
        this.mContext = context;
        Resources res = context.getResources();
        this.mInstructionTexts = res.getStringArray(instruction.getText());
        TypedArray typedArray = res.obtainTypedArray(instruction.getMedia());
        this.mInstructionMediaIds = Utilities.typedArrayToIds(typedArray);
        typedArray = res.obtainTypedArray(instruction.getAudio());
        this.mInstructionAudioIds = Utilities.typedArrayToIds(typedArray);
        typedArray.recycle();
    }

    @Override
    public Fragment getItem(int position) {
        String resType = mContext.getResources().getResourceTypeName(mInstructionMediaIds[position]);
        return InstructionFragment.getInstructionFragment(resType, mInstructionTexts[position], mInstructionMediaIds[position], mInstructionAudioIds[position]);
    }


    @Override
    public int getCount() {
        return mInstructionTexts.length;
    }
}
