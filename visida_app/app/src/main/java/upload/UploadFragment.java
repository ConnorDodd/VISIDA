package upload;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UploadCallback} interface
 * to handle interaction events.
 * Use the {@link UploadFragment#getInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Fragment {
    private static final String URLKEY = "URL";
    private static final String TAG = "uploadfragment";

    private String mUrl;
    private UploadTask mUploadTask;
    private UploadCallback mCallback;

    public UploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadFragment getInstance(FragmentManager fragmentManager, String url) {
        UploadFragment networkFragment = new UploadFragment();
        Bundle args = new Bundle();
        args.putString(URLKEY, url);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        return networkFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(URLKEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        return textView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadCallback) {
            mCallback = (UploadCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UploadCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelUpload();
        super.onDestroy();
    }

    /**
     * Start non-blocking execution of UploadTask.
     */
    public void startUpload() {
        //Cancel any previous upload tasks already running.
        cancelUpload();
        mUploadTask = new UploadTask(mCallback);
        mUploadTask.execute(mUrl);
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing UploadTask execution.
     */
    public void cancelUpload() {
        if (mUploadTask != null) {
            mUploadTask.cancel(true);
        }
    }


    private static class UploadTask extends AsyncTask<String, Integer, UploadTask.Result> {

        private UploadCallback<String> callback;

        UploadTask(UploadCallback<String> callback) {
            setCallback(callback);
        }

        void setCallback(UploadCallback<String> callback) {
            this.callback = callback;
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        static class Result {
            public String resultValue;
            public Exception exception;
            public Result(String resultValue) {
                this.resultValue = resultValue;
            }
            public Result(Exception exception) {
                this.exception = exception;
            }
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (callback != null) {
                NetworkCapabilities networkCapabilities = callback.getActiveNetworkCapabilities();
                if (networkCapabilities == null || !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    // If not on wifi, cancel task and update Callback with null data.
                    callback.updateFromUpload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected UploadTask.Result doInBackground(String... urls) {
            Result result = null;
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                try {
                    URL url = new URL(urlString);
                    String resultString = uploadUrl(url);
                    if (resultString != null) {
                        result = new Result(resultString);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch(Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Updates the UploadCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && callback != null) {
                if (result.exception != null) {
                    callback.updateFromUpload(result.exception.getMessage());
                } else if (result.resultValue != null) {
                    callback.updateFromUpload(result.resultValue);
                }
                callback.finishUploading();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
        }

        /**
         * Send DownloadCallback a progress update.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values.length >= 2) {
                callback.onProgressUpdate(values[0], values[1]);
            }
        }

        private String uploadUrl(URL url) throws IOException {
            String AUTH = "bearer 5a9881ff2c264dd0a7b9e564c7226db9";
            InputStream stream = null;
            HttpsURLConnection connection = null;
            StringBuffer result = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", AUTH);
                connection.connect();
                publishProgress(UploadCallback.Progress.CONNECT_SUCCESS, 0);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                stream = connection.getInputStream();
                publishProgress(UploadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
                if(stream != null){
                    BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                    String inputLine;
                    result = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine);
                    }
                }
            }
            finally {
                if(stream != null){
                    stream.close();
                }
                if(connection != null){
                    connection.disconnect();
                }
            }
            return result.toString();
        }
    }
}
