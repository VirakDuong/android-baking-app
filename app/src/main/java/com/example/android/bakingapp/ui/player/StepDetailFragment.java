package com.example.android.bakingapp.ui.player;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.databinding.FragmentStepDetailBinding;
import com.example.android.bakingapp.model.Step;

import timber.log.Timber;

import static com.example.android.bakingapp.utilities.Constant.SAVE_STEP;

/**
 * The StepDetailFragment displays a selected recipe step that includes a video and step instruction.
 */
public class StepDetailFragment extends Fragment {

    /** This field is used for data binding */
    private FragmentStepDetailBinding mStepDetailBinding;

    /** Member variable for Step that this fragment displays */
    private Step mStep;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public StepDetailFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Instantiate FragmentStepDetailBinding using DataBindingUtil
        mStepDetailBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_step_detail, container, false);
        View rootView = mStepDetailBinding.getRoot();

        // Load the saved state (the step) if there is one
        if (savedInstanceState != null) {
            mStep = savedInstanceState.getParcelable(SAVE_STEP);
        }

        // If the Step exists, set the description to the TextView
        // Otherwise, create a Log statement that indicates that the step was not found
        if(mStep != null) {
            String description = mStep.getDescription();
            // The Step 1 description of Brownies recipe contains a question mark, so replace it
            // with the degree sign.
            description = replaceString(description, rootView);
            mStepDetailBinding.tvDescription.setText(description);
        } else {
            Timber.v("This fragment has a null step");
        }

        // Return the rootView
        return rootView;
    }

    /**
     * Setter method for displaying current step
     */
    public void setStep(Step step) {
        mStep = step;
    }

    /**
     * Replace a question mark "�" with the degree "°".
     * The Step 1 description of Brownies recipe contains a question mark, so replace it with the degree sign.
     */
    private String replaceString(String target, View v) {
        if (target.contains(v.getContext().getString(R.string.question_mark))) {
            target = target.replace(v.getContext().getString(R.string.question_mark),
                    v.getContext().getString(R.string.degree));
        }
        return target;
    }

    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STEP, mStep);
    }
}