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

    static final HookTarget USER_AGENT_HOOK = new HookTarget("ak1.c$c", "b");
    static final HookTarget WEBVIEW_HOOK = new HookTarget("k74.k", "onPageFinished");
    static final HookTarget COMMUNICATION_ENUM_HOOK = new HookTarget("rp5.id", "a");
    static final HookTarget UNSENT_HOOK = new HookTarget("mo5.c", "u");
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("cl5.b", "H");
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("wd1.e$d", "run");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.n", "a");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.n", "b");
}
