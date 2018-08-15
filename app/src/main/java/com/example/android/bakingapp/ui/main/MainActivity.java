package com.example.android.bakingapp.ui.main;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import com.example.android.bakingapp.ConnectionStateMonitor;
import com.example.android.bakingapp.ConnectivityReceiver;
import com.example.android.bakingapp.GridAutofitLayoutManager;
import com.example.android.bakingapp.MyApp;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.databinding.ActivityMainBinding;
import com.example.android.bakingapp.model.Ingredient;
import com.example.android.bakingapp.model.Recipe;
import com.example.android.bakingapp.ui.detail.DetailActivity;
import com.example.android.bakingapp.utilities.InjectorUtils;
import com.example.android.bakingapp.widget.RecipeWidgetProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.example.android.bakingapp.utilities.Constant.EXTRA_RECIPE;
import static com.example.android.bakingapp.utilities.Constant.GRID_COLUMN_WIDTH;

/**
 * The MainActivity displays the list of recipes
 */
public class MainActivity extends AppCompatActivity implements RecipeAdapter.RecipeAdapterOnClickHandler,
        ConnectivityReceiver.ConnectivityReceiverListener {

    /** Member variable for RecipeAdapter */
    private RecipeAdapter mRecipeAdapter;

    /** Member variable for the list of recipes */
    private List<Recipe> mRecipeList;

    /** This field is used for data binding **/
    private ActivityMainBinding mMainBinding;

    /** ViewModel for MainActivity */
    private MainActivityViewModel mMainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Set up Timber
        Timber.plant(new Timber.DebugTree());

        // Create a LayoutManager and RecipeAdapter and set them to the RecyclerView
        initAdapter();

        // Observe data and update UI
        setupViewModel();

        // Set the SwipeRefreshLayout triggered by a swipe gesture
        setRefreshLayout();

        // Check internet connection
        checkConnection();

        checkConnectionStateMonitor();
    }

    /**
     * Creates a LayoutManager and RecipeAdapter and set them to the RecyclerView
     */
    private void initAdapter() {
        // A GridAutofitLayoutManager is responsible for calculating the amount of GridView columns
        // based on screen size and positioning item views within a RecyclerView into a grid layout.
        // Reference: @see "https://codentrick.com/part-4-android-recyclerview-grid/"
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(
                this, GRID_COLUMN_WIDTH);
        // Set the layout manager to the RecyclerView
        mMainBinding.rv.setLayoutManager(layoutManager);

        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mMainBinding.rv.setHasFixedSize(true);

        // Create an empty ArrayList
        mRecipeList = new ArrayList<>();

        // The RecipeAdapter is responsible for displaying each recipe in the list.
        mRecipeAdapter = new RecipeAdapter(mRecipeList, this);
        // Set adapter to the RecyclerView
        mMainBinding.rv.setAdapter(mRecipeAdapter);
    }

    /**
     * Every time the recipe data is updated, the onChanged callback will be invoked and update the UI
     */
    private void setupViewModel() {
        // Get the MainActivityViewModel from the factory
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(this);
        mMainViewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);

        // Update the list of recipes
        updateUI();
    }

    /**
     * Update the list of recipes
     */
    private void updateUI() {
        // Retrieve live data object using getRecipes() method from the ViewModel
        mMainViewModel.getRecipes().observe(MainActivity.this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipe) {
                if (recipe != null) {
                    mRecipeAdapter.addAll(recipe);
                }
            }
        });
    }

    /**
     * Sets the SwipeRefreshLayout triggered by a swipe gesture.
     */
    private void setRefreshLayout() {
        // Set the colors used in the progress animation
        mMainBinding.swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.image_mint),
                getResources().getColor(R.color.image_light_green),
                getResources().getColor(R.color.image_yellow),
                getResources().getColor(R.color.image_pink));

        // Set the listener to be notified when a refresh is triggered
        mMainBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            // Called when a swipe gesture triggers a refresh
            @Override
            public void onRefresh() {
                // Set a new value for the list of recipes
                mMainViewModel.setRecipes();
                // When refreshing, observe data and update UI
                updateUI();

                // Hide refresh progress
                mMainBinding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onItemClick(Recipe recipe) {
        // Wrap the parcelable into a bundle
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_RECIPE, recipe);

        // Update the list of ingredients using SharedPreferences each time the user selects the
        // recipe
        updateSharedPreference(recipe);

        // Send the update broadcast to the app widget
        sendBroadcastToWidget();

        // Create the Intent the will start the DetailActivity
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        // Pass the bundle through Intent
        intent.putExtra(EXTRA_RECIPE, b);
        // Once the Intent has been created, start the DetailActivity
        startActivity(intent);
    }

    /**
     * Updates the list of ingredients using SharedPreferences each time the user selects the recipe.
     *
     * Reference @see "https://discussions.udacity.com/t/not-sure-how-to-approach-widget-building/728592"
     */
    private void updateSharedPreference(Recipe recipe) {
        // Get a instance of SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the editor object
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the ingredient list and convert the list to string
        List<Ingredient> ingredientList = recipe.getIngredients();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Ingredient>>() {}.getType();
        String ingredientString = gson.toJson(ingredientList, listType);

        // Save the string
        editor.putString(getString(R.string.pref_ingredient_list_key), ingredientString);
        editor.apply();
    }

    /**
     * Sends the update broadcast message to the app widget.
     *
     * Reference: @see "https://stackoverflow.com/questions/10663800/sending-an-update-broadcast
     * -to-an-app-widget"
     */
    private void sendBroadcastToWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, RecipeWidgetProvider.class));

        Intent updateAppWidgetIntent = new Intent();
        updateAppWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateAppWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateAppWidgetIntent);
    }

    /**
     * Use BroadcastReceiver when checking the network connectivity status
     */
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    /**
     * Use NetworkCallback because the ability for a backgrounded application to receive network
     * connection state changes, android.net.conn.CONNECTIVITY_CHANGE, is deprecated for apps
     * targeting Android N or higher.
     *
     * Reference: @see "https://stackoverflow.com/questions/36421930/connectivitymanager-connectivity
     * -action-deprecated#36447866"
     */
    private void checkConnectionStateMonitor() {
        ConnectionStateMonitor connectionStateMonitor = new ConnectionStateMonitor();
        connectionStateMonitor.enable(this);
    }

    /**
     * Shows the network status in Snackbar
     * @param isConnected True if connected to the network
     */
    private void showSnack(boolean isConnected) {
        String message;
        if (isConnected) {
            message = "Connected to the Internet";
        } else {
            message = "No Internet Connection!";
        }

        Snackbar snackbar = Snackbar.make(mMainBinding.rv, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register connection status listener
        MyApp.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in the network connection.
     * Show snackbar message.
     * @param isConnected True if connected to the network
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
