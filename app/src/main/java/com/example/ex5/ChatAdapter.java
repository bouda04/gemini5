package com.example.ex5;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_MODEL = 1;

    private List<ChatItem> chatHistory;
    private RecyclerView recyclerView;

    public ChatAdapter(List<ChatItem> chatHistory) {
        this.chatHistory = chatHistory;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView; // Store reference to RecyclerView
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void fillRowData(int position) {
            ChatItem chatItem = chatHistory.get(position);
            messageTextView.setText(chatItem.getMessage());
        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_USER) ? R.layout.user_message_item : R.layout.model_message_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatItem chatItem = chatHistory.get(position);
        //TextPart textPart = (TextPart) content.getParts().get(0);
        //String message = textPart.getText().trim();
        holder.fillRowData(position);
    }

    @Override
    public int getItemCount() {
        return this.chatHistory.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem chatItem = chatHistory.get(position);
        return (chatItem.getRole() != null && chatItem.getRole().equals("user")) ? VIEW_TYPE_USER : VIEW_TYPE_MODEL;
    }

//    public void updateData(List<Content> newChatHistory) {
//        int oldSize = this.chatHistory.size();
//        for (int i = 0; i < newChatHistory.size()- oldSize; i++){
//            chatHistory.add(newChatHistory.get(oldSize + i));
//            //notifyItemInserted(oldSize + i);
//        }
//        notifyDataSetChanged();
//        if (chatHistory.size() > 0)
//            recyclerView.postDelayed(() -> {
//                recyclerView.scrollToPosition(getItemCount() - 1);
//            }, 1000);
//            //this.recyclerView.post(() -> recyclerView.smoothScrollToPosition(chatHistory.size() - 1));
//
//    }
}