package io.github.chipppppppppp.lime.hooks;

public class Constants {
    public static final String PACKAGE_NAME = "jp.naver.line.android";
    public static final String MODULE_NAME = "io.github.chipppppppppp.lime";

    public static class HookTarget {
        public String className;
        public String methodName;

        public HookTarget(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }

    // Hook targets for LINE 26.18.2 (versionCode 261820153).
    // Obfuscated names change on every LINE release; re-locate them with jadx when updating.

    // j53.b#k(): context-based builder producing "ANDROIDSECONDARY\t<ver>\tAndroid OS\t<os>"
    static final HookTarget USER_AGENT_HOOK = new HookTarget("j53.b", "k");
    // zw2.m0: WebViewClientCompat used by the in-app browser (holds IabWebViewCallbackImpl)
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("zw2.m0", "onPageFinished");
    // hs8.o1#a(cp8.b metadata, lm8.f muteType): writes the mute flag into outgoing message metadata
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("hs8.o1", "a");
    // MARK_AS_READ_HOOK removed: na3.e$d#run no longer exists in 26.x.
    // PreventMarkAsRead uses REQUEST_HOOK instead.
    // MainChatDataManager$getUnarchivedChatDataListOrderedByLastMessage$2#invokeSuspend
    static final HookTarget ARCHIVE_HOOK = new HookTarget("ei3.c1", "invokeSuspend");
    // hs8.s#b(int, Continuation): receive-operation handler for NOTIFIED_READ_MESSAGE
    static final HookTarget NOTIFICATION_READ_HOOK = new HookTarget("hs8.s", "b");
    // org.apache.thrift.o: abstract TServiceClient base; c = sendBase(name, args), b = receiveBase(name, result)
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.o", "c");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.o", "b");

    // Non-obfuscated classes that moved packages between releases
    static final String IN_APP_BROWSER_ACTIVITY = "com.linecorp.line.iab.browser.impl.InAppBrowserActivity";
    static final String WELCOME_FRAGMENT = "com.linecorp.line.registration.ui.fragment.WelcomeFragment";
}
