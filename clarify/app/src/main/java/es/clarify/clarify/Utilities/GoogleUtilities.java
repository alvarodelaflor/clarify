package es.clarify.clarify.Utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.UserData;
import es.clarify.clarify.R;

public class GoogleUtilities {

    private String TAG = "GoogleUtilities.java";
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public GoogleUtilities() {

    }

    public GoogleSignInOptions getGoogleSignInOptions(Context context) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    public GoogleSignInClient getmGoogleSignInClient(Activity activity) {
        return GoogleSignIn.getClient(activity, getGoogleSignInOptions(activity.getApplicationContext()));
    }


    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return getFirebaseAuth().getCurrentUser();
    }

    public void updateFirebaseAccount() {
        FirebaseUser currentUser = getCurrentUser();
        UserData userData = new UserData(currentUser.getDisplayName(), currentUser.getEmail(), currentUser.getPhotoUrl().toString(), currentUser.getUid(), currentUser.getPhoneNumber());
        deleteFromFirebase("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"));
        pushToFirebase("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"), userData);
    }

    public void pushToFirebase(String reference, List<String> childs, Object value) {
        DatabaseReference databaseReference = database.getReference(reference);
        databaseReference.child(getCurrentUser().getUid());
        for (String child:
             childs) {
            databaseReference = databaseReference.child(child);
        }
        databaseReference.push().setValue(value);
    }

    public void deleteFromFirebase(String reference, List<String> childs) {
        DatabaseReference databaseReference = database.getReference(reference);
        databaseReference.child(getCurrentUser().getUid());
        for (String child:
                childs) {
            databaseReference = databaseReference.child(child);
        }
        databaseReference.removeValue();
    }

    public Boolean addToStore(String store, ScannedTag scannedTag) {
        try {
            pushToFirebase("private", Arrays.asList(getCurrentUser().getUid(), "stores", store), scannedTag);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addToStore: Coldn't add to store", e);
            return false;
        }
    }
}
