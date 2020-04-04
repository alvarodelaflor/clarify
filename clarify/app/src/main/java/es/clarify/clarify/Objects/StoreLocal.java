package es.clarify.clarify.Objects;

import java.util.Date;
import java.util.List;

public class StoreLocal {

    private String name;
    private Date lastUpdate;
    private List<ScannedTagLocal> scannedTagLocals;

    public StoreLocal(String name, Date lastUpdate, List<ScannedTagLocal> scannedTagLocals) {
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

    public List<ScannedTagLocal> getScannedTagLocals() {
        return scannedTagLocals;
    }

    public void setScannedTagLocals(List<ScannedTagLocal> scannedTagLocals) {
        this.scannedTagLocals = scannedTagLocals;
    }
}
