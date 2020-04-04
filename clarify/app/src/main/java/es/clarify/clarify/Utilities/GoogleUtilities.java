package es.clarify.clarify.Utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import es.clarify.clarify.Objects.ScannedTagRemote;
import es.clarify.clarify.Objects.UserData;
import es.clarify.clarify.R;

public class GoogleUtilities {

    private String TAG = "GoogleUtilities";
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
//        deleteFromFirebase("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"));
        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"), userData);
    }

    public void pushToFirebaseWithoutId(String reference, List<String> childs, Object value) {
        DatabaseReference databaseReference = database.getReference(reference);
        databaseReference.child(getCurrentUser().getUid());
        for (String child :
                childs) {
            databaseReference = databaseReference.child(child);
        }
        databaseReference.setValue(value);
    }

    public void pushToFirebaseWithId(String reference, final List<String> childs, final Object value, final String id, final Activity activity) {
        DatabaseReference databaseReference = database.getReference(reference);
        databaseReference.child(getCurrentUser().getUid());
        for (String child :
                childs) {
            databaseReference = databaseReference.child(child);
        }
        final DatabaseReference databaseReferenceFinal = databaseReference;
        databaseReference.orderByChild("idFirebase").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Log.i(TAG, "Push to Firebase: object already exist");
                    Toast.makeText(activity, "¡Ya lo tenías guardado!", Toast.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, "Push to Firebase: pushing object");
                    databaseReferenceFinal.push().setValue(value);
                    if (childs.size() > 2 && childs.get(childs.size()-2).equals("stores")) {
                        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", childs.get(childs.size() -1), "lastUpdate"), new Date());
                    }
                    pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", "lastUpdate"), new Date());
                    Toast.makeText(activity, "¡Guardado!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteFromFirebase(String reference, List<String> childs) {
        DatabaseReference databaseReference = database.getReference(reference);
        databaseReference.child(getCurrentUser().getUid());
        for (String child :
                childs) {
            databaseReference = databaseReference.child(child);
        }
        databaseReference.removeValue();
    }

    public Boolean addToStore(String store, ScannedTagRemote scannedTag, Activity activity) {
        try {
            String id = scannedTag.getIdFirebase();
            pushToFirebaseWithId("private", Arrays.asList(getCurrentUser().getUid(), "stores", store), scannedTag, id, activity);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addToStore: Coldn't add to store", e);
            return false;
        }
    }


    /**
     * The store is called the amacén where all the {@link es.clarify.clarify.Objects.ScannedTag} that the user has scanned and
     * therefore has his registration in local ({@link es.clarify.clarify.Objects.ScannedTagLocal}) will be stored. It is the data
     * from the latter that is used to be tagged using {@link ScannedTagRemote}.
     *
     * @author alvarodelaflor.com
     * @version 1.0.0 (2020/04/04)
     */

    /**
     * Create a new store using the name that user want.
     *
     * @author alvarodelaflor.com
     * @version 1.0.0 (2020/04/04)
     */
    public Boolean createStoreFirebase(final String storeName, final Activity activity) {
        try {
            final DatabaseReference databaseReference = database.getReference("private").child(getCurrentUser().getUid()).child("stores");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(storeName)){
                        Log.i(TAG, "Push to Firebase: store already exist");
                        Toast.makeText(activity, "¡Ya tenías creado ese almacen!", Toast.LENGTH_LONG).show();
                    } else {
                        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", storeName, "lastUpdate"), new Date());
                        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", "lastUpdate"), new Date());
                        Log.i(TAG, "Push to Firebase: suscefully store created");
                        Toast.makeText(activity, String.format("¡Se ha creado el almacen %s!", storeName), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        } catch (Error e) {
            Log.e(TAG, String.format("createStoreFirebase: couldn't create new store with name %s", storeName), e);
            return false;
        }
    }
}
