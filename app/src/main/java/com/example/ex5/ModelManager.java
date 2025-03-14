package com.example.ex5;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class ModelManager {

    private static final String TAG = "ModelManager";
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static volatile ModelManager INSTANCE;
    private final GenerativeModel generativeModel;
    private Chat chat;


    private ModelManager(Context context) {
        final String SYSTEM_PROMPT = context.getString(R.string.system_prompt);
        List<Part> parts = new ArrayList<Part>();
        parts.add(new TextPart(SYSTEM_PROMPT));
        generativeModel = new GenerativeModel(
                "gemini-2.0-flash",
                API_KEY,
                /* generation config */ null,
                /* safety setting */ null,
                /* request options */ new RequestOptions(),
                /* tools */ null,
                /* tool config */ null,
                /* system prompt */ new Content(parts)
        );
        chat = generativeModel.startChat(Collections.emptyList());
        //executorService = Executors.newSingleThreadExecutor();
    }
    public Chat getChat() {
        return chat;
    }

    public static ModelManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ModelManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModelManager(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void sendMessage(String prompt, CallBacks callback) {
        chat.getHistory().add(new Content("user", List.of(new TextPart(prompt))));
        chat.sendMessage(prompt,
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
                            chat.getHistory().remove(chat.getHistory().size()-2);
                            callback.onModelSuccess(((GenerateContentResponse) result).getText());
                        }
                    }
                }
        );
        //Log.d(TAG, "sendMessage: " + chat.getHistory().size());
    }


    /**
     * Converts a Bitmap to a Part object for sending to the Gemini model.
     *
     * @param bitmap The Bitmap to convert.
     * @return The Part object, or null if an error occurred.
     */
    private Part convertBitmapToPart(Bitmap bitmap) {
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return new ImagePart(bitmap); //  .ImageData(byteArray, ImageType.IMAGE_JPEG);
        } catch (Exception e) {
            Log.e(TAG, "Error converting Bitmap to Part: " + e.getMessage(), e);
            return null;
        }
    }

    public interface CallBacks{
        void onModelSuccess(String response);
        void onModelError(Throwable error);
    }
}