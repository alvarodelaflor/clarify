package es.clarify.clarify.Utilities;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.Objects.StoreLocal;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Database {

    private Realm realm = Realm.getDefaultInstance();
    private static final String TAG = "REALM_DATABASE";

    public Database(Realm realm) {
        this.realm = realm;
    }

    public Database() {

    }

    public int calculateIndex() {
        Realm real = Realm.getDefaultInstance();
        Number currentIdNum = real.where(ScannedTagLocal.class).max("id");
        int nextId;
        if (currentIdNum == null) {
            nextId = 0;
        } else {
            nextId = currentIdNum.intValue() + 1;
        }
        return nextId;
    }

    public List<ScannedTagLocal> getAllScannedTag() {
        List<ScannedTagLocal> res = new ArrayList<>();
        RealmResults<ScannedTagLocal> results = realm.where(ScannedTagLocal.class).findAll();
        if (results.size() > 0) {
            res = results.stream().collect(Collectors.<ScannedTagLocal>toList());
        }
        return res;
    }

    public RealmList<ScannedTagLocal> getScannedTagPagination(String store, int limit) {
        RealmList<ScannedTagLocal> res = new RealmList<>();
        RealmResults<ScannedTagLocal> aux = realm.where(ScannedTagLocal.class)
                .equalTo("store", store)
                .limit(limit)
                .findAll();
        res.addAll(aux);
        return res;
    }

    public ScannedTagLocal getLastScannedTag() {
        Integer id = Integer.parseInt(realm.where(ScannedTagLocal.class).max("id").toString());
        return realm.where(ScannedTagLocal.class).equalTo("id", id).findFirst();
    }

    public void synchronizeScannedTagLocal(String idFirebase, final String store) {
        try {
            ScannedTagLocal alreadyInDatabase = realm.where(ScannedTagLocal.class).equalTo("idFirebase", idFirebase).findFirst();
            if (alreadyInDatabase != null) {
                Log.i(TAG, "updateStoreLocal: updating store");
                realm.beginTransaction();
                alreadyInDatabase.setStore(store);
                final StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", store).findFirst();
                if (storeLocal == null){
                    StoreLocal storeLocalToSave = realm.createObject(StoreLocal.class, store);
                    storeLocalToSave.setLastUpdate(new Date());
                    RealmList<ScannedTagLocal> res = new RealmList<>();
                    res.add(alreadyInDatabase);
                    storeLocalToSave.setScannedTagLocals(res);
                } else {
                    storeLocal.addNewScannedTagLocal(alreadyInDatabase);
                }
                realm.commitTransaction();
            } else {
                Log.i(TAG, "updateStoreLocal: no scannedtag found");
            }
        } catch (Error e) {
            Log.e(TAG, "updateStoreLocal: an error appear", e);
        }
    }

    public Boolean deleteItemFromPrivateStore(String store, String firebaseId) {
        try {
            realm.beginTransaction();
            realm.where(ScannedTagLocal.class).equalTo("idFirebase", firebaseId).findFirst().deleteFromRealm();
            realm.where(StoreLocal.class).equalTo("name", store).findFirst().setLastUpdate(new Date());
            realm.commitTransaction();
            return true;
        } catch (Error e) {
            Log.e(TAG, "deleteItemFromPrivateStore:", e);
            return false;
        }
    }

    public ScannedTagLocal getScannedTagByIdLocal(String id) {
        return realm.where(ScannedTagLocal.class).equalTo("id", id).findFirst();
    }

    public ScannedTagLocal getScannedTagByIdFirebase(String id) {
        return realm.where(ScannedTagLocal.class).equalTo("idFirebase", id).findFirst();
    }


    public void addScannedTagLocal(final ScannedTag scannedTag) {
        try {
            ScannedTagLocal alreadyInDatabase = realm.where(ScannedTagLocal.class).equalTo("idFirebase", scannedTag.getId()).findFirst();
            if (alreadyInDatabase != null) {
                Log.i(TAG, "addScannedTagLocal: ScannedTagLocal already existed in the database.");
                realm.beginTransaction();
                alreadyInDatabase.setStorageDate(new Date());
                realm.commitTransaction();
            } else {
                Log.i(TAG, String.format("addScannedTagLocal: creating a new object in database with idFirebase %s", scannedTag.getId()));
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        int index = calculateIndex();
                        ScannedTagLocal realmScannedTagLocal = realm.createObject(ScannedTagLocal.class, index);
                        realmScannedTagLocal.setStorageDate(new Date());

                        realmScannedTagLocal.setIdFirebase(scannedTag.getId());
                        realmScannedTagLocal.setBrand(scannedTag.getBrand());
                        realmScannedTagLocal.setModel(scannedTag.getModel());
                        realmScannedTagLocal.setLote(scannedTag.getLote());
                        realmScannedTagLocal.setColor(scannedTag.getColor());
                        realmScannedTagLocal.setExpiration_date(scannedTag.getExpiration_date());
                        realmScannedTagLocal.setReference(scannedTag.getReference());
                        realmScannedTagLocal.setImage(scannedTag.getImage());
                    }
                });
            }
        } catch (Error e) {
            Log.e(TAG, String.format("addScannedTagLocal: %s could not be saved.", scannedTag.toString()), e);
        }
    }

    public List<StoreLocal> getAllStoreLocal() {
        List<StoreLocal> res = new ArrayList<>();
        try {
            return realm.where(StoreLocal.class).findAll();
        } catch (Error e) {
            Log.e(TAG, "getAllStoreLocal: can not get all stores", e);
            return res;
        }
    }

    public Boolean deleteAllStoreLocal() {
        try {
            realm.delete(StoreLocal.class);
            return true;
        } catch (Error e) {
            Log.e(TAG, "deleteAllStoreLocal: can not delete all stores", e);
            return false;
        }
    }

    public ScannedTag getScannedTagFromLocal(ScannedTagLocal scannedTagLocal) {
        return new ScannedTag(scannedTagLocal.getIdFirebase(), scannedTagLocal.getBrand(), scannedTagLocal.getModel(), scannedTagLocal.getLote(), scannedTagLocal.getColor(), scannedTagLocal.getExpiration_date(), scannedTagLocal.getReference(), scannedTagLocal.getImage());
    }
}
