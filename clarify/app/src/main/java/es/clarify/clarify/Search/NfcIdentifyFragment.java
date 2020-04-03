package es.clarify.clarify.Search;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.Objects.ScannedTagRemote;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;
import io.realm.Realm;


public class NfcIdentifyFragment extends Fragment {

    private NfcAdapter nfcAdapter;
    private TextView text;
    private TextView text_company;
    private TextView text_model;
    private TextView text_expiration_date;
    private ImageView img;
    private Button buttonAdd;
    private Utilities utilities = new Utilities();
    private GoogleUtilities googleUtilities = new GoogleUtilities();
    Database database = new Database(Realm.getDefaultInstance());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nfc_identify, container, false);
        text = (TextView) v.findViewById(R.id.product);
        text_company = (TextView) v.findViewById(R.id.text_company2);
        text_model = (TextView) v.findViewById(R.id.text_model2);
        text_expiration_date = (TextView) v.findViewById(R.id.text_expiration_date2);
        img = (ImageView) v.findViewById(R.id.image_product);
        buttonAdd = (Button) v.findViewById(R.id.buttonAdd);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScannedTagLocal scannedTagLocal = database.getLastScannedTag();
                if (scannedTagLocal!=null) {
                    ScannedTagRemote scannedTagRemote= new ScannedTagRemote(scannedTagLocal);
                    Boolean saveResult = googleUtilities.addToStore("Nevera", scannedTagRemote, getActivity());
                    if (!saveResult) {
                        Toast.makeText(getActivity(), "¡No se pudo guardar!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

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

            NdefMessage[] msgs = utilities.getTagInfo(intent);
            utilities.printInfo(msgs, img, Arrays.asList(text, text_company, text_model, text_expiration_date));
        }
    }

}
