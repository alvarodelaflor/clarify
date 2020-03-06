package es.clarify.clarify.Search;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import es.clarify.clarify.NFC.NdefMessageParser;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.NFC.ParsedNdefRecord;
import es.clarify.clarify.R;

public class IdentifyFragment extends Fragment {

    public IdentifyFragment() {
        // Required empty public constructor
    }

    private TextView text;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private IdentifyFragment identifyFragment;
    private NfcUtility nfcUtility = new NfcUtility();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

        if (nfcAdapter == null) {
            Toast.makeText(getContext(), "Dispositivo incompatible", Toast.LENGTH_LONG).show();
        }

        pendingIntent = PendingIntent.getActivity(getContext(), 0,
                new Intent(getContext(), this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        return inflater.inflate(R.layout.fragment_identify, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                // Device has a NFC module but it is not enable. Sending the user to Android's configuration panel.
                showWirelessSettings();

            // Give the priority to our app
            nfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(getContext(), "¡Activa el NFC aquí!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
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
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }

            displayMsgs(msgs);
        }
    }

    private void displayMsgs(NdefMessage[] msgs) {
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

        text = (TextView) frameLayout.findViewById(R.id.tag_read);
        text.setText(builder.toString());
    }

}
