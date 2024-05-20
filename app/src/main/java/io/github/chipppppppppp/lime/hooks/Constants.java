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

    static final HookTarget USER_AGENT_HOOK = new HookTarget("kY0.c", "h");
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("ky0.l", "onPageFinished");
    static final HookTarget COMMUNICATION_ENUM_HOOK = new HookTarget("vZ0.a6", "b");
    static final HookTarget UNSENT_HOOK = new HookTarget("RY0.b", "p");
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("gX0.b", "H");
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("PI.e$d", "run");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.l", "a");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.l", "b");
}
