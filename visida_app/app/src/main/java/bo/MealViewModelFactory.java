package bo;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MealViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApp;
    private long mealId;

    public MealViewModelFactory(Application app, long mealId){
        mApp = app;
        this.mealId = mealId;
    }

    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MealViewModel((mApp), mealId);
    }

}
