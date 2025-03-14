package com.example.ex5;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    ChatViewModel chatViewModel;
    RecyclerView rvChat;
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initiateAdapterAndViewModel();

        findViewById(R.id.sendButton).setOnClickListener(v -> {
            EditText etMessage = findViewById(R.id.etMessage);
            String message = etMessage.getText().toString();
            if (!message.isEmpty()) {
                chatViewModel.sendMessage(message);
                etMessage.setText("");
            }
        });
    }

    private void initiateAdapterAndViewModel(){
        rvChat = findViewById(R.id.rvChat);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatAdapter = new ChatAdapter(chatViewModel.getChatHistoryLiveData().getValue());
        rvChat.setAdapter(chatAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatViewModel.getChatHistoryLiveData().observe(this, chatHistory -> {
            chatAdapter.updateData(chatHistory);
        });
    }
}