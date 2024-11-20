package io.github.hiro.lime.hooks;

public class Constants {
    public static final String PACKAGE_NAME = "jp.naver.line.android";
    public static final String MODULE_NAME = "io.github.hiro.lime";
    //TRADITIONAL_CHINESE
    static final HookTarget USER_AGENT_HOOK = new HookTarget("n91.c", "h");
    //HANDLED_AND_RETURN_TRUE
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("VH0.m", "onPageFinished");
    //NOTIFICATION_DISABLED
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("f81.b", "H");
    //PROCESSING
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("dM.d$d", "run");
    static final HookTarget Archive = new HookTarget("eB.Q", "invokeSuspend");
    //READNOTIFICATION
    static final HookTarget NOTIFICATION_READ_HOOK = new HookTarget("H91.c", "invokeSuspend");
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

