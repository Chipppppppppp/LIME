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

    static final HookTarget USER_AGENT_HOOK = new HookTarget("q01.c", "h");
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("xA0.k", "onPageFinished");
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("nZ0.b", "G");
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("BJ.d$d", "run");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.l", "b");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.l", "a");
}
