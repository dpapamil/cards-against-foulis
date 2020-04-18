package com.papamilios.dimitris.cardsagainstfoulis.database;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUtils {

    public interface IFireBaseFunction {
        public void apply(DataSnapshot data);
    }

    static public void getData(@NonNull DatabaseReference ref, final IFireBaseFunction func) {
        ValueEventListener singleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                func.apply(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addListenerForSingleValueEvent(singleListener);
    }

    static public void getData(@NonNull String path, final IFireBaseFunction func) {
        getData(FirebaseDatabase.getInstance().getReference().child(path), func);
    }
}
