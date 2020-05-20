package es.clarify.clarify.Utilities;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import es.clarify.clarify.Login.Login;
import es.clarify.clarify.Notifications.APIService;
import es.clarify.clarify.Notifications.Client;
import es.clarify.clarify.Notifications.Data;
import es.clarify.clarify.Notifications.MyFirebaseIdService;
import es.clarify.clarify.Notifications.MyResponse;
import es.clarify.clarify.Notifications.Sender;
import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.Objects.FriendRemote;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ScannedTagRemote;
import es.clarify.clarify.Objects.ShoppingCartLocal;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.Objects.UserData;
import es.clarify.clarify.R;
import es.clarify.clarify.ShoppingCart.ShoppingCart;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    public void updateFirebaseAccount(Context context) {
        FirebaseUser currentUser = getCurrentUser();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener((Activity) context, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.i("FCM Token", token);
                saveToken(token, currentUser);
            }
        });
        String token = "";
        UserData userData = new UserData(currentUser.getDisplayName(), currentUser.getEmail(), currentUser.getPhotoUrl().toString(), currentUser.getUid(), currentUser.getPhoneNumber(), token);
//        deleteFromFirebase("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"));
        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"), userData);
        ShoppingCartRemote shoppingCartLocal = new ShoppingCartRemote(getCurrentUser().getUid(),new Date(), true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        pushToFirebaseWithId("private", Arrays.asList(getCurrentUser().getUid(), "listaCompra"), shoppingCartLocal, getCurrentUser().getUid(), null);
    }

    private void saveToken(String token, FirebaseUser currentUser) {
        UserData userData = new UserData(currentUser.getDisplayName(), currentUser.getEmail(), currentUser.getPhotoUrl().toString(), currentUser.getUid(), currentUser.getPhoneNumber(), token);
//        deleteFromFirebase("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"));
        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "user_profile"), userData);
        ShoppingCartRemote shoppingCartLocal = new ShoppingCartRemote(getCurrentUser().getUid(),new Date(), true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        pushToFirebaseWithId("private", Arrays.asList(getCurrentUser().getUid(), "listaCompra"), shoppingCartLocal, getCurrentUser().getUid(), null);
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
                    if (activity!=null) {
                        Toast.makeText(activity, "¡Ya lo tenías guardado!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.i(TAG, "Push to Firebase: pushing object");
                    databaseReferenceFinal.push().setValue(value);
                    if (childs.size() > 2 && childs.get(childs.size() - 2).equals("stores")) {
                        pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", childs.get(childs.size() - 1), "lastUpdate"), new Date());
                    }
                    pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", "lastUpdate"), new Date());
                    if (activity != null) {
                        Toast.makeText(activity, "¡Guardado!", Toast.LENGTH_LONG).show();
                    }
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
                    if (dataSnapshot.hasChild(storeName)) {
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

    public Boolean deleteItemFromPrivateStore(String store, String firebaseId) {
        try {
            DatabaseReference databaseReference = database.getReference("private");
            databaseReference.child(getCurrentUser()
                    .getUid())
                    .child("stores")
                    .child(store)
                    .orderByChild("idFirebase")
                    .equalTo(firebaseId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                            pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", store, "lastUpdate"), new Date());
                            pushToFirebaseWithoutId("private", Arrays.asList(getCurrentUser().getUid(), "stores", "lastUpdate"), new Date());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "onCancelled", databaseError.toException());
                        }
                    });

            return true;
        } catch (Exception e) {
            Log.e(TAG, "deleteFromPrivateStore: could not delete", e);
            return false;
        }
    }

    public List<String> updateAllStoresByUserUID(String UID) {
        List<String> res = new ArrayList<>();

        return res;
    }

    public void deleteStore(String store) {
        DatabaseReference databaseReference = database.getReference("private");
        databaseReference.child(getCurrentUser()
                .getUid())
                .child("stores")
                .child(store)
                .removeValue();
    }

    public void deletePurchaseFromRemote(PurchaseLocal purchaseLocal, Boolean deleteAll, String uid) {
        DatabaseReference databaseReference = database.getReference("private");
        Query query = databaseReference.child(uid).child("listaCompra").orderByChild("idFirebase").equalTo(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (!deleteAll) {
                        ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                        PurchaseRemote purchaseRemote = shoppingCartRemote.getPurcharse() != null
                                ?
                                shoppingCartRemote.getPurcharse()
                                        .stream()
                                        .filter(x -> x.getIdFirebase() == purchaseLocal.getIdFirebase())
                                        .findFirst()
                                        .orElse(null)
                                :
                                null;
                        if (purchaseRemote != null) {
                            List<PurchaseRemote> purchaseRemotes = shoppingCartRemote.getPurcharse();
                            shoppingCartRemote.setLastUpdate(new Date());
                            shoppingCartRemote.getPurcharse().removeAll(purchaseRemotes.stream().filter(x -> x.getIdFirebase() == purchaseLocal.getIdFirebase()).collect(Collectors.toList()));
//                        new Database().deletePurchaseFromLocal(purchaseLocal);
                            databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("purcharse").setValue(shoppingCartRemote.getPurcharse());
                            databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("lastUpdate").setValue(shoppingCartRemote.getLastUpdate());
                        }
                    } else if (deleteAll) {
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("purcharse").setValue(new ArrayList<>());
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("lastUpdate").setValue(new Date());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void savePurchase(String query, int idFirebase, int idScannedTag, Boolean check, String uid) {
        DatabaseReference mReference = database.getReference("private")
                .child(uid);

        mReference.child("listaCompra")
                .orderByChild("idFirebase")
                .equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                            if (shoppingCartRemote != null) {
                                PurchaseRemote purchaseRemote = new PurchaseRemote(idFirebase, idScannedTag, uid, query, check);
                                List<PurchaseRemote> aux = shoppingCartRemote.getPurcharse();
                                if (aux == null) {
                                    aux = new ArrayList<>();
                                }
                                aux.add(purchaseRemote);
                                DatabaseReference purcharse = mReference.child(dataSnapshot.getKey()).child(data.getKey()).child("purcharse");
                                purcharse.setValue(aux);
                                DatabaseReference date = mReference.child(dataSnapshot.getKey()).child(data.getKey()).child("lastUpdate");
                                date.setValue(new Date());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void changeCheckStatusFromLocal(PurchaseLocal purchaseLocal, boolean checked, String uid) {
        DatabaseReference databaseReference = database.getReference("private");
        Query query = databaseReference.child(uid).child("listaCompra").orderByChild("idFirebase").equalTo(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    PurchaseRemote purchaseRemote = shoppingCartRemote.getPurcharse() != null
                            ?
                            shoppingCartRemote.getPurcharse()
                                    .stream()
                                    .filter(x -> x.getIdFirebase() == purchaseLocal.getIdFirebase())
                                    .findFirst()
                                    .orElse(null)
                            :
                            null;
                    if (purchaseRemote != null) {
                        List<PurchaseRemote> purchaseRemotes = shoppingCartRemote.getPurcharse();
                        shoppingCartRemote.setLastUpdate(new Date());
                        purchaseRemote.setCheck(checked);
                        purchaseRemotes.remove(purchaseRemote);
                        purchaseRemotes.add(purchaseRemote);
//                        new Database().deletePurchaseFromLocal(purchaseLocal);
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("purcharse").setValue(shoppingCartRemote.getPurcharse());
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("lastUpdate").setValue(shoppingCartRemote.getLastUpdate());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void removeAllPurchaseFromUserLogin() {
        String uid = getCurrentUser().getUid();
        DatabaseReference databaseReference = database.getReference("private");
        Query query = databaseReference.child(uid).child("listaCompra").orderByChild("idFirebase").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    if (shoppingCartRemote != null && shoppingCartRemote.getPurcharse() != null) {
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("purcharse").setValue(new ArrayList<>());
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("lastUpdate").setValue(shoppingCartRemote.getLastUpdate());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void changeStatusAllPurchaseFromUserLogin(Boolean status) {
        String uid = getCurrentUser().getUid();
        DatabaseReference databaseReference = database.getReference("private");
        Query query = databaseReference.child(uid).child("listaCompra").orderByChild("idFirebase").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    if (shoppingCartRemote != null && shoppingCartRemote.getPurcharse() != null) {
                        List<PurchaseRemote> purchaseRemotes = shoppingCartRemote.getPurcharse();
                        purchaseRemotes.stream().forEach(x -> x.setCheck(status));
                        shoppingCartRemote.setPurcharse(purchaseRemotes);
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("purcharse").setValue(shoppingCartRemote.getPurcharse());
                        databaseReference.child(uid).child("listaCompra").child(data.getKey()).child("lastUpdate").setValue(shoppingCartRemote.getLastUpdate());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void shareShoppingCart(String emailAux, Activity activity) {
        try {
            DatabaseReference databaseReference = database.getReference("private");
            DatabaseReference databaseReference2 = database.getReference("private");
            databaseReference.orderByChild("user_profile/email").equalTo(emailAux).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        FriendRemote friendRemote = null;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            DataSnapshot userProfile = data.child("user_profile");
                            friendRemote = new FriendRemote(
                                    userProfile.child("name").getValue(String.class),
                                    userProfile.child("email").getValue(String.class),
                                    userProfile.child("uid").getValue(String.class),
                                    false,
                                    userProfile.child("photo").getValue(String.class),
                                    getCurrentUser().getUid()
                            );
                            FriendRemote friendRemoteMe = new FriendRemote(
                                    getCurrentUser().getDisplayName(),
                                    getCurrentUser().getEmail(),
                                    getCurrentUser().getUid()+"invitation",
                                    false,
                                    getCurrentUser().getPhotoUrl().toString(),
                                    getCurrentUser().getUid());
                            String key = null;
                            for (DataSnapshot elem : data.child("listaCompra").getChildren()) {
                                key = elem.getKey();
                            }
                            ShoppingCartRemote shoppingCartRemote = data.child("listaCompra").child(key).getValue(ShoppingCartRemote.class);
                            if (key != null && shoppingCartRemote != null && friendRemote != null) {
                                List<FriendRemote> myFriends = shoppingCartRemote.getFriendInvitation();
                                FriendRemote check = myFriends != null ? myFriends.stream().filter(x -> x.getUid().equals(friendRemoteMe.getUid())).findFirst().orElse(null) : null;
                                final String uidFriend = friendRemote.getUid();
                                if (myFriends == null) {
                                    shoppingCartRemote.setFriendInvitation(Arrays.asList(friendRemoteMe));
                                    databaseReference2.child(uidFriend).child("listaCompra").child(key).child("friendInvitation").setValue(shoppingCartRemote.getFriendInvitation());
                                    databaseReference2.child(uidFriend).child("listaCompra").child(key).child("lastUpdate").setValue(new Date());
                                    saveNewAllowUser(friendRemote, activity);
                                    try {
                                        String title = "¡Nueva invitación!";
                                        String message = getCurrentUser().getDisplayName() + " te ha invitado a su lista";
                                        new Utilities().sendNotificationAux(userProfile.child("token").getValue(String.class), friendRemote.getUid(), message,title, ShoppingCartLocal.class, "goToShare",getCurrentUser().getPhotoUrl().toString());
                                    } catch (Exception e) {
                                        Log.e(TAG, "onDataChange: ", e);
                                    }
                                } else if (key != null &&  check == null) {
                                    myFriends.add(friendRemoteMe);
                                    shoppingCartRemote.setFriendInvitation(myFriends);
                                    databaseReference2.child(uidFriend).child("listaCompra").child(key).child("friendInvitation").setValue(shoppingCartRemote.getFriendInvitation());
                                    databaseReference2.child(uidFriend).child("listaCompra").child(key).child("lastUpdate").setValue(new Date());
                                    saveNewAllowUser(friendRemote, activity);
                                    try {
                                        String title = "¡Nueva invitación!";
                                        String message = getCurrentUser().getDisplayName() + " te ha invitado a su lita";
                                        new Utilities().sendNotificationAux(userProfile.child("token").getValue(String.class), friendRemote.getUid(), message,title, ShoppingCartLocal.class, "goToShare", getCurrentUser().getPhotoUrl().toString());
                                    } catch (Exception e) {
                                        Log.e(TAG, "onDataChange: ", e);
                                    }
                                } else {
                                    Toast.makeText(activity, "¡Ya le has enviado una invitación a este correo!", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(activity, "No se ha podido guardar" + friendRemote, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(activity, "No existe ese usuario", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "shareShoppingCart: ", e);
            Toast.makeText(activity.getApplicationContext(), "Vaya, se ha producido un error", Toast.LENGTH_LONG).show();
        }
    }

    private void saveNewAllowUser(FriendRemote friendRemote, Activity activity) {
        try {
            String uid = new GoogleUtilities().getCurrentUser().getUid();
            DatabaseReference mReference = database.getReference("private")
                    .child(uid);

            mReference.child("listaCompra")
                    .orderByChild("idFirebase")
                    .equalTo(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                                if (shoppingCartRemote != null) {
                                    List<FriendRemote> aux = shoppingCartRemote.getAllowUsers();
                                    if (aux == null) {
                                        aux = new ArrayList<>();
                                    }
                                    String uidFriend = friendRemote.getUid();
                                    friendRemote.setUid(uidFriend+"access");
                                    aux.add(friendRemote);
                                    DatabaseReference allowUsers = mReference.child(dataSnapshot.getKey()).child(data.getKey()).child("allowUsers");
                                    allowUsers.setValue(aux);
                                    DatabaseReference date = mReference.child(dataSnapshot.getKey()).child(data.getKey()).child("lastUpdate");
                                    date.setValue(new Date());
                                }
                            }
                            Toast.makeText(activity, "¡Invitación enviada!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "saveFriendToLocal: ", e);
            Toast.makeText(activity.getApplicationContext(), "Vaya, se ha producido un error", Toast.LENGTH_LONG).show();
        }
    }

    public void deleteAccessFriendFromRemote(FriendLocal friendLocal) {
        String uidMe = getCurrentUser().getUid();
        String uidFriend = friendLocal.getUid();
        String nameFriend = friendLocal.getName();
        DatabaseReference databaseReference = database.getReference("private");

        // First we delete the access from our list
        Query query1 = databaseReference.child(uidMe).child("listaCompra").orderByChild("idFirebase").equalTo(uidMe);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    if (shoppingCartRemote != null) {
                        List<FriendRemote> aux = shoppingCartRemote.getAllowUsers();
                        if (aux == null) {
                            aux = new ArrayList<>();
                        }
                        final String uidAux1 = friendLocal.getUid().replace("invitation", "access");
                        FriendRemote friendRemote = aux.stream().filter(x -> x.getUid().equals(uidAux1)).findFirst().orElse(null);
                        if (friendRemote != null) {
                            aux.remove(friendRemote);
                            DatabaseReference allowUsers = databaseReference.child(uidMe).child("listaCompra").child(data.getKey()).child("allowUsers");
                            allowUsers.setValue(aux);
                            DatabaseReference date = databaseReference.child(uidMe).child("listaCompra").child(data.getKey()).child("lastUpdate");
                            date.setValue(new Date());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Finally we delete the access from the invitation list
        uidFriend = uidFriend.replace("invitation", "");
        uidFriend = uidFriend.replace("access", "");
        final String uidFriendToFind = uidFriend;
        Query query2 = databaseReference.child(uidFriend);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String token = null;
                    String name = null;
                    try {
                        token = dataSnapshot.child("user_profile").child("token").getValue(String.class);
                        name = dataSnapshot.child("user_profile").child("name").getValue(String.class);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: no token found", e);
                    }
                    for (DataSnapshot data : dataSnapshot.child("listaCompra").getChildren()) {
                        ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                        if (shoppingCartRemote != null) {
                            List<FriendRemote> aux = shoppingCartRemote.getFriendInvitation();
                            if (aux == null) {
                                aux = new ArrayList<>();
                            }
                            FriendRemote friendRemote = aux.stream().filter(x -> x.getUid().equals(uidMe+"invitation")).findFirst().orElse(null);
                            if (friendRemote != null) {
                                aux.remove(friendRemote);
                                DatabaseReference friendInvitations = databaseReference.child(uidFriendToFind).child("listaCompra").child(data.getKey()).child("friendInvitation");
                                friendInvitations.setValue(aux);
                                DatabaseReference date = databaseReference.child(uidFriendToFind).child("listaCompra").child(data.getKey()).child("lastUpdate");
                                date.setValue(new Date());
                                if (token != null) {
                                    String message = getCurrentUser().getDisplayName() + " te ha eliminado el acceso a su lista";
                                    String title = "Invitación retirada";
                                    new Utilities().sendNotificationAux(token, uidFriendToFind, message, title, ShoppingCartLocal.class, "goToShare", getCurrentUser().getPhotoUrl().toString());
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deleteInvitation(FriendLocal friendLocal, Context context) {
        String uidMe = getCurrentUser().getUid();
        String uidFriend = friendLocal.getUid().replace("invitation", "").replace("access", "");
        DatabaseReference databaseReference = database.getReference("private");

        // First we delete the invitation from our list
        Query query1 = databaseReference.child(uidMe).child("listaCompra").orderByChild("idFirebase").equalTo(uidMe);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    if (shoppingCartRemote != null) {
                        List<FriendRemote> aux = shoppingCartRemote.getFriendInvitation();
                        if (aux == null) {
                            aux = new ArrayList<>();
                        }
                        FriendRemote friendRemote = aux.stream().filter(x -> x.getUid().equals(friendLocal.getUid())).findFirst().orElse(null);
                        if (friendRemote != null) {
                            aux.remove(friendRemote);
                            DatabaseReference allowUsers = databaseReference.child(uidMe).child("listaCompra").child(data.getKey()).child("friendInvitation");
                            allowUsers.setValue(aux);
                            DatabaseReference date = databaseReference.child(uidMe).child("listaCompra").child(data.getKey()).child("lastUpdate");
                            date.setValue(new Date());
                        } else {
                            Toast.makeText(context, "Vaya, habían retirado la invitación", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Finally we delete the access from the invitation list

        final String uidFriendToFind = friendLocal.getUid().replaceAll("access", "").replaceAll("invitation", "");
        Query query2 = databaseReference.child(uidFriend).child("listaCompra").orderByChild("idFirebase").equalTo(uidFriendToFind);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    if (shoppingCartRemote != null) {
                        List<FriendRemote> aux = shoppingCartRemote.getAllowUsers();
                        if (aux == null) {
                            aux = new ArrayList<>();
                        }
                        FriendRemote friendRemote = aux.stream().filter(x -> x.getUid().equals(uidMe+"access")).findFirst().orElse(null);
                        if (friendRemote != null) {
                            aux.remove(friendRemote);
                            DatabaseReference access = databaseReference.child(uidFriendToFind).child("listaCompra").child(data.getKey()).child("allowUsers");
                            access.setValue(aux);
                            DatabaseReference date = databaseReference.child(uidFriendToFind).child("listaCompra").child(data.getKey()).child("lastUpdate");
                            date.setValue(new Date());
                        }
                    } else {
                        Toast.makeText(context, "Vaya, habían retirado la invitación", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void acceptInvitation(FriendLocal friendLocal, Context context) {
        String uidMe = getCurrentUser().getUid();
        String uidFriend = friendLocal.getUid().replace("invitation", "").replace("access", "");
        DatabaseReference databaseReference = database.getReference("private");



        final String uidFriendToFind = friendLocal.getUid().replaceAll("access", "").replaceAll("invitation", "");
        Query query2 = databaseReference.child(uidFriend).child("listaCompra").orderByChild("idFirebase").equalTo(uidFriendToFind);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    if (shoppingCartRemote != null) {
                        List<FriendRemote> aux = shoppingCartRemote.getAllowUsers();
                        if (aux == null) {
                            aux = new ArrayList<>();
                        }
                        FriendRemote friendRemote = aux.stream().filter(x -> x.getUid().equals(uidMe+"access")).findFirst().orElse(null);
                        if (friendRemote != null) {
                            Integer index = aux.indexOf(friendRemote);
                            if (index != null) {
                                aux.remove(friendRemote);
                                friendRemote.setStatus(true);
                                aux.add(index, friendRemote);

                                DatabaseReference access = databaseReference.child(uidFriendToFind).child("listaCompra").child(data.getKey()).child("allowUsers");
                                access.setValue(aux);
                                DatabaseReference date = databaseReference.child(uidFriendToFind).child("listaCompra").child(data.getKey()).child("lastUpdate");
                                date.setValue(new Date());

                                Query query1 = databaseReference.child(uidMe).child("listaCompra").orderByChild("idFirebase").equalTo(uidMe);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                                            if (shoppingCartRemote != null) {
                                                List<FriendRemote> aux = shoppingCartRemote.getFriendInvitation();
                                                if (aux == null) {
                                                    aux = new ArrayList<>();
                                                }
                                                FriendRemote friendRemote = aux.stream().filter(x -> x.getUid().equals(friendLocal.getUid())).findFirst().orElse(null);
                                                if (friendRemote != null) {
                                                    int index = aux.indexOf(friendRemote);
                                                    aux.remove(friendRemote);
                                                    friendRemote.setStatus(true);
                                                    aux.add(index, friendRemote);

                                                    DatabaseReference allowUsers = databaseReference.child(uidMe).child("listaCompra").child(data.getKey()).child("friendInvitation");
                                                    allowUsers.setValue(aux);
                                                    DatabaseReference date = databaseReference.child(uidMe).child("listaCompra").child(data.getKey()).child("lastUpdate");
                                                    date.setValue(new Date());
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        } else {
                            Toast.makeText(context, "Vaya, habían retirado la invitación", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
