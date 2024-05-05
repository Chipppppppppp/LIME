package io.github.chipppppppppp.lime;

import android.content.Context;
import android.view.View;

public class Utils {
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
