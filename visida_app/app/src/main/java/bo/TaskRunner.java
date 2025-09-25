package bo;
// https://stackoverflow.com/questions/58767733/android-asynctask-api-deprecating-in-android-11-what-are-the-alternatives
// https://developer.android.com/codelabs/android-room-with-a-view#7

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;

import bo.db.AppDatabase;

// A Utility method for running tasks on background threads with a callback
public class TaskRunner {
    public interface Callback<R> {
        void onComplete(R result);
    }

    public static <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.executor.execute(() -> {
            final R result;
            try {
                result = callable.call();
                handler.post(() -> {
                    callback.onComplete(result);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}