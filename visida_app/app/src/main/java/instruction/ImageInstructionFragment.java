package instruction;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import au.edu.newcastle.jnc985.visida.R;

public class ImageInstructionFragment extends InstructionFragment implements FragmentLifecycle{

    public ImageInstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoInstructionFragment.
     */
    public static ImageInstructionFragment newInstance(String text, int mediaResId, int audioResId) {
        ImageInstructionFragment fragment = new ImageInstructionFragment();
        Bundle args = new Bundle();
        args.putString(TEXT, text);
        args.putInt(MEDIA_RES_ID, mediaResId);
        args.putInt(AUDIO_RES_ID, audioResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mText = getArguments().getString(TEXT);
            mMediaResId = getArguments().getInt(MEDIA_RES_ID);
            mAudioResId = getArguments().getInt(AUDIO_RES_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_image_instruction, container, false);

        ImageView imgv = v.findViewById(R.id.imgvInstructionImage);
        //Just load the image not the gif see below comment.
        Drawable d = v.getResources().getDrawable(mMediaResId, null);
        Glide.with(container.getContext())
                .load(d)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_info))
                .into(imgv);

        TextView txt = v.findViewById(R.id.txtInstruction);
        txt.setText(Html.fromHtml(mText, Html.FROM_HTML_MODE_COMPACT));

        ImageView audioImg = v.findViewById(R.id.imgvAudioFile);
        setupAudio(audioImg);

        return v;
    }


    @Override
    public void onFragmentPause() {
        System.out.println("Pause Image");
    }

    @Override
    public void onFragmentResume() {
        System.out.println("Resume VIDEO");
    }

}
