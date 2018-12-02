package com.papamilios.dimitris.cardsagainstfoulis.UI.chat;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;

import java.util.Collections;
import java.util.List;

public class ChatMessageListAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {

    private final LayoutInflater mInflater;
    private List<ChatMessage> mMessages = Collections.emptyList(); // Cached copy of messages

    public ChatMessageListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.chat_item, parent, false);
        ChatMessageViewHolder viewHolder = new ChatMessageViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage msg = mMessages.get(position);
        holder.setMessage(msg);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void addMessage(ChatMessage msg) {
        mMessages.add(msg);
        notifyDataSetChanged();
    }
}
