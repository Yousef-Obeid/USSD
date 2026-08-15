package com.example.ussd;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText numEt, priceEt;
    Button callPalpayBt, callJawwalBt;
    ImageButton pickContactBtn;
    RadioGroup typeRg;
    RadioButton freindRb, traderRb;

    ActivityResultLauncher<Intent> contactPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        numEt = findViewById(R.id.numEt);
        priceEt = findViewById(R.id.priceEt);
        callPalpayBt = findViewById(R.id.callPalpayBt);
        callJawwalBt = findViewById(R.id.callJawwalBt);
        pickContactBtn = findViewById(R.id.pickContactBtn);
        typeRg = findViewById(R.id.typeRg);
        freindRb = findViewById(R.id.freindPayRb);
        traderRb = findViewById(R.id.traderPayRb);

        freindRb.setChecked(true);


        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                            if (cursor != null && cursor.moveToFirst()) {
                                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                String phoneNumber = cursor.getString(numberIndex);

                                phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

                                if (phoneNumber.startsWith("972")) {
                                    phoneNumber = "0" + phoneNumber.substring(3);
                                } else if (phoneNumber.length() > 10) {
                                    phoneNumber = "0" + phoneNumber.substring(phoneNumber.length() - 9);
                                }

                                numEt.setText(phoneNumber);
                                numEt.setError(null); // إلغاء إظهار الخطأ فور إدخال رقم صحيح
                            }
                        }
                    }
                });

        pickContactBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS}, 100);
            } else {
                openContactPicker();
            }
        });

        callPalpayBt.setOnClickListener(v -> {

            if (validateInputs()) {
                String number = numEt.getText().toString().trim();
                String price = priceEt.getText().toString().trim();

                Intent intent = new Intent(Intent.ACTION_DIAL);
                String typeCode = traderRb.isChecked() ? "*2" : "*1*1";
                String allCode = "*370" + typeCode + "*" + number + "*" + price + "#";
                intent.setData(Uri.fromParts("tel", allCode, null));
                startActivity(intent);
            }
        });

        callJawwalBt.setOnClickListener(v -> {
            if (validateInputs()) {
                String number = numEt.getText().toString().trim();
                String price = priceEt.getText().toString().trim();

                String typeCode = traderRb.isChecked() ? "2" : "1";
                String allCode = "*268*" + typeCode + "*" + number + "*" + price + "#";

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.fromParts("tel", allCode, null));
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs() {
        String number = numEt.getText().toString().trim();
        String price = priceEt.getText().toString().trim();
        boolean isValid = true;

        if (number.isEmpty()) {
            numEt.setError("يرجى إدخال رقم الهاتف");
            isValid = false;
        } else if (number.length() != 10) {
            numEt.setError("يجب أن يتكون رقم الهاتف من 10 أرقام بالضبط");
            isValid = false;
        }

        if (price.isEmpty()) {
            priceEt.setError("يرجى إدخال المبلغ");
            isValid = false;
        }

        return isValid;
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        }
    }
}