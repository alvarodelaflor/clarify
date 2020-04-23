package es.clarify.clarify.Objects;

import android.util.Log;

import java.util.Date;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StoreLocal extends RealmObject {

    @PrimaryKey
    private String name;
    private Date lastUpdate;
    private RealmList<ScannedTagLocal> scannedTagLocals;

    public StoreLocal() {
    }

    public StoreLocal(String name, Date lastUpdate, RealmList<ScannedTagLocal> scannedTagLocals) {
        this.name = name;
        this.lastUpdate = lastUpdate;
        this.scannedTagLocals = scannedTagLocals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public RealmList<ScannedTagLocal> getScannedTagLocals() {
        return scannedTagLocals;
    }

    public void setScannedTagLocals(RealmList<ScannedTagLocal> scannedTagLocals) {
        this.scannedTagLocals = scannedTagLocals;
    }

    public void addNewScannedTagsLocal(RealmList<ScannedTagLocal> scannedTagsLocal) {
        for (ScannedTagLocal elem: scannedTagsLocal) {
            addNewScannedTagLocal(elem);
        }
    }

    public void addNewScannedTagLocal(final ScannedTagLocal scannedTagLocal) {
        RealmList<ScannedTagLocal> res = getScannedTagLocals();
        if (res == null) {
            setScannedTagLocals(new RealmList<>());
        } else {
            Boolean check = res.stream().filter(x -> x.getIdFirebase() == scannedTagLocal.getIdFirebase()).count() >= 0 ? false : res.add(scannedTagLocal);
            if (!check) {
                Log.i("StoreLocal", "addNewScannedTagLocal: already in database");
            }
        }
    }
}
