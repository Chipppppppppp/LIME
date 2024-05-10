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

    static final HookTarget USER_AGENT_HOOK = new HookTarget("sM.b$c", "b");
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("hy0.k", "onPageFinished");
    static final HookTarget COMMUNICATION_ENUM_HOOK = new HookTarget("uZ0.a6", "b");
    static final HookTarget UNSENT_HOOK = new HookTarget("OY0.b", "p");
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("eX0.b", "H");
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("fJ.e$d", "run");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.m", "a");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.m", "b");
}
