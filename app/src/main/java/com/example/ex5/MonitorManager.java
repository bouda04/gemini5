package com.example.ex5;

import android.content.Context;

public class MonitorManager extends ModelManager{
    private String type;
    public MonitorManager(Context context, int systemPromptId, String type) {
        super(context, systemPromptId);
        this.type = type;
    }

    public String getType(){
        return type;
    }
    public static MonitorManager createForCritique(Context context) {
        return new MonitorManager(context, R.string.system_prompt_monitor_criticize, "critique");
    }

    public static MonitorManager createForHint(Context context) {
        return new MonitorManager(context, R.string.system_prompt_monitor_hint, "hinter");
    }

    public static MonitorManager createForDetection(Context context) {
        return new MonitorManager(context, R.string.system_prompt_monitor_detect, "detector");
    }

    public void sendCurrentScript(String historyText, CallBacks callback) {
        String prompt ="להלן תסריט השיחה עד כה:" + "\n" + historyText;
        super.sendMessage(prompt, null, callback);
    }

}
