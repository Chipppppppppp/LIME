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

    static final HookTarget USER_AGENT_HOOK = new HookTarget("bZ0.c", "h");
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("hz0.l", "onPageFinished");
    static final HookTarget COMMUNICATION_ENUM_HOOK = new HookTarget("n01.Z5", "b");
    static final HookTarget UNSENT_HOOK = new HookTarget("IZ0.b", "p");
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("YX0.b", "G");
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("mJ.d$d", "run");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.l", "b");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.l", "a");
}
