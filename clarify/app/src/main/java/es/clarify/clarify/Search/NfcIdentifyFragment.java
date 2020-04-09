package es.clarify.clarify.Search;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    private TextView text_company;
    private TextView text_model;
    private ImageView img;
    private Button buttonAdd;
    private Button buttonCancel;
    private Button buttonScan;
    private Dialog myDialog_info;
    private Dialog mydialog;
    private Utilities utilities = new Utilities();
    private GoogleUtilities googleUtilities = new GoogleUtilities();
    Database database = new Database(Realm.getDefaultInstance());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nfc_identify, container, false);
        mydialog = new Dialog(getContext());
        myDialog_info = new Dialog(getContext());
        myDialog_info.setContentView(R.layout.dialog_product_identify_nfc);
        myDialog_info.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = myDialog_info.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.BOTTOM);
        if (NfcIdentifyFragment.this.isVisible()) {
            myDialog_info.show();
        }
        mydialog.setContentView(R.layout.dialog_identify_product);
        mydialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        text_company = (TextView) mydialog.findViewById(R.id.text_company);
        text_model = (TextView) mydialog.findViewById(R.id.text_model);
        img = (ImageView) mydialog.findViewById(R.id.image_product);
        buttonAdd = (Button) mydialog.findViewById(R.id.buttonAdd);
        buttonCancel = (Button) myDialog_info.findViewById(R.id.button_cancel_identify);
        buttonScan = (Button) v.findViewById(R.id.button_scan);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog_info.dismiss();
            }
        });

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog_info.show();
            }
        });

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            myDialog_info.show();
        }
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
            utilities.printInfo(getActivity(), msgs, img, Arrays.asList(text_company, text_model), mydialog, myDialog_info, buttonAdd);
        }
    }

}
