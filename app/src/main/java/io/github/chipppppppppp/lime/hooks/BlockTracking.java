package io.github.chipppppppppp.lime.hooks;


import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class BlockTracking implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // Disabled: blocking the send side leaves the paired receive call
        // with no source and crashes the app ("receiveSource is not set").
    }
}
