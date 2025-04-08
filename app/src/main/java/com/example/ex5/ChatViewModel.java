package com.example.ex5;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ai.client.generativeai.type.Content;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Content>> chatHistoryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> monitorLiveData = new MutableLiveData<>();
    private final ChatManager modelManager;

    public ChatViewModel(Application application) {
        super(application);
        //modelManager = ChatManager.getInstance(application.getApplicationContext(), R.string.system_prompt_game_chat);
        modelManager = new ChatManager(application.getApplicationContext(), R.string.system_prompt_game_chat);
        chatHistoryLiveData.setValue(modelManager.getChat().getHistory());
        sendMessage("");
    }

    public LiveData<List<Content>> getChatHistoryLiveData() {
        return chatHistoryLiveData;
    }
    public LiveData<String> getMonitorLiveData() { return monitorLiveData; }
    public void startMonitor() {
        monitorLiveData.setValue(getApplication().getString(R.string.monitor_intro));
        MonitorManager detector = MonitorManager.createForDetection(getApplication().getApplicationContext());
        String history = modelManager.getChatHistoryAsString();
        Log.i("monitor-history", "\n" + history);
        detector.sendCurrentScript(history, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                Log.i("monitor", response);
                String[] parts = response.split(":");
                monitorLiveData.postValue(parts[2].trim());
            }

            @Override
            public void onModelError(Throwable error) {

            }
        });
    }
    public void sendMessage(String message) {
        modelManager.sendMessage(message, null, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                chatHistoryLiveData.postValue(modelManager.getChat().getHistory());
            }

            @Override
            public void onModelError(Throwable error) {
               Log.e("model error",error.getMessage());
            }
        });
        // next line is for having the user message displayed
        // in the chat and not waiting for the model response
        chatHistoryLiveData.postValue(modelManager.getChat().getHistory());
    }


}