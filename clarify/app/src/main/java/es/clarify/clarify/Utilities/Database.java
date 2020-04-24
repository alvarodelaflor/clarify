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
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Database {

    private String TAG = "REALM_DATABASE";

    public Database() {

    }

    public Boolean deleteAllDataFromDevice() {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "deleteAllDataFromDevice: ", e);
            return false;
        }
    }

    public int calculateIndex() {
        Realm realm = Realm.getDefaultInstance();
        Number currentIdNum = realm.where(ScannedTagLocal.class).max("id");
        int nextId;
        if (currentIdNum == null) {
            nextId = 0;
        } else {
            nextId = currentIdNum.intValue() + 1;
        }
        return nextId;
    }

    public List<ScannedTagLocal> getAllScannedTagLocal() {
        Realm realm = Realm.getDefaultInstance();
        List<ScannedTagLocal> res = new ArrayList<>();
        RealmResults<ScannedTagLocal> results = realm.where(ScannedTagLocal.class).findAll();
        if (results.size() > 0) {
            res = results.stream().collect(Collectors.<ScannedTagLocal>toList());
        }
        return res;
    }


    public Boolean updateLastUserLogin() {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.where(UserDataLocal.class).findAll().deleteAllFromRealm();
            realm.beginTransaction();
            UserDataLocal user = realm.createObject(UserDataLocal.class, calculateIndex());
            FirebaseUser firebaseUser = new GoogleUtilities().getCurrentUser();
            user.setEmail(firebaseUser.getEmail());
            user.setName(firebaseUser.getDisplayName());
            user.setPhoto(firebaseUser.getPhotoUrl().getHost());
            user.setUid(firebaseUser.getUid());
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "updateLastUserLogin: ", e);
            return false;
        }
    }


    /**
     * This method return all the {@link ScannedTagLocal} using a filter by {@link StoreLocal} name and limiting the number of result
     *
     * @param store
     * @param limit
     * @author alvarodelaflor.com
     * @since 16/04/2020
     */

    public RealmList<ScannedTagLocal> getScannedTagLocalPagination(String store, int limit) {
        Realm realm = Realm.getDefaultInstance();
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
        Realm realm = Realm.getDefaultInstance();
        StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", store).findFirst();
        if (store != null) {
            res = storeLocal.getLastUpdate();
        }
        return res;
    }

    public Long getNumberScannedTagLocalByStore(String store) {
        Realm realm = Realm.getDefaultInstance();
        Long res = realm.where(ScannedTagLocal.class).equalTo("store", store).count();
        return res;
    }

    public ScannedTagLocal getLastScannedTag() {
        Realm realm = Realm.getDefaultInstance();
        Integer id = Integer.parseInt(realm.where(ScannedTagLocal.class).max("id").toString());
        Date date = null;
        ScannedTagLocal res = realm.where(ScannedTagLocal.class).equalTo("id", id).equalTo("storageDate", date).findFirst();
        return res;
    }

    public Boolean synchronizeScannedTagLocal(ScannedTagLocal scannedTagLocal) {
        try {
            if (scannedTagLocal.getStorageDate() == null) {
                Realm realm = Realm.getDefaultInstance();
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
            Realm realm = Realm.getDefaultInstance();
            ScannedTagLocal toDelete = realm.where(ScannedTagLocal.class).equalTo("idFirebase", firebaseId).findFirst();
            if (toDelete != null) {
                realm.beginTransaction();
                toDelete.deleteFromRealm();
                realm.where(StoreLocal.class).equalTo("name", store).findFirst().setLastUpdate(new Date());
                realm.commitTransaction();
                realm.close();
            }
            return true;
        } catch (Error e) {
            Log.e(TAG, "deleteItemFromPrivateStore:", e);
            return false;
        }
    }

    public ScannedTagLocal getScannedTagByIdLocal(String id) {
        Realm realm = Realm.getDefaultInstance();
        ScannedTagLocal res = realm.where(ScannedTagLocal.class).equalTo("id", id).findFirst();
        return res;
    }

    public ScannedTagLocal getScannedTagByIdFirebase(String id) {
        Realm realm = Realm.getDefaultInstance();
        ScannedTagLocal res = realm.where(ScannedTagLocal.class).equalTo("idFirebase", id).findFirst();
        return res;
    }


    public void addLastScannedTagLocalToChache(ScannedTag scannedTag) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            Date date = null;
            realm.where(ScannedTagLocal.class).equalTo("storageDate", date).findAll().deleteAllFromRealm();
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
            realm.close();
        } catch (Error e) {
            Log.e(TAG, String.format("addScannedTagLocal: %s could not be saved.", scannedTag.toString()), e);
        }
    }

    public List<StoreLocal> getAllStoreLocal() {
        try {
            Realm realm = Realm.getDefaultInstance();
            List<StoreLocal> res = realm.where(StoreLocal.class).findAll();
            return res;
        } catch (Error e) {
            Log.e(TAG, "getAllStoreLocal: can not get all stores", e);
            return new ArrayList<>();
        }
    }

    public Boolean deleteAllStoreLocal() {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.delete(StoreLocal.class);
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Error e) {
            Log.e(TAG, "deleteAllStoreLocal: can not delete all stores", e);
            return false;
        }
    }

    public Boolean deleteStoreLocal(String store) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.where(ScannedTagLocal.class).equalTo("store", store).findAll().deleteAllFromRealm();
            realm.where(StoreLocal.class).equalTo("name", store).findAll().deleteAllFromRealm();
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "deleteStoreLocal: ", e);
            return false;
        }
    }

    public void deleteStoresLocal(List<String> stores) {
        for (String store :
                stores) {
            deleteStoreLocal(store);
        }
    }

    public ScannedTag getScannedTagFromLocal(ScannedTagLocal scannedTagLocal) {
        return new ScannedTag(scannedTagLocal.getIdFirebase(), scannedTagLocal.getBrand(), scannedTagLocal.getModel(), scannedTagLocal.getLote(), scannedTagLocal.getColor(), scannedTagLocal.getExpiration_date(), scannedTagLocal.getReference(), scannedTagLocal.getImage(), scannedTagLocal.getStore());
    }
}
