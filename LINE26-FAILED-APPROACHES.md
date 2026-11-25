# Approaches that do not work on LINE 26 (and why)

Kept so the next port does not repeat them. Each entry states the idea, what was
observed, and the conclusion. Measurement method is noted so results can be
re-checked rather than trusted.

## 1. Hiding the home promotional banner by view class

**Idea:** hook `ViewGroup.addView` and hide any view whose class name contains
`Ad`, which is how this module has always found ads.

**Observed:** instrumenting that hook and logging every class added while the
home tab builds yields ~23 view types and none of them is ad-related.

**Conclusion:** the surface these classes belong to no longer exists in the app.
Do not add more class names to a list; the mechanism has nothing to match.

## 2. Hiding it through the feed adapter's row types

**Idea:** the home list is a RecyclerView with a typed adapter, and one row type
is named for a top banner. Filter that row out of the adapter.

**Observed:** the hook binds, but the count/item methods it targets are not the
ones in use, and the banner is unaffected.

**Conclusion:** the RecyclerView that carries those row types is not the one
rendering the current home tab. A RecyclerView adapter in the APK is not
evidence that it drives the live screen.

## 3. Hiding it via known resource ids

**Idea:** the recommendation and service rows have stable ids; hide by id.

**Observed:** both ids resolve and both views are hidden successfully. The banner
still renders.

**Conclusion:** the banner is not in either container. Hiding a correctly
resolved id is not proof of having found the right view.

## 4. Overlaying a block on the banner band

**Idea:** rather than identify it, cover the area with a full-width view.

**Observed:** the banner scrolls with the list, so a fixed overlay does not track
it. A global `onAttachedToWindow` hook to find the container added a layout
listener on every match and never removed them, and still did not hide the
banner. Reverted.

**Conclusion:** do not hook `View.onAttachedToWindow` app-wide to locate one
view. It costs work on every attach and leaks listeners. Scope to a fragment or
view-creation callback instead.

## 5. Blocking the banner request

**Idea:** the banner is fetched, so block the RPC that fetches it.

**Observed:** every banner-related request that could be identified was blocked,
and the ad SDK's inventory cache was emptied. The banner still renders. It
disappears only when networking is disabled.

**Conclusion:** the content is server-driven and none of the blocked paths
carries it. A `null` returned from a method whose caller expects a stream does
not stop that caller; it degrades silently. Blocking a request is not the same
as preventing a render.

## 6. Assuming a page is a webview

**Idea:** tapping the banner opens a hosted web app, so hook `WebView.loadUrl`
or `setWebViewClient` and strip the payload.

**Observed:** neither hook fires on the home tab.

**Conclusion:** the banner points at a hosted page but is not itself a webview.
The click destination tells you nothing about how the element is drawn.

## What the live view tree actually shows

Walking the decor view after layout, with screen coordinates: everything below
the search bar is a **single Compose host** occupying the full content area.
There is no child View for the banner, and none for the rows around it.

That is why every approach above fails. Compose draws into one host view; the
module's entire model is "find the child View and hide it". No amount of
additional class names, ids, or hooks will produce a child View that does not
exist.

## If this is attempted again

The only routes that can work are:

1. Skip a section when the content list is composed, i.e. intercept the
   composition input rather than its output.
2. Draw something above the Compose host and port its scrolling, which means
   tracking the host's scroll position.

Both are new capability, not a port. Neither is a constant or an id change.

## Method notes

- Verify an install actually replaced the APK before concluding anything from a
  missing log line: read the installed dex, do not trust the install command.
- `XposedBridge.hookAllMethods` does not throw when it matches nothing. A hook
  that never fires looks identical to one that works. Log once from inside the
  callback.
- Check whether the device is locked before trusting any UI observation.
- Confirming "view X is hidden" is not evidence the feature is gone. Confirm the
  user-visible outcome.

## 7. Blaming the module for broken chat images

**Symptom:** images sent in a chat never render; the message thumbnail is a
single flat colour while the sender's avatar beside it loads normally.

**What settled it:** the same thumbnail rendered identically with the module
disabled, so the module was not involved. The log showed image loading failing
to prepare the app's external files directory.

**Cause:** a SUSFS option that emulates vold's app-data mounts. With it enabled
the app cannot resolve its external files directory, so every image load fails.
Disabling it and rebooting restored image loading.

**Conclusion:** before attributing a rendering failure to the module, disable it
and reproduce on the same screen. Also check storage-level options on the device
before touching module code - an app that cannot reach its own files directory
will fail to load images regardless of what any Xposed hook does.

## Measurement that closes the question

Walking the live decor view after the home tab settles, with screen coordinates:
590 views in total, 66 with a non-zero size. Every sized view sits either above
the header, in the search bar, or at the bottom navigation. The entire content
area below the search bar is one Compose host spanning the full height.

Nothing is sized in the band where the banner appears. That is the whole finding:
there is no view to locate, hide, cover, or block at the View layer.

## Where the banner can still be attacked

Not attempted here, recorded so the option is not lost:

1. Intercept the list of sections the screen composes and drop the banner entry
   before composition.
2. Draw above the Compose host and mirror its scroll offset, so a cover tracks
   the banner as the list moves.

Both need composition-level work. Neither is a view hook, an id, or a constant.
