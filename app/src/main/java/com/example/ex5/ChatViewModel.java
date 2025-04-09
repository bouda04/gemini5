package com.example.ex5;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ai.client.generativeai.type.Content;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Content>> chatHistoryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> monitorLiveData = new MutableLiveData<>();
    private final ChatManager chatManager;
    private Context appContext;
    private Handler handler;

    public ChatViewModel(Application application) {
        super(application);
        this.appContext = application.getApplicationContext();
        this.handler = new Handler(getApplication().getMainLooper());
        //chatManager = ChatManager.getInstance(application.getApplicationContext(), R.string.system_prompt_game_chat);
        chatManager = new ChatManager(application.getApplicationContext(), R.string.system_prompt_game_chat);
        chatHistoryLiveData.setValue(chatManager.getChat().getHistory());
        sendMessage("");
    }

    public LiveData<List<Content>> getChatHistoryLiveData() {
        return chatHistoryLiveData;
    }
    public LiveData<String> getMonitorLiveData() { return monitorLiveData; }
    public void startMonitor() {
        monitorLiveData.setValue(getApplication().getString(R.string.monitor_intro));
        MonitorManager detector = MonitorManager.createForDetection(appContext);
        String history = chatManager.getChatHistoryAsString();
        Log.i("monitor-history", "\n" + history);
        detector.sendCurrentScript(history, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                String[] parts = response.split(":");
                Log.i("monitor", response);
                String responseToDisplay = parts[2].trim();
                MonitorManager monitorAgent;
                if (parts[0].equals("user")) {
                    responseToDisplay += "\n" + getApplication().getString(R.string.wait_for_hint);
                    monitorAgent = MonitorManager.createForHint(appContext);
                }
                else if (parts[0].equals("model")) {
                    responseToDisplay += "\n" + getApplication().getString(R.string.wait_for_critics);
                    monitorAgent = MonitorManager.createForCritique(appContext);
                }
                else monitorAgent=null;
                monitorLiveData.postValue(responseToDisplay);
                if (monitorAgent != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generateReview(monitorAgent);
                        }
                    }, 4000);
                }
            }
            @Override
            public void onModelError(Throwable error) {}
        });
    }

    public void generateReview(MonitorManager modelAgent) {
        String history = chatManager.getChatHistoryAsString();
        modelAgent.sendCurrentScript(history, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                monitorLiveData.postValue(response);
            }

            @Override
            public void onModelError(Throwable error) {

            }
        });
    }
    public void sendMessage(String message) {
        chatManager.sendMessage(message, null, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                chatHistoryLiveData.postValue(chatManager.getChat().getHistory());
            }

            @Override
            public void onModelError(Throwable error) {
               Log.e("model error",error.getMessage());
            }
        });
        // next line is for having the user message displayed
        // in the chat and not waiting for the model response
        chatHistoryLiveData.postValue(chatManager.getChat().getHistory());
    }


}