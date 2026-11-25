package io.github.chipppppppppp.lime.hooks;

import android.view.View;
import android.webkit.WebView;


import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveAds implements IHook {

    static void hookAdFacade(XC_LoadPackage.LoadPackageParam lp) {
        // Disabled: k03.b a/b turned out to be a thrift client whose receive
        // method crashes when its paired send is short-circuited. See
        // LINE26-FAILED-APPROACHES.md.
    }

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.removeAds.checked) return;
        hookAdFacade(loadPackageParam);

        // Block banner RPCs on BOTH sides. Blocking only the send side leaves
        // the matching receive call with no source and crashes the app
        // ("receiveSource is not set"), because LINE pairs every send with a
        // receive. Block both so neither side runs.
        // RPC blocking for banner ads is disabled: blocking the send side
        // ("getBanners" etc.) leaves the paired receive call with no source and
        // crashes the app ("receiveSource is not set"). The home banner is
        // Compose-rendered and cannot be hidden at the View layer either, so
        // the Leger ad fetch is left untouched here. See
        // LINE26-FAILED-APPROACHES.md.

        // SmartChannelViewLayout hook disabled: it touches the parent view and
        // may NPE if the layout is not attached. Re-enable only after confirming
        // it does not crash. See LINE26-FAILED-APPROACHES.md.

        // LadAdView hook disabled: getParent().getParent() may be null and NPE.
        // Re-enable only after verifying no crash.

        // addView Ad-hiding hook disabled for crash isolation.

        // WebView client hook disabled for crash isolation.
    }
}
