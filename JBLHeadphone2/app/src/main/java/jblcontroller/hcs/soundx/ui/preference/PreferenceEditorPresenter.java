package jblcontroller.hcs.soundx.ui.preference;

import android.util.Log;

import java.util.Calendar;

import jblcontroller.hcs.soundx.base.BasePresenter;
import jblcontroller.hcs.soundx.utils.PreferrenceCalc;

public class PreferenceEditorPresenter extends BasePresenter {

    private PreferenceView mView;

    public PreferenceEditorPresenter(PreferenceView view) {
        mView = view;
    }

    /**
     * This method will return the preferred bass value based on formula
     */
    public void calculatePreferredBass(int year, int listeningExperience) {
        Log.d("TEST", "calculatePreferredBass  " + year + "  " + listeningExperience + " age " + getAge(year));

        double bass = PreferrenceCalc.getPreferredBass(getAge(year), listeningExperience);


        mView.getPreferredBass(bass);
    }

    /**
     * This method will return the preferred treble value based on formula
     */
    public void calculatePreferredTreble(int year, int listeningExperience, int gender) {
        Log.d("TEST", "calculatePreferredTreble  " + year + "  " + listeningExperience + " age " + getAge(year) + " gender " + gender);
        double treble = PreferrenceCalc.getPreferredTreble(getAge(year), listeningExperience, gender);
        mView.getPreferredTreble(treble);
    }

    private int getAge(int year) {
        int age;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        age = currentYear - year;
        Log.d("TEST", "age  " + age + "current year :: " + currentYear);
        return age;
    }
}
