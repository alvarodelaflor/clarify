package es.clarify.clarify.Utilities;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.Objects.ShoppingCartLocal;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.Objects.UserDataLocal;
import es.clarify.clarify.ShoppingCart.ShoppingCart;
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

    public List<PurchaseLocal> getAllPurchaseLocalOwnerLogin() {
        List<PurchaseLocal> res;
        try {
            Realm realm = Realm.getDefaultInstance();
            String uidUser = new GoogleUtilities().getCurrentUser().getUid();
            ShoppingCartLocal aux = realm.where(ShoppingCartLocal.class).findFirst();
            res = new ArrayList<>(realm.copyFromRealm(aux.getPurcharse()));
            realm.close();
        } catch (Exception e) {
            Log.e(TAG, "getAllPurchaseLocalOwnerLogin: ", e);
            res = new ArrayList<>();
        }
        return res;
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

    public ShoppingCartLocal synchronizeShoppingCart(ShoppingCartRemote shoppingCartRemote) {
        ShoppingCartLocal res;
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            ShoppingCartLocal shoppingCartLocal = realm.createObject(ShoppingCartLocal.class, shoppingCartRemote.getIdFirebase());
            Boolean own = shoppingCartRemote.getOwn();
            Date lastUpdate = shoppingCartRemote.getLastUpdate();
            RealmList<String> allowUsersLocal = new RealmList<>();
            List<String> allowUsers = shoppingCartRemote.getAllowUsers();
            if (allowUsers != null) {
                allowUsersLocal.addAll(allowUsers);
            }

            shoppingCartLocal.setOwn(own);
            shoppingCartLocal.setLastUpdate(lastUpdate);
            shoppingCartLocal.setAllowUsers(allowUsersLocal);
            shoppingCartLocal.getPurcharse().addAll(syncronizePurcharse(shoppingCartRemote));
            realm.commitTransaction();
            res = realm.copyFromRealm(shoppingCartLocal);
        } catch (Exception e) {
            Log.e(TAG, "synchronizeShoppingCart: ", e);
            res = null;
        }
        return res;
    }

    private RealmList<PurchaseLocal> syncronizePurcharse(ShoppingCartRemote shoppingCartRemote) {
        RealmList<PurchaseLocal> res = new RealmList<>();
        try {
            List<PurchaseRemote> aux = shoppingCartRemote.getPurcharse();
            for (PurchaseRemote elem : aux) {
                Realm realm = Realm.getDefaultInstance();

                int id = elem.getIdFirebase();
                int idFirebase = elem.getIdFirebase();
                int idScannedTag = elem.getIdScannedTag();
                String idShoppingCart = elem.getIdShoppingCart();
                String name = elem.getName();

                PurchaseLocal purchaseLocal = realm.createObject(PurchaseLocal.class, id);
                purchaseLocal.setIdFirebase(idFirebase);
                purchaseLocal.setIdScannedTag(idScannedTag);
                purchaseLocal.setIdShoppingCart(idShoppingCart);
                purchaseLocal.setName(name);

                res.add(realm.copyFromRealm(purchaseLocal));
            }
        } catch (Exception e) {
            Log.e(TAG, "syncronizePurcharse: ", e);
        }
        return res;
    }

    public void updateShoppingCartLocal(ShoppingCartLocal shoppingCartLocal, ShoppingCartRemote shoppingCartRemote) {
        Realm realm = Realm.getDefaultInstance();
        List<PurchaseLocal> purchaseLocals = shoppingCartLocal.getPurcharse();
        List<PurchaseRemote> purchaseRemotes = shoppingCartRemote.getPurcharse();
        // First we delete the purchases that no longer exist in remote
        List<PurchaseLocal> toDelete =  purchaseLocals != null
                && purchaseRemotes != null
                && purchaseLocals.size() > 0
                && purchaseRemotes.size() > 0
                ?
                purchaseLocals.stream()
                .filter(x -> purchaseRemotes.stream().noneMatch(y -> y.getIdFirebase() == x.getIdFirebase()))
                .collect(Collectors.toList())
                : new ArrayList<>();
        for (PurchaseLocal elem : toDelete) {
            realm.beginTransaction();
            realm.where(PurchaseLocal.class).equalTo("id", elem.getId()).findFirst().deleteFromRealm();
            realm.commitTransaction();
        }

        if (purchaseRemotes == null) {
            realm.beginTransaction();
            RealmResults<PurchaseLocal> purchasesLocalToDelete = realm.where(PurchaseLocal.class).equalTo("idShoppingCart", shoppingCartLocal.getId()).findAll();
            if (purchasesLocalToDelete.size() > 1) {
                purchasesLocalToDelete.deleteAllFromRealm();
            }
            realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartLocal.getId()).findFirst().setPurcharse(new RealmList<>());
            realm.commitTransaction();
        }

        // Finally we add the new purchases
        List<PurchaseRemote> toAdd = purchaseLocals != null
                && purchaseRemotes != null
                &&purchaseLocals.size() > 0
                && purchaseRemotes.size() > 0
                ?
                purchaseRemotes.stream()
                .filter(x -> purchaseLocals.stream().noneMatch(y -> y.getIdFirebase() == x.getIdFirebase()))
                .collect(Collectors.toList())
                :
                new ArrayList<>();
        for (PurchaseRemote elem : toAdd) {
            PurchaseLocal check = realm.where(PurchaseLocal.class).equalTo("idFirebase", elem.getIdFirebase()).findFirst();
            if (check == null) {
                realm.beginTransaction();
                int id = elem.getIdFirebase();
                int idFirebase = elem.getIdFirebase();
                int idScannedTag = elem.getIdScannedTag();
                String idShoppingCart = elem.getIdShoppingCart();
                String name = elem.getName();
                PurchaseLocal purchaseLocal = realm.createObject(PurchaseLocal.class, id);
                purchaseLocal.setIdFirebase(idFirebase);
                purchaseLocal.setIdScannedTag(idScannedTag);
                purchaseLocal.setIdShoppingCart(idShoppingCart);
                purchaseLocal.setName(name);
                realm.commitTransaction();

                realm.beginTransaction();
                shoppingCartLocal.getPurcharse().add(purchaseLocal);
                realm.commitTransaction();
            }
        }

        realm.close();
    }

    public Boolean deletePurchaseFromLocal(PurchaseLocal purchaseLocal) {
        Boolean res;
        try {
            Realm realm = Realm.getDefaultInstance();
            PurchaseLocal aux = realm.where(PurchaseLocal.class).equalTo("id", purchaseLocal.getId()).findFirst();
            if (aux != null) {
                realm.beginTransaction();
                aux.deleteFromRealm();
                realm.commitTransaction();
            }
            realm.close();
            res = true;
        } catch (Exception e) {
            Log.e(TAG, "deletePurchaseFromLocal: ", e);
            res = false;
        }
        return res;
    }
}
