package com.example.ex5;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

import java.util.List;

public class ChatViewModel extends AndroidViewModel implements ModelManager.CallBacks{

    private final MutableLiveData<List<Content>> chatHistoryLiveData = new MutableLiveData<>();

    private final ModelManager modelManager;

    public ChatViewModel(Application application) {
        super(application);
        modelManager = ModelManager.getInstance(application.getApplicationContext());
        chatHistoryLiveData.setValue(modelManager.getChat().getHistory());
        sendMessage("");
    }

    public LiveData<List<Content>> getChatHistoryLiveData() {
        return chatHistoryLiveData;
    }

    public void sendMessage(String message) {
        modelManager.sendMessage(message, new ModelManager.CallBacks() {
            @Override
            public void onModelSuccess(String response) {
                chatHistoryLiveData.postValue(modelManager.getChat().getHistory());
            }

            @Override
            public void onModelError(Throwable error) {
                // Handle error
            }
        });
        chatHistoryLiveData.postValue(modelManager.getChat().getHistory());
    }

    @Override
    public void onModelSuccess(String response) {

    }

    @Override
    public void onModelError(Throwable error) {

    }
}