package com.papamilios.dimitris.cardsagainstfoulis.UI;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
*  The adapter for a list of cards.
 */

public class CardListAdapter extends RecyclerView.Adapter<CardViewHolder> implements CardViewHolder.OnItemSelectedListener {


    private final LayoutInflater mInflater;
    private List<Card> mCards = Collections.emptyList(); // Cached copy of words
    private int mSelectedPos = -1;
    private List<CardViewHolder> mViewHolders = new ArrayList<CardViewHolder>();

    public CardListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        CardViewHolder viewHolder = new CardViewHolder(itemView, this);
        mViewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        Card card = mCards.get(position);
        holder.setCard(card);
        holder.setChecked(position == mSelectedPos);
    }

    public void setCards(List<Card> cards) {
        mCards = cards;
        notifyDataSetChanged();
    }

    public Card getSelectedCard() {
        if (mSelectedPos >= 0 && mSelectedPos <= mCards.size()) {
            return mCards.get(mSelectedPos);
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    // Select the card with the given text
    public void setSelected(String cardText) {
        for (Card card : mCards) {
            if (card.getText().equals(cardText)) {
                mSelectedPos = -1;
                onSelectionChanged(card);
                break;
            }
        }
    }

    // Clear all selections
    public void clearSelection() {
        mSelectedPos = -1;
        notifyDataSetChanged();
    }

    // Enable/disable selection by clicking
    public void enableSelection(boolean enable) {
        for (CardViewHolder viewHolder : mViewHolders) {
            viewHolder.enableSelection(enable);
        }
    }

    @Override
    public void onSelectionChanged(Card card) {
        for (int i = 0; i < mCards.size(); i++) {
            if (card.equals(mCards.get(i))) {
                if (mSelectedPos == i) {
                    // Means we are unselecting this selection
                    mSelectedPos = -1;
                } else {
                    // We have a new selection
                    mSelectedPos = i;
                }
                notifyDataSetChanged();
                break;
            }
        }
    }
}


