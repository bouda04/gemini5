package com.example.ex5;

import android.content.Context;

public class MonitorManager extends ModelManager{
    private static volatile MonitorManager instance;

    private MonitorManager(Context context, int systemPromptId) {
        super(context, systemPromptId);
    }

    public static MonitorManager getInstance(Context context, int systemPromptId) {
        if (instance == null) {
            synchronized (MonitorManager.class) {
                if (instance == null) {
                    instance = new MonitorManager(context.getApplicationContext(), systemPromptId);
                }
            }
        }
        return instance;
    }

    public void analyzeHistory(String historyText, CallBacks callback) {
        String prompt = "Analyze this guessing game history and provide hints or critique:\n" + historyText;
        sendMessage(prompt, null, callback);
    }
}
