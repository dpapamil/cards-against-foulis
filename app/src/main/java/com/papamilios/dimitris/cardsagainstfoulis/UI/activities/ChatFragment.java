package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.chat.ChatMessageListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GameController;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    // Chat messages
    private ChatMessageListAdapter mChatMessageAdapter = null;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_chat, container, false);


        RecyclerView recyclerChatView = getActivity().findViewById(R.id.reyclerview_message_list);
        mChatMessageAdapter = new ChatMessageListAdapter(getActivity());
        recyclerChatView.setAdapter(mChatMessageAdapter);
        recyclerChatView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }


    public void addChatMessage(@NonNull ChatMessage chatMsg) {
        mChatMessageAdapter.addMessage(chatMsg);
    }

    // Handler for sending a chat message
    public void onSendChatMessage(View view) {
        TextView msgView = (TextView) getActivity().findViewById(R.id.edittext_chatbox);
        String msg = msgView.getText().toString();
        if (msg != null) {
            msgView.setText("");
            getGameActivity().controller().sendChatMessage(msg);
        }
    }

    private GameActivity getGameActivity() {
        return (GameActivity) getActivity();
    }

}
