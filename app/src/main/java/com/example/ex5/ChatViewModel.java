package com.example.ex5;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ai.client.generativeai.type.Content;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Content>> chatHistoryLiveData = new MutableLiveData<>();

    private final ChatManager modelManager;

    public ChatViewModel(Application application) {
        super(application);
        modelManager = ChatManager.getInstance(application.getApplicationContext(), R.string.system_prompt_game_chat);
        chatHistoryLiveData.setValue(modelManager.getChat().getHistory());
        sendMessage("");
    }

    public LiveData<List<Content>> getChatHistoryLiveData() {
        return chatHistoryLiveData;
    }

    public void sendMessage(String message) {
        modelManager.sendMessage(message, null, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                chatHistoryLiveData.postValue(modelManager.getChat().getHistory());
            }

            @Override
            public void onModelError(Throwable error) {
                // Handle error
            }
        });
        // next line is for having the user message displayed
        // in the chat and not waiting for the model response
        chatHistoryLiveData.postValue(modelManager.getChat().getHistory());
    }


}