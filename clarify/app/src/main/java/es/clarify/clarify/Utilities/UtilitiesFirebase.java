package es.clarify.clarify.Utilities;

import android.nfc.NdefMessage;
import android.util.Log;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

import es.clarify.clarify.NFC.NdefMessageParser;
import es.clarify.clarify.NFC.ParsedNdefRecord;
import es.clarify.clarify.Objects.ScannedTag;

public class UtilitiesFirebase {

    private NdefMessage[] msgs;
    private TextView text;
    private TextView text_company;
    private TextView text_model;
    private TextView text_expiration_date;

    public UtilitiesFirebase(NdefMessage[] msgs, List<TextView> textViews) {
        this.msgs = msgs;
        this.text = textViews.get(0);
        this.text_company = textViews.get(1);
        this.text_model = textViews.get(2);
        this.text_expiration_date = textViews.get(3);
    }

    public void printInfo() {
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
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot scannedTagFirebase : dataSnapshot.getChildren()) {
                        Log.d("FIREBASE_ASYN_1", scannedTagFirebase.toString());
                        ScannedTag scannedTag = scannedTagFirebase.getValue(ScannedTag.class);
                        Log.d("FIREBASE_ASYN_2", scannedTag.toString());
                        text.setText("");
                        text_company.setText(scannedTag.getBrand());
                        text_model.setText(scannedTag.getModel());
                        text_expiration_date.setText(scannedTag.getExpiration_date());
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

}
