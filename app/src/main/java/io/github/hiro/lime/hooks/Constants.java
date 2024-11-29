package io.github.hiro.lime.hooks;

public class Constants {
    public static final String PACKAGE_NAME = "jp.naver.line.android";
    public static final String MODULE_NAME = "io.github.hiro.lime";
    //TRADITIONAL_CHINESE
    static final HookTarget USER_AGENT_HOOK = new HookTarget("Wc1.c", "h");
    //HANDLED_AND_RETURN_TRUE
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("OK0.l", "onPageFinished");
    //NOTIFICATION_DISABLED
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("Ob1.b", "H");
    //PROCESSING
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("WM.c$d", "run");

    //ChatListViewModel
    static final HookTarget Archive = new HookTarget("sB.Q", "invokeSuspend");
    //StreamingFetchOperationHandler
    static final HookTarget NOTIFICATION_READ_HOOK = new HookTarget("qd1.b", "invokeSuspend");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.l", "b");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.l", "a");

    public static class HookTarget {
        public String className;
        public String methodName;

        public HookTarget(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }
}

