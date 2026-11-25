# Porting LIME to a new LINE release

Practical notes from bringing the module from LINE 15.x to 26.18.2.

## 1. Get the target APK

    adb shell pm path jp.naver.line.android
    adb shell su -c 'cp <base.apk path> /data/local/tmp/line.apk'
    adb pull /data/local/tmp/line.apk

Splits carry the native libs; `base.apk` alone is enough for class lookup.

## 2. Confirm which hook targets actually survive

Cheap check before decompiling anything:

    for c in <a/b/C> <d/e/F>; do
      printf '%s ' "$c"
      unzip -p line.apk "classes*.dex" | strings | grep -c "L$c;"
    done

A zero means the class is gone or renamed. **Do not trust `unzip` globs inside
`adb shell`** - the shell does not expand them, so every count comes back 0 and
looks like everything is missing. Pull the APK and run it on the host.

## 3. Decompile

    brew install jadx
    jadx --no-res --no-imports -d out line.apk

26.18.2 produces ~111k java files and takes several minutes.

## 4. Re-locate obfuscated targets by anchor, not by name

Obfuscated names change on every release. Find the class by something stable
around it:

- search the literal the code builds, e.g. `"Android OS"` for the user agent
  builder, `NOTIFIED_READ_MESSAGE` for the read handler
- search a unique method or field name that survived, e.g.
  `getUnarchivedChatDataListOrderedByLastMessage`
- search the constant/enum, e.g. `TO_BE_SENT_SILENTLY`

Then confirm the method signature matches what the hook expects.

## 5. Verify hooks really bound

`XposedBridge.hookAllMethods` **does not throw when nothing matches**. A hook
that silently matches zero methods looks identical to a working one. Log once
from inside the callback and confirm it prints.

`XposedHelpers.findAndHookMethod` does throw on a missing class or method, so a
hook wrapped in the per-hook try/catch that never logs a failure did bind.

## 6. Keep one failing hook from killing the rest

`Main.handleLoadPackage` runs the hook list in a bare loop. One
`ClassNotFoundException` aborts every hook after it - in this module that meant
the ad and tracking blocking at the end of the list never installed. Wrap each
`hook.hook(...)` in its own try/catch.

## 7. Traps found in practice

- **Stale module path.** LSPosed passes `startupParam.modulePath` captured at
  zygote init. Reinstalling the module while the target app runs leaves that
  path pointing at a deleted directory, and `AssetManager.addAssetPath` then
  throws `IOException`, which aborts whatever was being built (for this module,
  the settings entry). Resolve the path from `PackageManager` at call time.
- **Module resources are not merged into the host app.** Every `R.string` lookup
  from module code can throw `Resources$NotFoundException` (`No package ID 64`).
  Guard them.
- **`adb install` can silently keep the old APK.** Verify after installing:
  `pm path <pkg>`, then grep the installed dex for a string unique to the new
  build. `adb install --no-streaming` was reliable where plain `-r` was not.
- **Check the lock state before believing any UI test.** With
  `deviceLocked=1`, `uiautomator dump` returns lock-screen content and every
  conclusion drawn from it is wrong. `dumpsys trust | grep deviceLocked`.
- **Never run sqlite3 as root on an app database.** It creates `-shm`/`-wal`
  files owned by root; the app's uid then cannot open the database and crashes
  with `SQLITE_CANTOPEN`. If it happens, `chown -R <app uid>` the directory.
- The LIME settings button is a floating overlay pinned to the bottom-end of the
  settings list, not a row. OCR will not find it; grep the hierarchy for
  `text="LIME"`. Keep it clear of the gesture inset or it reads as absent.

## 8. What this port changed

Obfuscated targets for 26.18.2:

| hook | 26.18.2 |
|---|---|
| USER_AGENT | `j53.b#k` |
| WEBVIEW_CLIENT | `zw2.m0#onPageFinished` |
| MUTE_MESSAGE | `hs8.o1#a` |
| ARCHIVE | `ei3.c1#invokeSuspend` |
| NOTIFICATION_READ | `hs8.s#b` |
| REQUEST/RESPONSE | `org.apache.thrift.o#c` / `#b` |

`org.apache.thrift.n` is a `read`/`write` interface in this release - the
TServiceClient base is `org.apache.thrift.o`, and the methods are swapped
relative to older versions (`c` sends, `b` receives).

`MARK_AS_READ` (`na3.e$d#run`) is still unported: `sendChatChecked` is now a
coroutine rather than a `Runnable`.

## How this module identifies ads (read before porting RemoveAds)

The author's approach, from `RemoveAds.java` history:

- **PR #172** (`991138e`) replaced a hardcoded class list with a runtime
  heuristic: hook `ViewGroup.addView` and hide any view whose class name
  contains `"Ad"`, remembering newly seen classes.
- The hardcoded list it replaced documents the naming convention:
  `com.linecorp.line.ladsdk.ui.inventory.{home,wallet,album,timeline}.Lad*AdView`
  plus `com.google.android.gms.ads.nativead.NativeAdView` and
  `com.linecorp.square.v2.view.ad.common.SquareCommon*AdView`.

In LINE 26.18.2 that entire `Lad*AdView` inventory family is **gone** - only
`LadAdView` survives - and `ladsdk/ui/inventory/` now holds just `smartch/` and
`timeline/`. The `addView` heuristic therefore has nothing to match.

### The home promotional slot specifically

Measured, not inferred:

- logging every class passed to `addView` across the home tab yields 23 view
  types, none containing `Ad`
- the slot disappears when networking is disabled, so it is fetched live
- it is in no database or cache under `/data/user/0/jp.naver.line.android`
- tapping it opens `com.linecorp.liff.impl.LiffActivity`
- blocking `getBanners`, `getPrefetchableBanners`,
  `getBannersWithInCampaignPeriod`, the ladsdk inventory table, `b33.z`,
  `eh0.g` and `SmartChannelViewLayout` all leave it on screen
- `WebView.loadUrl` and `WebView.setWebViewClient` never fire, so the home slot
  is not a webview either - it is painted by Compose

So for this slot there is no view to hide and no hooked RPC to block. The one
hook that does act is the Leger facade request path
(`com.linecorp.line.ladsdk.impl.internal.network.legy.facade.*` implementing
`l03.a`, class `k03.b`), which stops ad fetches but does not remove an already
rendered slot.

## See also

`LINE26-FAILED-APPROACHES.md` records attempts that do not work on LINE 26 -
banner hiding by view class, adapter row type, resource id, overlay, request
blocking, and webview hooks - with what was measured and why each fails. Read it
before re-deriving the same dead ends.
