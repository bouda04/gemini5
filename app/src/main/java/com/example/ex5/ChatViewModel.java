package com.example.ex5;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ChatItem>> chatHistoryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> monitorLiveData = new MutableLiveData<>();
    private List<ChatItem> chatHistory;
    private final ChatManager chatManager;
    private Context appContext;
    private Handler handler;
    private String pendingCritique;

    public ChatViewModel(Application application) {
        super(application);
        this.appContext = application.getApplicationContext();
        this.handler = new Handler(getApplication().getMainLooper());
        this.pendingCritique = null;
        //chatManager = ChatManager.getInstance(application.getApplicationContext(), R.string.system_prompt_game_chat);
        chatManager = new ChatManager(application.getApplicationContext(), R.string.system_prompt_game_chat);
        this.chatHistory = new ArrayList<ChatItem>();
        chatHistoryLiveData.setValue(chatHistory);
        sendMessage("");
    }

    public LiveData<List<ChatItem>> getChatHistoryLiveData() {
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
                if (modelAgent.getType().equals("critique")){
                    int index = response.indexOf(':');
                    pendingCritique = (index != -1 && index + 1 < response.length())
                            ? response.substring(index + 1).trim()
                            : null;
                }
                else
                    pendingCritique = null;
                monitorLiveData.postValue(response);
            }

            @Override
            public void onModelError(Throwable error) {

            }
        });
    }
    public void sendMessage(String message) {
        List<Content> history = chatManager.getChat().getHistory();

        // 1. Inject pending system critique if exists
        boolean injected;
        if (pendingCritique != null) {
            Content critiqueMessage = new Content("user", List.of(
                    new TextPart("[CRITIQUE-INTERNAL] " + pendingCritique)
            ));
            history.add(critiqueMessage);
            injected = true;
        }
        else injected = false;
        chatManager.sendMessage(message, null, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                if (injected)
                    cleanupInjectedCritique();
                pendingCritique = null; // 4. Clear stored critique
                chatHistory.add(new ChatItem("model",response));
                chatHistoryLiveData.postValue(chatHistory);
            }

            @Override
            public void onModelError(Throwable error) {
                if (injected)
                    cleanupInjectedCritique();
                Log.e("model error",error.getMessage());
            }

            private void cleanupInjectedCritique() {
                boolean removed = history.removeIf(content ->
                        "user".equals(content.getRole())
                                && content.getParts().size() > 0
                                && content.getParts().get(0) instanceof TextPart
                                && ((TextPart) content.getParts().get(0)).getText().startsWith("[CRITIQUE-INTERNAL]")
                );
                Log.i("model cleanup","the history was cleaned up from critiques injections");
            }

        });
        if (!message.isEmpty()){
            chatHistory.add(new ChatItem("user",message));
            chatHistoryLiveData.postValue(chatHistory);
        }
    }


}