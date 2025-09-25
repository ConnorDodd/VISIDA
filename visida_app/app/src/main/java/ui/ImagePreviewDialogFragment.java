package ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import au.edu.newcastle.jnc985.visida.R;

/**
 * A simple {@link DialogFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImagePreviewDialogFragment.OnImagePreviewFragmentClickListener} interface
 * to handle interaction events.
 * Use the {@link ImagePreviewDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImagePreviewDialogFragment extends DialogFragment {


    public interface OnImagePreviewFragmentClickListener {
        void onPositiveClick(String imageName);
        void onNegativeClick(String imageName);
    }

    private static final String IMAGE_PATH = "imagepath";

    private String mImagePath;

    private OnImagePreviewFragmentClickListener mListener;

    // Required empty public constructor
    public ImagePreviewDialogFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imagePath Parameter 1.
     * @return A new instance of fragment ImagePreviewDialogFragment.
     */
    public static ImagePreviewDialogFragment newInstance(String imagePath) {
        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_PATH, imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImagePath = getArguments().getString(IMAGE_PATH);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.fragment_image_preview_dialog, null);
        final ImageView imgView = dialogLayout.findViewById(R.id.dialog_imageview);
        builder.setView(dialogLayout);
        AlertDialog dialog = builder.create();
        Button btnAccept = dialogLayout.findViewById(R.id.btnImageAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imageFile = new File(mImagePath);
                String fileName = imageFile.getName();
                clearGlideCache();
                mListener.onPositiveClick(fileName);
            }
        });
        Button btnDecline = dialogLayout.findViewById(R.id.btnImageDecline);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imageFile = new File(mImagePath);
                String fileName = imageFile.getName();
                clearGlideCache();
                mListener.onNegativeClick(fileName);
                dialog.dismiss();
            }
        });


        Glide.with(this)
                .load(mImagePath)
                .apply(new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_thinking)
                        .skipMemoryCache(true))
                .into(imgView);

        return dialog;
    }

    private void clearGlideCache() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Glide.get(ImagePreviewDialogFragment.this.getContext()).clearDiskCache();

            }
        });
        Glide.get(ImagePreviewDialogFragment.this.getContext()).clearMemory();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnImagePreviewFragmentClickListener) {
            mListener = (OnImagePreviewFragmentClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnImagePreviewFragmentClickListener");
        }


    }

    @Override
    public void onDismiss(final DialogInterface dialog){
        super.onDismiss(dialog);
        final Activity context = getActivity();
        if(context instanceof DialogInterface.OnDismissListener){
            ((DialogInterface.OnDismissListener)context).onDismiss(dialog);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
