package io.github.chipppppppppp.lime.hooks;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public interface IHook {
    void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam);
}
