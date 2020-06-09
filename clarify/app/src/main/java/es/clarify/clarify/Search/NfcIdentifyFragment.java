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
import androidx.viewpager2.widget.ViewPager2;

import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import es.clarify.clarify.MainActivity;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GifImageView;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;


public class NfcIdentifyFragment extends Fragment {

    private NfcAdapter nfcAdapter;
    public static TextView text_company;
    public static TextView text_model;
    public static ImageView img;
    public static Button buttonAdd;
    private Button buttonCancel;
    private Button buttonScan;
    public static Dialog myDialogInfo;
    public static Dialog myDialog;
    private Utilities utilities = new Utilities();
    private GoogleUtilities googleUtilities = new GoogleUtilities();
    private Database database = new Database();
    private ViewPager2 viewPager;
    public static Button addShoppingCart;
    public static Button anotherTry;
    public static TextView price;
    public static TextView expirationDate;
    public static LinearLayout moreInfo;

    public NfcIdentifyFragment(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nfc_identify, container, false);

        GifImageView gifImageView = (GifImageView) v.findViewById(R.id.GifImageView);
        gifImageView.setGifImageResource(R.drawable.nfc_3);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

        myDialog = new Dialog(getContext());
        myDialogInfo = new Dialog(getContext());
        myDialogInfo.setContentView(R.layout.dialog_product_identify_nfc);
        myDialogInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = myDialogInfo.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.BOTTOM);
        myDialog.setContentView(R.layout.dialog_identify_product);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        moreInfo = (LinearLayout) myDialog.findViewById(R.id.more_info);
        text_company = (TextView) myDialog.findViewById(R.id.text_company);
        text_model = (TextView) myDialog.findViewById(R.id.text_model);
        price = (TextView) myDialog.findViewById(R.id.price_dialog_2);
        expirationDate = (TextView) myDialog.findViewById(R.id.date_expiration);
        img = (ImageView) myDialog.findViewById(R.id.image_product);
        buttonAdd = (Button) myDialog.findViewById(R.id.buttonAdd);
        buttonCancel = (Button) myDialogInfo.findViewById(R.id.button_cancel_identify);
        buttonScan = (Button) v.findViewById(R.id.button_scan);
        addShoppingCart = (Button) myDialog.findViewById(R.id.dialog_add_shopping_cart);
        anotherTry = (Button) myDialog.findViewById(R.id.another_try);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialogInfo.dismiss();
            }
        });

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nfcAdapter == null) {
                    Toast.makeText(getContext(), "Necesitas NFC para usarlo", Toast.LENGTH_LONG).show();
                } else if (viewPager.getCurrentItem() == 2){
                    if (!nfcAdapter.isEnabled()) {
                        MainActivity.dialogNfc.show();
                        // Device has a NFC module but it is not enable. Sending the user to Android's configuration panel.
                        MainActivity.showSettings.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MainActivity.dialogNfc.dismiss();
                                showWirelessSettings();
                            }
                        });
                        MainActivity.cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MainActivity.alert = false;
                                MainActivity.dialogNfc.dismiss();
                            }
                        });
                    } else {
                        myDialogInfo.show();
                    }
                }
            }
        });


        return v;
    }

    private void showWirelessSettings() {
        Toast.makeText(getContext(), "¡Activa el NFC aquí!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NFC instances


    }

    public void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            if ((myDialogInfo != null && myDialogInfo.isShowing()) || (myDialog != null && myDialog.isShowing())) {
                NdefMessage[] msgs = utilities.getTagInfo(intent);
                utilities.printInfo(getActivity(), msgs);
            }
        }
    }

}
