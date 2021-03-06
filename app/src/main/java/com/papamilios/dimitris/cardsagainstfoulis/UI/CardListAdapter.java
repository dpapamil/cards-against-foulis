package com.papamilios.dimitris.cardsagainstfoulis.UI;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private List<Integer> mSelectedPos = new ArrayList<Integer>();
    private List<CardViewHolder> mViewHolders = new ArrayList<CardViewHolder>();
    private int mNumOfAllowedSelections = 0;

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
        holder.setChecked(mSelectedPos.contains(position));
    }

    public void setCards(List<Card> cards) {
        mCards = cards;
        notifyDataSetChanged();
    }

    public List<Card> getSelectedCards() {
        List<Card> selectedCards = new ArrayList<Card>();

        for (Integer pos : mSelectedPos) {
            if (pos >= 0 && pos <= mCards.size()) {
                selectedCards.add(mCards.get(pos));
            }
        }
        return selectedCards;
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    // Select the card with the given text
    public void setSelected(String cardText) {
        for (int i = 0; i < mCards.size(); i++) {
            Card card = mCards.get(i);
            if (card.getText().equals(cardText)) {
                if (!mSelectedPos.contains(i)) {
                    onSelectionChanged(card);
                }
                break;
            }
        }
    }

    // Clear all selections
    public void clearSelection() {
        mSelectedPos.clear();
        notifyDataSetChanged();
    }

    // Enable/disable selection by clicking
    public void enableSelection(boolean enable) {
        for (CardViewHolder viewHolder : mViewHolders) {
            viewHolder.enableSelection(enable);
        }
    }

    // Set the number of allowed selections
    public void setAllowedSelections(int num) {
        if (num < 0) {
            return;
        }
        mNumOfAllowedSelections = num;
        enableSelection(num > 0);
    }

    @Override
    public void onSelectionChanged(Card card) {
        for (int i = 0; i < mCards.size(); i++) {
            if (card.equals(mCards.get(i))) {
                if (mSelectedPos.contains(i)) {
                    // Means we are unselecting this selection
                    mSelectedPos.remove((Integer)i);
                } else {
                    // We have a new selection
                    if (mNumOfAllowedSelections > 1) {
                        if (mSelectedPos.size() < mNumOfAllowedSelections) {
                            mSelectedPos.add(i);
                        }
                    } else {
                        // We have single selection, so unselect the current selection
                        // and select this one
                        mSelectedPos.clear();
                        mSelectedPos.add(i);
                    }
                }
                notifyDataSetChanged();
                break;
            }
        }
    }
}


