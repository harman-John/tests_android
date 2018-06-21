package jbl.stc.com.scan;

import android.content.Context;
import android.os.Build;

public class LeScannerCompat {

    public static BtScanner getLeScanner(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new LeJBScanner(context);
        } else {
            return new LeLollipopScanner(context);
        }
    }
}
