package com.example.android.bakingapp.utilities;

import android.content.Context;

import com.example.android.bakingapp.AppExecutors;
import com.example.android.bakingapp.data.RecipeDatabase;
import com.example.android.bakingapp.data.RecipeRepository;
import com.example.android.bakingapp.ui.main.MainViewModelFactory;

public class InjectorUtils {

    public static RecipeRepository provideRepository(Context context) {
        RecipeDatabase database = RecipeDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        BakingInterface bakingInterface = RetrofitClient.getClient().create(BakingInterface.class);
        return RecipeRepository.getInstance(database.recipeDao(), bakingInterface, executors);
    }

    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        RecipeRepository repository = provideRepository(context);
        return new MainViewModelFactory(repository);
    }
}