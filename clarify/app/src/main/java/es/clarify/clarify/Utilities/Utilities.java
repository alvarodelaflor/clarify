package es.clarify.clarify.Utilities;

import android.app.Activity;
import android.app.Dialog;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.clarify.clarify.NFC.NdefMessageParser;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.NFC.ParsedNdefRecord;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.Objects.ScannedTagRemote;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.Store.MyAdapter;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

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

    public void printInfo(Activity activity, NdefMessage[] msgs, final ImageView img, List<TextView> textViews, Dialog myDialog, Dialog myDialog_info, Button buttonAdd) {

        final List<TextView> params = new ArrayList<>(textViews);

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

            // Add here all TextView to modify in the NfcIdentifyFragment.java
            TextView text_company = params.get(0);
            TextView text_model = params.get(1);
            ImageView imgToChange = img;
            Dialog myDialogAux = myDialog;
            Dialog myDialogInfoAux = myDialog_info;
            Button buttonAddAux = buttonAdd;
            Activity activityAux = activity;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot scannedTagFirebase : dataSnapshot.getChildren()) {
                        ScannedTag scannedTag = scannedTagFirebase.getValue(ScannedTag.class);
                        this.text_company.setText(scannedTag.getBrand());
                        this.text_model.setText(scannedTag.getModel());
                        Picasso.get().load(scannedTag.getImage()).into(imgToChange);
                        new Database().addLastScannedTagLocalToChache(scannedTag);
                    }
                } else {
                    text_company.setText("Etiqueta no encontrada");
                    text_model.setText("Intentalo de nuevo");
                }
                myDialogInfoAux.dismiss();
                myDialogAux.show();
                buttonAddAux.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ScannedTagLocal scannedTagLocal = new Database().getLastScannedTag();
                        if (scannedTagLocal != null) {
                            Boolean result = addItemToPrivateStrore(scannedTagLocal, activityAux);
                            if (!result) {
                                Toast.makeText(activityAux, "¡No se pudo guardar!", Toast.LENGTH_LONG).show();
                            } else {
                                myDialogAux.dismiss();
                            }
                        }
                    }
                });
                myDialogAux.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface arg0) {
                        myDialog_info.show();
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

    public Boolean synchronizationWithFirebaseFirstLogin() {
        try {
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId);
            Query query = databaseReference.child("stores");
            query.addListenerForSingleValueEvent(new ValueEventListener() {

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
                                        scannedTagLocals.add(scannedTagLocal);

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
            Log.e("Ulities", "synchronizationWithFirebaseFirstLogin: ", e);
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

    public void showStoreListenerFirebase(MyAdapter adapter, String store) {
        try {
            Realm realm = Realm.getDefaultInstance();
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId).child("stores").child(store);
            databaseReference
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                                ScannedTagLocal insideItems = adapter.getItems().stream().filter(x -> x.getIdFirebase().equals(aux2.getIdFirebase())).findFirst().orElse(null);
                                if (insideItems != null) {
                                    int position = adapter.getItems().indexOf(insideItems);
                                    deleteItemFromPrivateStore(store, aux2.getIdFirebase());
                                    adapter.removeItem(position);
                                }
                            }

                            // Now save the new tags
                            RealmList<ScannedTagLocal> toSaveInStore = new RealmList<>();
                            for (ScannedTagRemote elem : scannedTagsFirebase) {
                                List<ScannedTagLocal> exitsTag2 = realm.where(ScannedTagLocal.class).equalTo("idFirebase", elem.getIdFirebase()).findAll();
                                if (exitsTag2.size() <= 0) {
                                    realm.beginTransaction();
                                    ScannedTagLocal scannedTagLocal = realm.createObject(ScannedTagLocal.class, realmDatabase.calculateIndex());
                                    scannedTagLocal.setStorageDate(null);
                                    scannedTagLocal.setIdFirebase(elem.getIdFirebase());
                                    scannedTagLocal.setBrand(elem.getBrand());
                                    scannedTagLocal.setModel(elem.getModel());
                                    scannedTagLocal.setLote(elem.getLote());
                                    scannedTagLocal.setColor(elem.getColor());
                                    scannedTagLocal.setExpiration_date(elem.getExpiration_date());
                                    scannedTagLocal.setReference(elem.getReference());
                                    scannedTagLocal.setImage(elem.getImage());
                                    scannedTagLocal.setStore(elem.getStore());
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
                            storeLocal.setLastUpdate(lastUpdateFirebase);
                            storeLocal.addNewScannedTagsLocal(toSaveInStore);
                            realm.commitTransaction();
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } catch (Exception e) {
            Log.e("Utilities", "updateDataFromFirebaseToLocal: ", e);
        }
    }

    public void storeListenerFirebase() {
        try {
            String userId = googleUtilities.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(("private")).child(userId).child("stores");
            databaseReference
                    .addValueEventListener(new ValueEventListener() {
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
                    });
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
}
