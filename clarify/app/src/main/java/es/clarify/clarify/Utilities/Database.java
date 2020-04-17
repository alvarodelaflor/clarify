package es.clarify.clarify.Utilities;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.Objects.UserDataLocal;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Database {

    private Realm realm = Realm.getDefaultInstance();
    private static final String TAG = "REALM_DATABASE";

    public Database() {

    }

    public Boolean deleteAllDataFromDevice() {
        try {
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "deleteAllDataFromDevice: ", e);
            return false;
        }
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

    public List<ScannedTagLocal> getAllScannedTagLocal() {
        List<ScannedTagLocal> res = new ArrayList<>();
        RealmResults<ScannedTagLocal> results = realm.where(ScannedTagLocal.class).findAll();
        if (results.size() > 0) {
            res = results.stream().collect(Collectors.<ScannedTagLocal>toList());
        }
        return res;
    }


    public Boolean updateLastUserLogin() {
        try {
            realm.where(UserDataLocal.class).findAll().deleteAllFromRealm();
            realm.beginTransaction();
            UserDataLocal user = realm.createObject(UserDataLocal.class, calculateIndex());
            FirebaseUser firebaseUser = new GoogleUtilities().getCurrentUser();
            user.setEmail(firebaseUser.getEmail());
            user.setName(firebaseUser.getDisplayName());
            user.setPhoto(firebaseUser.getPhotoUrl().getHost());
            user.setUid(firebaseUser.getUid());
            realm.commitTransaction();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "updateLastUserLogin: ", e);
            return false;
        }
    }


    /**
     *
     * This method return all the {@link ScannedTagLocal} using a filter by {@link StoreLocal} name and limiting the number of result
     *
     * @author alvarodelaflor.com
     * @since 16/04/2020
     * @param store
     * @param limit
     */

    public RealmList<ScannedTagLocal> getScannedTagLocalPagination(String store, int limit) {
        RealmList<ScannedTagLocal> res = new RealmList<>();
        RealmResults<ScannedTagLocal> aux = realm.where(ScannedTagLocal.class)
                .equalTo("store", store)
                .limit(limit)
                .findAll();
        res.addAll(aux);
        return res;
    }

    public Date getLastUpadateByStore(String store) {
        Date res = null;
        StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", store).findFirst();
        if (store != null) {
            res = storeLocal.getLastUpdate();
        }
        return res;
    }

    public Long getNumberScannedTagLocalByStore(String store) {
        return realm.where(ScannedTagLocal.class).equalTo("store", store).count();
    }

    public ScannedTagLocal getLastScannedTag() {
        Integer id = Integer.parseInt(realm.where(ScannedTagLocal.class).max("id").toString());
        Date date = null;
        return realm.where(ScannedTagLocal.class).equalTo("id", id).equalTo("storageDate", date).findFirst();
    }

    public Boolean synchronizeScannedTagLocal(ScannedTagLocal scannedTagLocal) {
        try {
            if (scannedTagLocal.getStorageDate() == null) {
                final StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", scannedTagLocal.getStore()).findFirst();
                realm.beginTransaction();
                scannedTagLocal.setStorageDate(new Date());
                if (storeLocal == null) {
                    StoreLocal storeLocalToSave = realm.createObject(StoreLocal.class, scannedTagLocal.getStore());
                    storeLocalToSave.setLastUpdate(new Date());
                    RealmList<ScannedTagLocal> res = new RealmList<>();
                    res.add(scannedTagLocal);
                    storeLocalToSave.setScannedTagLocals(res);
                } else {
                    storeLocal.addNewScannedTagLocal(scannedTagLocal);
                }
                realm.commitTransaction();
            }
            return true;
        } catch (Error e) {
            Log.e(TAG, "updateStoreLocal: an error appear", e);
            return false;
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


    public void addLastScannedTagLocalToChache(final ScannedTag scannedTag) {
        try {
            realm.beginTransaction();
            Date date = null;
            realm.where(ScannedTagLocal.class).equalTo("storageDate", date).findAll().deleteAllFromRealm();
            realm.commitTransaction();
            realm.beginTransaction();
            int index = calculateIndex();
            ScannedTagLocal realmScannedTagLocal = realm.createObject(ScannedTagLocal.class, index);

            realmScannedTagLocal.setStorageDate(null);
            realmScannedTagLocal.setIdFirebase(scannedTag.getId());
            realmScannedTagLocal.setBrand(scannedTag.getBrand());
            realmScannedTagLocal.setModel(scannedTag.getModel());
            realmScannedTagLocal.setLote(scannedTag.getLote());
            realmScannedTagLocal.setColor(scannedTag.getColor());
            realmScannedTagLocal.setExpiration_date(scannedTag.getExpiration_date());
            realmScannedTagLocal.setReference(scannedTag.getReference());
            realmScannedTagLocal.setImage(scannedTag.getImage());
            realmScannedTagLocal.setStore(scannedTag.getStore());
            realm.commitTransaction();
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
        return new ScannedTag(scannedTagLocal.getIdFirebase(), scannedTagLocal.getBrand(), scannedTagLocal.getModel(), scannedTagLocal.getLote(), scannedTagLocal.getColor(), scannedTagLocal.getExpiration_date(), scannedTagLocal.getReference(), scannedTagLocal.getImage(), scannedTagLocal.getStore());
    }
}
