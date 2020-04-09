package com.papamilios.dimitris.cardsagainstfoulis.UI.chat;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private final TextView mNameView;
    private final TextView mMessageView;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        mNameView = (TextView) itemView.findViewById(R.id.chat_name);
        mMessageView = (TextView) itemView.findViewById(R.id.chat_text);
    }

    public void setMessage(ChatMessage msg) {
        mNameView.setText(msg.getSenderName());
        mMessageView.setText(msg.getText());
    }
}
