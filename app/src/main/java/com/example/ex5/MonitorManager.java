package com.example.ex5;

import android.content.Context;

public class MonitorManager extends ModelManager{
    public MonitorManager(Context context, int systemPromptId) {
        super(context, systemPromptId);
    }

    public static MonitorManager createForCritique(Context context) {
        return new MonitorManager(context, R.string.system_prompt_monitor_criticize);
    }

    public static MonitorManager createForHint(Context context) {
        return new MonitorManager(context, R.string.system_prompt_monitor_hint);
    }

    public static MonitorManager createForDetection(Context context) {
        return new MonitorManager(context, R.string.system_prompt_monitor_detect);
    }

    public void sendCurrentScript(String historyText, CallBacks callback) {
        String prompt ="להלן תסריט השיחה עד כה:" + "\n" + historyText;
        super.sendMessage(prompt, null, callback);
    }

}
