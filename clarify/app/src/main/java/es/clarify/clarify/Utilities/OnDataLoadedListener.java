package es.clarify.clarify.Utilities;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import es.clarify.clarify.Objects.ScannedTag;

interface OnDataLoadedListener {
    public void onFinishLoading(List<ScannedTag> data);
    public void onCancelled(DatabaseError firebaseError);
}
