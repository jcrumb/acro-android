package com.crumbsauce.acro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.crumbsauce.acro.backend.ApiCallStatusReceiver;
import com.crumbsauce.acro.backend.Backend;
import com.crumbsauce.acro.backend.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageContactsScreen extends AppCompatActivity
        implements OnClickListener, ApiCallStatusReceiver<Contact[]>, AdapterView.OnItemLongClickListener {
    private static final String LOG_TAG = "MANAGE_CONTACTS";
    private Backend backend;
    private Contact[] contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_contacts_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        backend = new Backend(getApplicationContext(), Util.getSessionUserEmail(), Util.getSessionUserToken());
        backend.getContactsAsync(this);

        ListView lv = (ListView) findViewById(R.id.contactsListView);
        lv.setOnItemLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                createContact();
                break;
        }
    }

    private void createContact() {
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);

        prompt.setTitle("Add contact");
        prompt.setIcon(R.drawable.bike_icon);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(this);
        final EditText phoneInput = new EditText(this);

        nameInput.setHint("Name");
        nameInput.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        phoneInput.setHint("Phone Number");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);

        layout.addView(nameInput);
        layout.addView(phoneInput);


        prompt.setView(layout);
        prompt.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Contact c = new Contact();
                c.firstName = nameInput.getText().toString().split(" ")[0];
                c.lastName = nameInput.getText().toString().split(" ")[1];
                c.phoneNumber = phoneInput.getText().toString();

                addContactToList(c);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        prompt.create().show();
    }

    private void addContactToList(Contact newContact) {
        final ManageContactsScreen s = this;

        backend.createContactAsync(newContact, new ApiCallStatusReceiver<Void>() {
            @Override
            public void onSuccess(Void result) {
                backend.getContactsAsync(s);
            }

            @Override
            public void onError(String error) {
                Log.e(LOG_TAG, error);
            }
        });
    }

    // ApiCallStatusReceiver
    @Override
    public void onSuccess(Contact[] result) {
        Log.d(LOG_TAG, String.format("Number of contacts: %d", result.length));

        contacts = result;

        String[] from = { "name", "phone" };
        int[] to = { android.R.id.text1, android.R.id.text2 };

        SimpleAdapter adapter = new SimpleAdapter(this, formatContactArray(result),
                android.R.layout.simple_list_item_2, from, to);
        ListView lv = (ListView) findViewById(R.id.contactsListView);
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Map<String, String>> formatContactArray(final Contact[] contacts) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (Contact contact : contacts) {
            list.add(contactToHash(contact));
        }

        return list;
    }

    private HashMap<String, String> contactToHash(Contact c) {
        HashMap<String, String> item = new HashMap<>();

        item.put("name", String.format("%s %s", c.firstName, c.lastName));
        item.put("phone", c.phoneNumber);

        return item;
    }

    @Override
    public void onError(String error) {
        Log.e(LOG_TAG, error);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (contacts == null) { return true; }

        Log.d(LOG_TAG, String.format("Position: %d", position));
        Log.d(LOG_TAG, String.format("Phone: %s", contacts[position].phoneNumber));

        final ManageContactsScreen s = this;
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);

        prompt.setTitle("Confirm");
        prompt.setMessage("Are you sure you wish to remove this contact?");
        prompt.setIcon(R.drawable.bike_icon);
        prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backend.deleteContactAsync(contacts[position].phoneNumber, new ApiCallStatusReceiver<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Util.makeToast(s, "Contact removed");
                        backend.getContactsAsync(s);
                    }

                    @Override
                    public void onError(String error) {
                        Util.makeToast(s, "Error removing contact");
                    }
                });
            }
        });
        prompt.setNegativeButton("No", null);

        prompt.create().show();

        return true;
    }
}
