package com.example.ex5;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class ChatManager extends ModelManager{

    private static volatile ChatManager instance;
    private Chat chat;

    public ChatManager(Context context, int systemPromptId) {
        super(context, systemPromptId);
        chat = modelReference.startChat(Collections.emptyList());
    }

    public static ChatManager getInstance(Context context, int systemPromptId) {
        if (instance == null) {
            synchronized (ChatManager.class) {
                if (instance == null) {
                    instance = new ChatManager(context.getApplicationContext(), systemPromptId);
                }
            }
        }
        return instance;
    }

    @Override
    public void sendMessage(String input, Bitmap bitmap, CallBacks callback) {
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(input));

        if (bitmap != null)
            parts.add(new ImagePart(bitmap));

        Content message = new Content("user", parts);
        if (chat.getHistory().size() > 0) {
            chat.getHistory().add(message);
        }

        chat.sendMessage(message,
                new Continuation<GenerateContentResponse>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object result) {
                        if (result instanceof Result.Failure) {
                            callback.onModelError(((Result.Failure) result).exception);
                        } else {
                            chat.getHistory().remove(chat.getHistory().size() - 2);
                            callback.onModelSuccess(((GenerateContentResponse) result).getText());
                        }
                    }
                });
    }

    public List<Content> getChatHistory() {
        return chat.getHistory();
    }
    public Chat getChat() {
        return chat;
    }
    public void resetChat() {
        chat = modelReference.startChat(Collections.emptyList());
    }

    public String getChatHistoryAsString() {
        List<Content> history = chat.getHistory();
        StringBuilder builder = new StringBuilder();
        for (Content content : history) {
            String role = content.getRole();
            for (Part part : content.getParts()) {
                if (part instanceof TextPart) {
                    builder.append(role.equals("user") ? "user: " : "model: ");
                    builder.append(((TextPart) part).getText()).append("\n");
                }
            }
        }
        return builder.toString();
    }
}
