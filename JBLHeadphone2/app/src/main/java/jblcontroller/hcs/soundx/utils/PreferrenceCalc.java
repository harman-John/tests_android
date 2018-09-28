package jblcontroller.hcs.soundx.utils;

import android.util.Log;

public class PreferrenceCalc {

    public static  double getPreferredBass(double age, double listeningExperience)
    {
        double ageFactor = (age-10)/10;
        double prefreedBass = (-0.187* Math.pow(listeningExperience,2)+ (0.3361*listeningExperience)+8.25
                -((ageFactor-1)*0.9));
        Log.e("preferred Bass ", prefreedBass+"");
        return prefreedBass;
    }
    public static  double getPreferredTreble(double age, double listeningExperience, int gender)
    {
        double ageFactor = (age-10)/10;
        double exFTreble = (-0.2172* listeningExperience)- 0.6135+1.25;

        double preferredTreble = (0.0238*(Math.pow(ageFactor,3))) + (0.0562*(Math.pow(ageFactor,2)))-(1.032*ageFactor)+(0.2639-( gender* 1.9)) + exFTreble;
        Log.e("preferredTreble ", preferredTreble+"");
        return preferredTreble;
    }

}
