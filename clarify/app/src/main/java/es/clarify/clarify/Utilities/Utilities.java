package es.clarify.clarify.Utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.clarify.clarify.NFC.NdefMessageParser;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.NFC.ParsedNdefRecord;
import es.clarify.clarify.Notifications.APIService;
import es.clarify.clarify.Notifications.Client;
import es.clarify.clarify.Notifications.Data;
import es.clarify.clarify.Notifications.MyResponse;
import es.clarify.clarify.Notifications.Sender;
import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.Objects.FriendRemote;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.Objects.ScannedTagRemote;
import es.clarify.clarify.Objects.ShoppingCartLocal;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Search.NfcIdentifyFragment;
import es.clarify.clarify.ShoppingCart.ShoppingCart;
import es.clarify.clarify.Store.MyAdapter;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utilities {

    private NfcUtility nfcUtility = new NfcUtility();
    private GoogleUtilities googleUtilities = new GoogleUtilities();
    private Database realmDatabase = new Database();

    public Utilities() {

    }

    public NdefMessage[] getTagInfo(Intent intent) {

        // Get raw from TAG
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        NdefMessage[] msgs;

        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];

            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }

        } else {
            byte[] empty = new byte[0];
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] payload = nfcUtility.dumpTagData(tag).getBytes();
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
            msgs = new NdefMessage[]{msg};
        }
        return msgs;
    }

    public void printInfo(Activity activity, NdefMessage[] msgs) {
        Context context = activity.getApplicationContext();
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(("public"));
        String toSearch = builder.toString().replaceAll("\n", "");
        Query query = databaseReference.child("tags").orderByChild("id").equalTo(toSearch);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    NfcIdentifyFragment.moreInfo.setVisibility(View.VISIBLE);
                    NfcIdentifyFragment.addShoppingCart.setVisibility(View.VISIBLE);
                    NfcIdentifyFragment.buttonAdd.setVisibility(View.VISIBLE);
                    NfcIdentifyFragment.anotherTry.setVisibility(View.GONE);
                    for (DataSnapshot scannedTagFirebase : dataSnapshot.getChildren()) {
                        ScannedTag scannedTag = scannedTagFirebase.getValue(ScannedTag.class);
                        NfcIdentifyFragment.text_company.setText(scannedTag.getBrand());
                        NfcIdentifyFragment.text_model.setText(scannedTag.getModel());
                        NfcIdentifyFragment.price.setText(scannedTag.getPrice().toString() + " €");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
                        String date = "";
                        try {
                            LocalDate dateAux = LocalDate.parse(scannedTag.getExpiration_date(), formatter);
                            date = dateAux.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } catch (Exception e) {
                            Log.e("Parsing date", "onBindViewHolder: ", e);
                        }
                        NfcIdentifyFragment.expirationDate.setText(date);
                        Picasso.get().load(scannedTag.getImage()).into(NfcIdentifyFragment.img);
                        new Database().addLastScannedTagLocalToChache(scannedTag);
                        NfcIdentifyFragment.addShoppingCart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String product = scannedTag.getModel() + " " + scannedTag.getBrand();
                                Boolean check = new Utilities().savePurchase(product, Integer.parseInt(scannedTag.getId()), context, false, new GoogleUtilities().getCurrentUser().getUid());
                                if (check) {
                                    NfcIdentifyFragment.myDialog.dismiss();
                                    Toast.makeText(context, product + " se ha añadido", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else {
                    NfcIdentifyFragment.moreInfo.setVisibility(View.GONE);
                    Picasso.get().load(R.drawable.no_image).into(NfcIdentifyFragment.img);
                    NfcIdentifyFragment.addShoppingCart.setVisibility(View.GONE);
                    NfcIdentifyFragment.buttonAdd.setVisibility(View.GONE);
                    NfcIdentifyFragment.text_company.setText("ID inválido o inexistente");
                    NfcIdentifyFragment.text_model.setText("Producto no encontrado");
                    NfcIdentifyFragment.anotherTry.setVisibility(View.VISIBLE);
                    NfcIdentifyFragment.anotherTry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            NfcIdentifyFragment.myDialog.dismiss();
                        }
                    });
                }
                NfcIdentifyFragment.myDialogInfo.dismiss();
                NfcIdentifyFragment.myDialog.show();
                NfcIdentifyFragment.buttonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ScannedTagLocal scannedTagLocal = new Database().getLastScannedTag();
                        if (scannedTagLocal != null) {
                            Boolean result = addItemToPrivateStrore(scannedTagLocal, activity);
                            if (!result) {
                                Toast.makeText(activity, "¡No se pudo guardar!", Toast.LENGTH_LONG).show();
                            } else {
                                NfcIdentifyFragment.myDialog.dismiss();
                            }
                        }
                    }
                });
                NfcIdentifyFragment.myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface arg0) {
                        NfcIdentifyFragment.myDialogInfo.show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Boolean deleteItemFromPrivateStore(String store, String firebaseId) {
        try {
            Boolean aux1 = new GoogleUtilities().deleteItemFromPrivateStore(store, firebaseId);
            Boolean aux2 = new Database().deleteItemFromPrivateStore(store, firebaseId);
            return aux1 && aux2;
        } catch (RuntimeException e) {
            Log.e("Utilities", "deleteItemFromPrivateStore:", e);
            return false;
        }
    }

    public Boolean addItemToPrivateStrore(ScannedTagLocal scannedTagLocal, Activity activity) {
        try {
            ScannedTagRemote scannedTagRemote = new ScannedTagRemote(scannedTagLocal);
            Boolean aux1 = new GoogleUtilities().addToStore(scannedTagRemote.getStore(), scannedTagRemote, activity);
            Boolean aux2 = new Database().synchronizeScannedTagLocal(scannedTagLocal);
            return aux1 && aux2;
        } catch (Exception e) {
            Log.e("Utilities", "addItemToPrivateStroe: ", e);
            return false;
        }
    }

    /**
     * Sincronización de los elementos de firebase tras inicio (usuario no tiene nada a nivel local)
     *
     * @author alvarodelaflor.com
     * @since 19/04/2020
     */

    public Boolean synchronizationWithFirebaseFirstLoginTags() {
        try {
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId);
            Query query1 = databaseReference.child("stores");
            query1.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        Date lastUpdateStore = new Date();
                        for (DataSnapshot storeFirebase : dataSnapshot.getChildren()) {
                            if (!storeFirebase.getKey().equals("lastUpdate")) {
                                StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", storeFirebase.getKey()).findFirst();
                                if (storeLocal == null) {
                                    storeLocal = realm.createObject(StoreLocal.class, storeFirebase.getKey());
                                    storeLocal.setLastUpdate(storeFirebase.getValue(Date.class));
                                }
                                Iterable<DataSnapshot> scannedTagLocalFirebase = storeFirebase.getChildren();
                                RealmList<ScannedTagLocal> scannedTagLocals = new RealmList<>();
                                for (DataSnapshot scannedTagLocalFirebaseAux : scannedTagLocalFirebase) {
                                    if (!scannedTagLocalFirebaseAux.getKey().equals("lastUpdate")) {
                                        ScannedTagRemote scannedTag = scannedTagLocalFirebaseAux.getValue(ScannedTagRemote.class);
                                        ScannedTagLocal tagLocal = realm.where(ScannedTagLocal.class).equalTo("idFirebase", scannedTag.getIdFirebase()).findFirst();
                                        if (tagLocal == null) {
                                            ScannedTagLocal scannedTagLocal = realm.createObject(ScannedTagLocal.class, realmDatabase.calculateIndex());
                                            scannedTagLocal.setStorageDate(null);
                                            scannedTagLocal.setIdFirebase(scannedTag.getIdFirebase());
                                            scannedTagLocal.setBrand(scannedTag.getBrand());
                                            scannedTagLocal.setModel(scannedTag.getModel());
                                            scannedTagLocal.setLote(scannedTag.getLote());
                                            scannedTagLocal.setColor(scannedTag.getColor());
                                            scannedTagLocal.setExpiration_date(scannedTag.getExpiration_date());
                                            scannedTagLocal.setReference(scannedTag.getReference());
                                            scannedTagLocal.setImage(scannedTag.getImage());
                                            scannedTagLocal.setStore(scannedTag.getStore());
                                            scannedTagLocal.setPrice(scannedTag.getPrice());
                                            scannedTagLocals.add(scannedTagLocal);
                                        }
                                    } else {
                                        lastUpdateStore = scannedTagLocalFirebaseAux.getValue(Date.class);
                                    }
                                }
                                if (storeLocal != null) {
                                    storeLocal.addNewScannedTagsLocal(scannedTagLocals);
                                    storeLocal.setLastUpdate(lastUpdateStore);
                                }
                            }
                        }
                        realm.commitTransaction();
                        realm.close();
                        Thread.interrupted();
                    } else {
                        Thread.interrupted();
                        /*
                        TODO
                        No data was found in Firebase
                         */
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Thread.interrupted();
                }
            });
            return true;

        } catch (Exception e) {
            Log.e("Ulities", "synchronizationWithFirebaseFirstLoginTags: ", e);
            return false;
        }
    }

    public Boolean synchronizationWithFirebaseFirstLoginShoppingCart() {
        try {
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId);
            Query query2 = databaseReference.child("listaCompra");
            query2.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Realm realm = Realm.getDefaultInstance();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                            ShoppingCartLocal shoppingCartLocal = null;
                            if (shoppingCartRemote != null && shoppingCartRemote.getIdFirebase() != null) {
                                shoppingCartLocal = realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartRemote.getIdFirebase()).findFirst();
                            }
                            if (shoppingCartLocal == null) {
                                realmDatabase.synchronizeShoppingCart(shoppingCartRemote);
                            }

                        }
                        realm.close();
                        Thread.interrupted();
                    } else {
                        Thread.interrupted();
                        /*
                        TODO
                        No data was found in Firebase
                         */
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Thread.interrupted();
                }
            });
            return true;


        } catch (Exception e) {
            Log.e("Ulities", "synchronizationWithFirebaseFirstLoginShoppingCart: ", e);
            return false;
        }
    }

    /**
     * This method return de last update date of a store local. That is, the date of the last local change in this store.
     *
     * @param storeName
     * @author alvarodelaflor.com
     * @since 19/04/2020
     */

    public Date getLastUpdateByStore(@NonNull String storeName) {
        Date res = null;
        Realm realm = Realm.getDefaultInstance();
        StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", storeName).findFirst();
        if (storeLocal != null) {
            res = storeLocal.getLastUpdate();
        }
        return res;
    }

    /**
     * This method returns a map of the last change date of all stores.
     *
     * @author alvarodelaflor.com
     * @since 10/04/2020
     */

    public Map<String, Date> getLastUpdateAllStores() {
        Map<String, Date> res = new HashMap<>();
        try {
            List<StoreLocal> stores = realmDatabase.getAllStoreLocal();
            for (StoreLocal store :
                    stores) {
                Date lastUpdate = getLastUpdateByStore(store.getName());
                res.put(store.getName(), lastUpdate);
            }
        } catch (Exception e) {
            Log.e("Utilities", "getLastUpdateAllStores: ", e);
        }
        return res;
    }

    public void showStoreListenerFirebase(MyAdapter adapter, String store, ValueEventListener valueEventListener) {
        try {
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId).child("stores").child(store);
            databaseReference
                    .addValueEventListener(valueEventListener);
        } catch (Exception e) {
            Log.e("Utilities", "updateDataFromFirebaseToLocal: ", e);
        }
    }

    public ValueEventListener createStoreListenerFirebase() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date lastUpdateFirebase = null;
                Map<String, Date> lastUpadateAllStoresLocal = getLastUpdateAllStores();
                List<String> storesFirebase = new ArrayList<>();

                List<String> storesToSyncronize = new ArrayList<String>();
                List<String> newStores = new ArrayList<>();
                List<String> deleteStore = new ArrayList<>();

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot child : children) {

                    if (child.getKey().equals("lastUpdate")) {
                        lastUpdateFirebase = child.getValue(Date.class);
                    } else {
                        storesFirebase.add(child.getKey());
                    }
                }

                // First of all, if there is a new store, we add it together with all its labels
                newStores.addAll(storesFirebase.stream().filter(x -> !lastUpadateAllStoresLocal.keySet().contains(x)).collect(Collectors.toList()));
                updateStore(newStores, dataSnapshot);

                // Second, we delete the stores that no longer exist remotely
                deleteStore.addAll(lastUpadateAllStoresLocal.keySet());
                deleteStore.removeAll(storesFirebase);
                realmDatabase.deleteStoresLocal(deleteStore);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    public void storeListenerFirebase(ValueEventListener valueEventListener) {
        try {
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId).child("stores");
            databaseReference
                    .addValueEventListener(valueEventListener);
        } catch (Exception e) {
            Log.e("Utilities", "updateDataFromFirebaseToLocal: ", e);
        }
    }

    public Boolean updateStore(List<String> stores, DataSnapshot dataSnapshot) {
        try {
            Realm realm = Realm.getDefaultInstance();
            for (String store : stores) {
                realm.beginTransaction();
                Iterable<DataSnapshot> scannedTagLocalFirebase = dataSnapshot.child(store).getChildren();
                RealmList<ScannedTagLocal> scannedTagLocals = new RealmList<>();
                Date lastUpdateStore = null;
                for (DataSnapshot scannedTagLocalFirebaseAux : scannedTagLocalFirebase) {
                    if (!scannedTagLocalFirebaseAux.getKey().equals("lastUpdate")) {
                        ScannedTagRemote scannedTag = scannedTagLocalFirebaseAux.getValue(ScannedTagRemote.class);
                        ScannedTagLocal check = realm.where(ScannedTagLocal.class).equalTo("idFirebase", scannedTag.getIdFirebase()).findFirst();
                        if (check == null) {
                            ScannedTagLocal scannedTagLocal = realm.createObject(ScannedTagLocal.class, realmDatabase.calculateIndex());
                            scannedTagLocal.setStorageDate(null);
                            scannedTagLocal.setIdFirebase(scannedTag.getIdFirebase());
                            scannedTagLocal.setBrand(scannedTag.getBrand());
                            scannedTagLocal.setModel(scannedTag.getModel());
                            scannedTagLocal.setLote(scannedTag.getLote());
                            scannedTagLocal.setColor(scannedTag.getColor());
                            scannedTagLocal.setExpiration_date(scannedTag.getExpiration_date());
                            scannedTagLocal.setReference(scannedTag.getReference());
                            scannedTagLocal.setImage(scannedTag.getImage());
                            scannedTagLocal.setStore(scannedTag.getStore());
                            scannedTagLocal.setPrice(scannedTag.getPrice());
                            scannedTagLocals.add(scannedTagLocal);
                        }

                    } else {
                        lastUpdateStore = scannedTagLocalFirebaseAux.getValue(Date.class);
                    }
                }
                realm.commitTransaction();
                if (realm.where(StoreLocal.class).equalTo("name", store).findAll().size() <= 0) {
                    realm.beginTransaction();
                    StoreLocal storeLocal = realm.createObject(StoreLocal.class, store);
                    storeLocal.addNewScannedTagsLocal(scannedTagLocals);
                    storeLocal.setLastUpdate(lastUpdateStore);
                    realm.commitTransaction();
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("Utilities", "updateStore: ", e);
            return false;
        }
    }

    public ValueEventListener createValueEventListenerShowStore(MyAdapter adapter, String store) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Realm realm = Realm.getDefaultInstance();
                    Date lastUpdateFirebase = null;
                    RealmResults<ScannedTagLocal> res = realm.where(ScannedTagLocal.class).equalTo("store", store).findAll();
                    List<ScannedTagRemote> scannedTagsFirebase = new ArrayList<>();

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot child : children) {
                        if (child.getKey().equals("lastUpdate")) {
                            lastUpdateFirebase = child.getValue(Date.class);
                        } else {
                            ScannedTagRemote scannedTagFirebase = child.getValue(ScannedTagRemote.class);
                            scannedTagsFirebase.add(scannedTagFirebase);
                        }
                    }

                    // First we delete locally the tags that have been removed remotely
                    List<ScannedTagLocal> toDelete = new ArrayList<>();
                    toDelete.addAll(res);
                    for (ScannedTagRemote aux1 : scannedTagsFirebase) {
                        List<ScannedTagLocal> exitsTag = toDelete.stream().filter(x -> x.getIdFirebase().equals(aux1.getIdFirebase())).collect(Collectors.toList());
                        toDelete.removeAll(exitsTag);
                    }
                    for (ScannedTagLocal aux2 : toDelete) {
                        if (aux2 != null && adapter != null) {
                            ScannedTagLocal insideItems = adapter.getItems().stream().filter(x -> x.getIdFirebase().equals(aux2.getIdFirebase())).findFirst().orElse(null);
                            if (insideItems != null) {
                                int position = adapter.getItems().indexOf(insideItems);
                                deleteItemFromPrivateStore(store, aux2.getIdFirebase());
                                adapter.removeItem(position);
                            } else {
                                realmDatabase.deleteItemFromPrivateStore(store, aux2.getIdFirebase());
                                adapter.removeItem(-1);
                            }
                        }
                    }

                    // Now save the new tags
                    RealmList<ScannedTagLocal> toSaveInStore = new RealmList<>();
                    for (ScannedTagRemote elem : scannedTagsFirebase) {
                        List<ScannedTagLocal> exitsTag2 = realm.where(ScannedTagLocal.class).equalTo("idFirebase", elem.getIdFirebase()).findAll();
                        if (exitsTag2.size() <= 0) {
                            realm.beginTransaction();
                            ScannedTagLocal scannedTagLocal = realm.createObject(ScannedTagLocal.class, realmDatabase.calculateIndex());
                            scannedTagLocal.setStorageDate(elem.getStorageDate());
                            scannedTagLocal.setIdFirebase(elem.getIdFirebase());
                            scannedTagLocal.setBrand(elem.getBrand());
                            scannedTagLocal.setModel(elem.getModel());
                            scannedTagLocal.setLote(elem.getLote());
                            scannedTagLocal.setColor(elem.getColor());
                            scannedTagLocal.setExpiration_date(elem.getExpiration_date());
                            scannedTagLocal.setReference(elem.getReference());
                            scannedTagLocal.setImage(elem.getImage());
                            scannedTagLocal.setStore(elem.getStore());
                            scannedTagLocal.setPrice(elem.getPrice());
                            realm.commitTransaction();
                            toSaveInStore.add(scannedTagLocal);
                            adapter.addItem(scannedTagLocal);
                        }
                    }
                    StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", store).findFirst();
                    realm.beginTransaction();
                    if (store == null) {
                        storeLocal = realm.createObject(StoreLocal.class, store);
                    }
                    if (storeLocal != null) {
                        storeLocal.setLastUpdate(lastUpdateFirebase);
                        storeLocal.addNewScannedTagsLocal(toSaveInStore);
                    }
                    realm.commitTransaction();
                    adapter.notifyDataSetChanged();
                    realm.close();
                } catch (Error e) {
                    Log.e("TAG", "onDataChange: ", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    public ValueEventListener createListenerOwnShoppingCart() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Realm realm = Realm.getDefaultInstance();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                    ShoppingCartLocal shoppingCartLocal = null;
                    if (shoppingCartRemote != null && shoppingCartRemote.getIdFirebase() != null) {
                        shoppingCartLocal = realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartRemote.getIdFirebase()).findFirst();
                        if (shoppingCartLocal == null) {
                            realmDatabase.synchronizeShoppingCart(shoppingCartRemote);
                        } else if (shoppingCartLocal != null && shoppingCartLocal.getLastUpdate() != null && !realm.copyFromRealm(shoppingCartLocal).getLastUpdate().equals(shoppingCartRemote.getLastUpdate())) {
                            ShoppingCartLocal shoppingCartLocalCopy = realm.copyFromRealm(shoppingCartLocal);
                            realmDatabase.updateShoppingCartLocal(shoppingCartLocalCopy, shoppingCartRemote);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    public void listenerOwnShoppingCart(ValueEventListener valueEventListener) {
        String userId = googleUtilities.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(("private")).child(userId).child("listaCompra");
        databaseReference
                .addValueEventListener(valueEventListener);
    }

    public Boolean savePurchase(String query, int idScannedTag, Context context, Boolean check, String uid) {
        Boolean res = false;
        try {
            int id = realmDatabase.calculateIndexPurchase();
            Boolean saveLocal = realmDatabase.savePurchase(query, id, idScannedTag, check);
            if (saveLocal) {
                FriendRemote friendRemote = new FriendRemote();
                friendRemote.setName(googleUtilities.getCurrentUser().getDisplayName());
                friendRemote.setPhoto(googleUtilities.getCurrentUser().getPhotoUrl().toString());
                googleUtilities.savePurchase(query, id, idScannedTag, check, uid, friendRemote);
                res = true;
            } else {
                Toast.makeText(context, "Se ha producido un error al guardar", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            res = false;
            Log.e("Utilities", "savePurchase: ", e);
            Toast.makeText(context, "Se ha producido un error al guardar", Toast.LENGTH_SHORT).show();
        }
        return res;
    }

    public Boolean deleteAccessFriendFromLocal(FriendLocal friendLocal) {
        Boolean res = false;
        try {
            Realm realm = Realm.getDefaultInstance();
            final String originalUid = friendLocal.getUid();
            String uid = friendLocal.getUid().replace("invitation", "access");
            FriendLocal auxRealm = realm.where(FriendLocal.class).equalTo("uid", uid).findFirst();
            if (auxRealm != null) {
                FriendLocal aux = realm.copyFromRealm(auxRealm);
                realm.beginTransaction();
                auxRealm.deleteFromRealm();
                realm.commitTransaction();
                friendLocal.setUid(originalUid);
                googleUtilities.deleteAccessFriendFromRemote(friendLocal);
                res = true;
            }
        } catch (Exception e) {
            res = false;
            Log.e("Utilities", "deleteAccessFriendFromLocal: ", e);
        }
        return res;
    }

    public Boolean deleteAllAccessFriendFromLocal() {
        Boolean res;
        try {
            String uid = googleUtilities.getCurrentUser().getUid();
            Realm realm = Realm.getDefaultInstance();
            ShoppingCartLocal auxRealm = realm.where(ShoppingCartLocal.class).equalTo("id", uid).findFirst();
            if (auxRealm != null) {
                ShoppingCartLocal aux = realm.copyFromRealm(auxRealm);
                RealmList<FriendLocal> friendLocals = aux.getAllowUsers();
                if (friendLocals.size() > 0) {
                    for (FriendLocal elem : friendLocals) {
                        Boolean check = new Utilities().deleteAccessFriendFromLocal(elem);
                        if (!check) {
                            res = false;
                            break;
                        }
                    }
                }
            }
            realm.close();
            res = true;
        } catch (Exception e) {
            Log.e("Utilities", "deleteAllAccessFriendFromLocal: ", e);
            res = false;
        }
        return res;
    }

    public void sendNotificationAux(String token, String uidReceiver, String message, String title, String putExtra, String photo) {
        FirebaseUser firebaseUser = new GoogleUtilities().getCurrentUser();
        Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher,
                message, title, uidReceiver, putExtra, photo);

        Sender sender = new Sender(data, token);
        APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        apiService.sendNotification(sender)
                .enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.code() == 200){
                            if (response.body().success != 1){
                                Log.i("Utilities", "onResponse: error notification");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {

                    }
                });
    }

    public Integer getPhotoByStore(String store) {
        int res = R.drawable.box_opt;
        int frigorifico = R.drawable.fridge_opt;
        int despensa = R.drawable.despensa_opt;
        int medicamentos = R.drawable.medicamentos;
        if (store.equals("Frigorífico")) {
            res = frigorifico;
        } else  if (store.equals("Despensa")) {
            res = despensa;
        } else  if (store.equals("Medicamentos")) {
            res = medicamentos;
        }
        return res;
    }
}
