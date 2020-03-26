package es.clarify.clarify.Search;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Utilities;


public class NfcIdentifyFragment extends Fragment {

    private NfcAdapter nfcAdapter;
    private NfcUtility nfcUtility = new NfcUtility();

    private TextView text;
    private TextView text_company;
    private TextView text_model;
    private TextView text_expiration_date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nfc_identify, container, false);
        text = (TextView) v.findViewById(R.id.product);
        text_company = (TextView) v.findViewById(R.id.text_company2);
        text_model = (TextView) v.findViewById(R.id.text_model2);
        text_expiration_date = (TextView) v.findViewById(R.id.text_expiration_date2);

        return v;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // NFC instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

        if (nfcAdapter == null) {
            Toast.makeText(getContext(), "Dispositivo incompatible", Toast.LENGTH_LONG).show();
            return;
        }

    }

    public void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Utilities utilities = new Utilities();
            NdefMessage[] msgs = utilities.getTagInfo(intent);
            utilities.printInfo(msgs, Arrays.asList(text, text_company, text_model, text_expiration_date));
        }
    }

}
