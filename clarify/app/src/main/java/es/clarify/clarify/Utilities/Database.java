package es.clarify.clarify.Utilities;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.Objects.FriendRemote;
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

    public int calculateIndexPurchase() {
        Realm realm = Realm.getDefaultInstance();
        Number currentIdNum = realm.where(PurchaseLocal.class).max("id");
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
            Realm realm = Realm.getDefaultInstance();
            ScannedTagLocal scannedTagLocalFromRealm = realm.where(ScannedTagLocal.class).equalTo("id", scannedTagLocal.getId()).findFirst();
            if (scannedTagLocalFromRealm == null && scannedTagLocal.getStorageDate() == null) {
                StoreLocal storeLocal = realm.where(StoreLocal.class).equalTo("name", scannedTagLocal.getStore()).findFirst();
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
            ShoppingCartLocal shoppingCartLocal = realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartRemote.getIdFirebase()).findFirst();
            if (shoppingCartLocal == null) {
                shoppingCartLocal = realm.createObject(ShoppingCartLocal.class, shoppingCartRemote.getIdFirebase());
            }
            Boolean own = shoppingCartRemote.getOwn();
            Date lastUpdate = shoppingCartRemote.getLastUpdate();
            realm.commitTransaction();

            RealmList<FriendLocal> allowUsersLocal = new RealmList<>();
            List<FriendRemote> allowUsers = shoppingCartRemote.getAllowUsers();
            if (allowUsers != null) {
                for (FriendRemote elem : allowUsers) {
                    FriendLocal friendLocal = realm.where(FriendLocal.class).equalTo("uid", elem.getUid()).findFirst();
                    if (friendLocal == null) {
                        realm.beginTransaction();
                        friendLocal = realm.createObject(FriendLocal.class, elem.getUid());
                        friendLocal.setEmail(elem.getEmail());
                        friendLocal.setName(elem.getName());
                        friendLocal.setPhoto(elem.getPhoto());
                        friendLocal.setStatus(elem.getStatus());
                        realm.commitTransaction();
                    }
                    allowUsersLocal.add(realm.copyFromRealm(friendLocal));
                }
            }

            RealmList<FriendLocal> friendInvitationListLocal = new RealmList<>();
            List<FriendRemote> friendInvitationListRemote = shoppingCartRemote.getFriendInvitation();
            if (friendInvitationListRemote != null) {
                for (FriendRemote elem : friendInvitationListRemote) {
                    FriendLocal friendLocal = realm.where(FriendLocal.class).equalTo("uid", elem.getUid()).findFirst();
                    if (friendLocal == null) {
                        realm.beginTransaction();
                        friendLocal = realm.createObject(FriendLocal.class, elem.getUid());
                        friendLocal.setEmail(elem.getEmail());
                        friendLocal.setName(elem.getName());
                        friendLocal.setPhoto(elem.getPhoto());
                        friendLocal.setStatus(elem.getStatus());
                        realm.commitTransaction();
                    }
                    friendInvitationListLocal.add(realm.copyFromRealm(friendLocal));
                }
            }
            realm.beginTransaction();
            shoppingCartLocal.setOwn(own);
            shoppingCartLocal.setLastUpdate(lastUpdate);
            shoppingCartLocal.getAllowUsers().addAll(allowUsersLocal);
            shoppingCartLocal.getFriendInvitation().addAll(friendInvitationListLocal);
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
            if (aux != null) {
                for (PurchaseRemote elem : aux) {
                    Realm realm = Realm.getDefaultInstance();

                    int id = elem.getIdFirebase();
                    int idFirebase = elem.getIdFirebase();
                    int idScannedTag = elem.getIdScannedTag();
                    String idShoppingCart = elem.getIdShoppingCart();
                    String name = elem.getName();
                    Boolean check = elem.getCheck();

                    PurchaseLocal purchaseLocal = realm.createObject(PurchaseLocal.class, id);
                    purchaseLocal.setIdFirebase(idFirebase);
                    purchaseLocal.setIdScannedTag(idScannedTag);
                    purchaseLocal.setIdShoppingCart(idShoppingCart);
                    purchaseLocal.setName(name);
                    purchaseLocal.setCheck(check);

                    res.add(realm.copyFromRealm(purchaseLocal));
                }
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
        List<PurchaseLocal> toDelete = purchaseLocals != null
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
            realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartLocal.getId()).findFirst().getPurcharse().remove(elem);
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
                ?
                purchaseRemotes.stream()
                        .filter(x -> purchaseLocals
                                .stream()
                                .map(PurchaseLocal::getIdFirebase)
                                .allMatch(y -> y != x.getIdFirebase()))
                        .collect(Collectors.toList())
                :
                new ArrayList<>();
        for (PurchaseRemote elem : toAdd) {
            PurchaseLocal check = realm.where(PurchaseLocal.class).equalTo("idFirebase", elem.getIdFirebase()).findFirst();
            if (check != null) {
                realm.beginTransaction();
                check.deleteFromRealm();
                realm.commitTransaction();
            }
            realm.beginTransaction();
            int id = elem.getIdFirebase();
            int idFirebase = elem.getIdFirebase();
            int idScannedTag = elem.getIdScannedTag();
            String idShoppingCart = elem.getIdShoppingCart();
            String name = elem.getName();
            Boolean checkAtribute = elem.getCheck();

            PurchaseLocal purchaseLocal = realm.createObject(PurchaseLocal.class, id);
            purchaseLocal.setIdFirebase(idFirebase);
            purchaseLocal.setIdScannedTag(idScannedTag);
            purchaseLocal.setIdShoppingCart(idShoppingCart);
            purchaseLocal.setName(name);
            purchaseLocal.setCheck(checkAtribute);
            realm.commitTransaction();

            realm.beginTransaction();
            ShoppingCartLocal shoppingCartLocalAux = realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartLocal.getId()).findFirst();
            shoppingCartLocalAux.getPurcharse().add(realm.copyFromRealm(purchaseLocal));
            shoppingCartLocalAux.setLastUpdate(new Date());
            realm.commitTransaction();
        }

        // We now update the products that have been marked as check
        List<PurchaseLocal> toCheck = purchaseLocals != null && purchaseRemotes != null ?
                purchaseLocals.stream()
                .filter(x -> purchaseRemotes.stream().anyMatch(y -> x.getIdFirebase() == y.getIdFirebase() && x.getCheck() != y.getCheck()))
                .collect(Collectors.toList())
                : new ArrayList<>();
        for (PurchaseLocal elem : toCheck) {
            PurchaseLocal p = realm.where(PurchaseLocal.class).equalTo("id", elem.getId()).findFirst();
            if (p != null) {
                realm.beginTransaction();
                PurchaseRemote purchaseRemoteAux = purchaseRemotes.stream().filter(x -> x.getIdFirebase() == p.getIdFirebase()).findFirst().orElse(null);
                if (purchaseRemoteAux != null && purchaseRemoteAux.getCheck()!= null) {
                    p.setCheck(purchaseRemoteAux.getCheck());
                }
                realm.commitTransaction();
            }
        }

        // Finally, we update the users who have access to the shopping list
        List<FriendLocal> friendLocals = shoppingCartLocal.getAllowUsers();
        List<FriendRemote> friendRemotes = shoppingCartRemote.getAllowUsers();

        List<FriendLocal> toDeleteFriend = purchaseLocals != null
                && friendRemotes != null
                && friendLocals.size() > 0
                && friendRemotes.size() > 0
                ?
                friendLocals.stream()
                        .filter(x -> friendRemotes.stream().noneMatch(y -> y.getUid().equals(x.getUid())))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        for (FriendLocal elem : toDeleteFriend) {
            realm.beginTransaction();
            realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartLocal.getId()).findFirst().getAllowUsers().remove(elem);
            realm.where(FriendLocal.class).equalTo("uid", elem.getUid()).findFirst().deleteFromRealm();
            realm.commitTransaction();
        }

        if (friendRemotes == null) {
            realm.beginTransaction();
            RealmResults<FriendLocal> friendLocalToDelete = realm.where(FriendLocal.class).equalTo("idShoppingCart", shoppingCartLocal.getId()).findAll();
            if (friendLocalToDelete.size() > 1) {
                friendLocalToDelete.deleteAllFromRealm();
            }
            realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartLocal.getId()).findFirst().setAllowUsers(new RealmList<>());
            realm.commitTransaction();
        }

        // Finally we add the new access user to list
        List<FriendRemote> toAddFriend = friendLocals != null
                && friendRemotes != null
                ?
                friendRemotes.stream()
                        .filter(x -> friendLocals
                                .stream()
                                .map(FriendLocal::getUid)
                                .allMatch(y -> !y.equals(x.getUid())))
                        .collect(Collectors.toList())
                :
                new ArrayList<>();
        for (FriendRemote elem : toAddFriend) {
            FriendLocal check = realm.where(FriendLocal.class).equalTo("uid", elem.getUid()).findFirst();
            if (check != null) {
                realm.beginTransaction();
                check.deleteFromRealm();
                realm.commitTransaction();
            }
            realm.beginTransaction();
            String name = elem.getName();
            String email = elem.getEmail();
            String uid = elem.getUid();
            Boolean status = elem.getStatus();
            String photo = elem.getPhoto();
            String idShoppingCart = elem.getIdShoppingCart();

            FriendLocal friendLocalToCreate = realm.createObject(FriendLocal.class, uid);
            friendLocalToCreate.setName(name);
            friendLocalToCreate.setEmail(email);
            friendLocalToCreate.setStatus(status);
            friendLocalToCreate.setPhoto(photo);
            friendLocalToCreate.setIdShoppingCart(idShoppingCart);
            realm.commitTransaction();

            realm.beginTransaction();
            ShoppingCartLocal shoppingCartLocalAux = realm.where(ShoppingCartLocal.class).equalTo("id", shoppingCartLocal.getId()).findFirst();
            shoppingCartLocalAux.getAllowUsers().add(realm.copyFromRealm(friendLocalToCreate));
            shoppingCartLocalAux.setLastUpdate(new Date());
            realm.commitTransaction();
        }

        // We check for changes
        List<FriendLocal> toCheckFriend = friendLocals != null && friendRemotes != null ?
                shoppingCartLocal.getAllowUsers().stream()
                        .filter(x -> friendRemotes.stream().anyMatch(y -> x.getUid().equals(y.getUid()) && (!x.getName().equals(y.getName()) || !x.getPhoto().equals(y.getPhoto()) || !x.getStatus().equals(y.getStatus()))))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        for (FriendLocal elem : toCheckFriend) {
            FriendLocal pAux = realm.where(FriendLocal.class).equalTo("uid", elem.getUid()).findFirst();
            FriendLocal p = pAux!= null ? realm.copyFromRealm(pAux) : null;
            if (p != null) {
                realm.beginTransaction();
                FriendRemote friendRemoteAux = friendRemotes.stream().filter(x -> x.getUid().equals(p.getUid())).findFirst().orElse(null);
                if (friendRemoteAux != null) {
                    pAux.setName(friendRemoteAux.getName());
                    pAux.setPhoto(friendRemoteAux.getPhoto());
                    pAux.setStatus(friendRemoteAux.getStatus());
                    pAux.setEmail(friendRemoteAux.getEmail());
                }
                realm.commitTransaction();
            }
        }
        realm.close();
    }

    public Boolean deleteAllPurchaseFromLocal(String idShoppingCart) {
        Boolean res = false;
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            RealmResults<PurchaseLocal> aux1 = realm.where(PurchaseLocal.class).equalTo("idShoppingCart", idShoppingCart).findAll();
            aux1.deleteAllFromRealm();
            ShoppingCartLocal aux2 = realm.where(ShoppingCartLocal.class).equalTo("id", idShoppingCart).findFirst();
            if (aux2 != null) {
                aux2.setPurcharse(new RealmList<>());
            }
            realm.commitTransaction();
            res = true;
            if (res) {
                new GoogleUtilities().deletePurchaseFromRemote(null, true);
            }
        } catch (Exception e) {
            res = false;
            Log.e(TAG, "deleteAllPurcharseFromLocal: ", e);
        }
        return  res;
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
            if (res) {
                new GoogleUtilities().deletePurchaseFromRemote(purchaseLocal, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "deletePurchaseFromLocal: ", e);
            res = false;
        }
        return res;
    }

    public boolean savePurchase(String query, int id, int idScannedTag, Boolean check) {
        Boolean res;
        try {
            String uid = new GoogleUtilities().getCurrentUser().getUid();
            Realm realm = Realm.getDefaultInstance();
            ShoppingCartLocal shoppingCartLocal = realm.where(ShoppingCartLocal.class).equalTo("id", uid).findFirst();
            realm.beginTransaction();
            RealmList<PurchaseLocal> purchaseLocals = new RealmList<>();
            if (shoppingCartLocal == null) {
                shoppingCartLocal = realm.createObject(ShoppingCartLocal.class, uid);
                shoppingCartLocal.setOwn(true);
                shoppingCartLocal.setPurcharse(purchaseLocals);
            }
            PurchaseLocal purchaseLocal = realm.createObject(PurchaseLocal.class, id);
            purchaseLocal.setName(query);
            purchaseLocal.setIdShoppingCart(uid);
            purchaseLocal.setIdFirebase(id);
            purchaseLocal.setIdScannedTag(idScannedTag);
            purchaseLocal.setCheck(check);
            shoppingCartLocal.getPurcharse().add(realm.copyFromRealm(purchaseLocal));
            realm.commitTransaction();
            res = true;
        } catch (Exception e) {
            res = false;
            Log.e(TAG, "savePuchase: ", e);
        }
        return res;
    }

    public Boolean changeCheckStatusFromLocal(PurchaseLocal purchaseLocal, boolean checked) {
        Boolean res = false;
        try {
            String uid = new GoogleUtilities().getCurrentUser().getUid();
            Realm realm = Realm.getDefaultInstance();
            PurchaseLocal aux = realm.where(PurchaseLocal.class).equalTo("id", purchaseLocal.getId()).findFirst();
            if (aux != null) {
                realm.beginTransaction();
                aux.setCheck(checked);
                realm.commitTransaction();
                res = true;
            }
            if (res) {
                new GoogleUtilities().changeCheckStatusFromLocal(aux, checked);
            }
        } catch (Exception e) {
            res = false;
            Log.e(TAG, "savePuchase: ", e);
        }
        return res;
    }

    public List<FriendLocal> getAccessListUserLogin() {
        Realm realm = Realm.getDefaultInstance();
        ShoppingCartLocal aux = null;
        List<FriendLocal> res = new ArrayList<>();
        ShoppingCartLocal realmResult = realm.where(ShoppingCartLocal.class).equalTo("id", new GoogleUtilities().getCurrentUser().getUid()).findFirst();
        if (realmResult != null) {
            aux = realm.copyFromRealm(realmResult);
        }
        if (aux != null && aux.getAllowUsers() != null) {
            res.addAll(aux.getAllowUsers());
        }
        realm.close();
        return res;
    }

    public List<FriendLocal> getAllFriendsInvitation() {
        Realm realm = Realm.getDefaultInstance();
        ShoppingCartLocal aux = null;
        List<FriendLocal> res = new ArrayList<>();
        ShoppingCartLocal realmResult = realm.where(ShoppingCartLocal.class).equalTo("id", new GoogleUtilities().getCurrentUser().getUid()).findFirst();
        if (realmResult != null) {
            aux = realm.copyFromRealm(realmResult);
        }
        if (aux != null && aux.getFriendInvitation() != null) {
            res.addAll(aux.getFriendInvitation());
        }
        realm.close();
        return res;
    }

    public Boolean deleteInvitation(FriendLocal friendLocal) {
        Boolean res;
        try {
            Realm realm = Realm.getDefaultInstance();
            FriendLocal aux = realm.where(FriendLocal.class).equalTo("uid", friendLocal.getUid()).findFirst();
            realm.beginTransaction();
            aux.deleteFromRealm();
            realm.commitTransaction();
            realm.close();
            res = true;
            if (res) {
                new GoogleUtilities().deleteInvitation(friendLocal);
            }
        } catch (Exception e) {
            Log.e(TAG, "changeStatusFriendList: ", e);
            res = false;
        }
        return res;
    }
}
