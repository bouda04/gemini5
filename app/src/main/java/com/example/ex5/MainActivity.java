package com.example.ex5;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    ChatViewModel chatViewModel;
    RecyclerView rvChat;
    ChatAdapter chatAdapter;
    TextView monitorView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private Observer<String> monitorObserver=null;
    private FloatingActionButton fabToggle;

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
        rvChat = findViewById(R.id.rvChat);
        monitorView = findViewById(R.id.monitorView);
        fabToggle = findViewById(R.id.fabToggleBottomSheet);

        initiateBottomSheetRendering();
        initiateAdapterAndViewModel();

        findViewById(R.id.sendButton).setOnClickListener(v -> {
            EditText etMessage = findViewById(R.id.etMessage);
            String message = etMessage.getText().toString();
            if (!message.isEmpty()) {
                closeBottomSheet();
                chatViewModel.sendMessage(message);
                etMessage.setText("");
            }
        });
        fabToggle.setOnClickListener(v -> toggleBottomSheet());
    }

    private void toggleBottomSheet() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            closeBottomSheet();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            fabToggle.setImageResource(R.drawable.monitor); // שינוי לאייקון סגירה
        }
    }

    private void closeBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        fabToggle.setImageResource(R.drawable.monitor); // שינוי לאייקון פתיח
    }
    private void updateGuidelineToMatchBottomSheet(View bottomSheet) {
        Guideline guideline = findViewById(R.id.guideline);

        int[] bottomSheetLocation = new int[2];
        bottomSheet.getLocationOnScreen(bottomSheetLocation);
        int bottomSheetTop = bottomSheetLocation[1];

        int[] layoutLocation = new int[2];
        findViewById(R.id.rootLayout).getLocationOnScreen(layoutLocation);
        int layoutTop = layoutLocation[1];

        int relativeTop = bottomSheetTop - layoutTop;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guideBegin = relativeTop;
        guideline.setLayoutParams(params);
        rvChat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rvChat.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (chatAdapter != null && chatAdapter.getItemCount() > 0) {
                    rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            }
        });
    }
    private void initiateBottomSheetRendering(){
        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setDraggable(true);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Handle state changes like expanded, collapsed, hidden, etc.
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d("BottomSheet", "Expanded");
                        bottomSheet.setVisibility(View.VISIBLE);
                        fabToggle.setVisibility(View.GONE);
                        chatViewModel.getMonitorLiveData().observe(MainActivity.this, monitorObserver);
                        chatViewModel.startMonitor();

                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d("BottomSheet", "Collapsed");
                        bottomSheet.setVisibility(View.GONE);
                        fabToggle.setVisibility(View.VISIBLE);
                        chatViewModel.getMonitorLiveData().removeObserver(monitorObserver);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d("BottomSheet", "Hidden");
                        break;
                }
                bottomSheet.postDelayed(() -> {
                    updateGuidelineToMatchBottomSheet(bottomSheet);
                },1000);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                updateGuidelineToMatchBottomSheet(bottomSheet);
//                if (slideOffset < 0.1f) {
//                    // Forcefully collapse if close to bottom
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
            }
        });

    }
    private void initiateAdapterAndViewModel(){

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatAdapter = new ChatAdapter(chatViewModel.getChatHistoryLiveData().getValue());
        rvChat.setAdapter(chatAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatViewModel.getChatHistoryLiveData().observe(this, chatHistory -> {
            chatAdapter.updateData(chatHistory);
        });
        monitorObserver = monitorResponse -> {
            if (monitorResponse != null) {
                monitorView.setText(monitorResponse);
            }
        };
    }
}