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

    static final HookTarget USER_AGENT_HOOK = new HookTarget("C81.c", "h");
    static final HookTarget WEBVIEW_CLIENT_HOOK = new HookTarget("qH0.m", "onPageFinished");
    static final HookTarget MUTE_MESSAGE_HOOK = new HookTarget("u71.b", "H");
    static final HookTarget MARK_AS_READ_HOOK = new HookTarget("JL.e$d", "run");
    static final HookTarget REQUEST_HOOK = new HookTarget("org.apache.thrift.k", "b");
    static final HookTarget RESPONSE_HOOK = new HookTarget("org.apache.thrift.k", "a");
}
