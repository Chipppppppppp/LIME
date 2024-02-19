package io.github.chipppppppppp.lime;

public class LimeOptions {
    public class Option {
        public final String name;
        int id;
        public boolean checked;

        public Option(String name, int id, boolean checked) {
            this.name = name;
            this.id = id;
            this.checked = checked;
        }
    }

    public Option deleteVoom = new Option("delete_voom", R.string.switch_delete_voom, true);
    public Option deleteWallet = new Option("delete_wallet", R.string.switch_delete_wallet, true);
    public Option deleteNewsOrCall = new Option("delete_news_or_call", R.string.switch_delete_news_or_call, true);
    public Option distributeEvenly = new Option("distribute_evenly", R.string.switch_distribute_evenly, true);
    public Option deleteIconLabels = new Option("delete_icon_labels", R.string.switch_delete_icon_labels, true);
    public Option deleteAds = new Option("delete_ads", R.string.switch_delete_ads, true);
    public Option deleteRecommendation = new Option("delete_recommendation", R.string.switch_delete_recommendation, true);
    public Option deleteReplyMute = new Option("delete_reply_mute", R.string.switch_delete_reply_mute, true);
    public Option redirectWebView = new Option("redirect_webview", R.string.switch_redirect_webview, true);
    public Option openInBrowser = new Option("open_in_browser", R.string.switch_open_in_browser, false);
    public Option preventMarkAsRead = new Option("prevent_mark_as_read", R.string.switch_prevent_mark_as_read, false);
    public Option preventUnsendMessage = new Option("prevent_unsend_message", R.string.switch_prevent_unsend_message, false);
    public Option sendMuteMessage = new Option("mute_message", R.string.switch_send_mute_message, false);
    public Option deleteKeepUnread = new Option("delete_keep_unread", R.string.switch_delete_keep_unread, false);
    public Option[] options = {
            deleteVoom,
            deleteWallet,
            deleteNewsOrCall,
            distributeEvenly,
            deleteIconLabels,
            deleteAds,
            deleteRecommendation,
            deleteReplyMute,
            redirectWebView,
            openInBrowser,
            preventMarkAsRead,
            preventUnsendMessage,
            sendMuteMessage,
            deleteKeepUnread,
    };
}
