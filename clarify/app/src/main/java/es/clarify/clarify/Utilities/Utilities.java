package es.clarify.clarify.Utilities;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import es.clarify.clarify.NFC.NdefMessageParser;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.NFC.ParsedNdefRecord;
import es.clarify.clarify.Objects.ScannedTag;
import io.realm.Realm;

public class Utilities {

    private NfcUtility nfcUtility = new NfcUtility();

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

    public void printInfo(NdefMessage[] msgs, final ImageView img, List<TextView> textViews) {

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
            TextView text = params.get(0);
            TextView text_company = params.get(1);
            TextView text_model = params.get(2);
            TextView text_expiration_date = params.get(3);
            ImageView imgToChange = img;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot scannedTagFirebase : dataSnapshot.getChildren()) {
                        ScannedTag scannedTag = scannedTagFirebase.getValue(ScannedTag.class);
                        this.text.setText("¡Genial! Etiqueta encontrada");
                        this.text_company.setText(scannedTag.getBrand());
                        this.text_model.setText(scannedTag.getModel());
                        this.text_expiration_date.setText(scannedTag.getExpiration_date());
                        Picasso.get().load(scannedTag.getImage()).into(imgToChange);
                        Database database = new Database(Realm.getDefaultInstance());
                        database.addScannedTagLocal(scannedTag);
                    }
                } else {
                    text.setText("Etiqueta no encontrada");
                }
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
}
