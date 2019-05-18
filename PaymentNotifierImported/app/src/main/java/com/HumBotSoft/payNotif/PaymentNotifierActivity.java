/**
 * Robert humble
 * "HumBot Software"
 *
 * most recent change - 3/10/12
 *
 * ***********
 * Payment Notifier
 *
 * Purpose:
 * To provide a simple method for notifying other parties of their share of a bill or charge.
 *
 * input: bill or charge total, number of recipients to share in payment
 *
 * output: SMS messages marked with share of the payment sent to selected contacts
 *
 *
 *
 *
 * ! reused some code provided by google examples: BusinesCard.java,  most of ContactAccessor*.java and TargetContact.java(ContactInfo)
 */


package com.HumBotSoft.payNotif;

import com.HumBotSoft.payNotif.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.PendingIntent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class PaymentNotifierActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    //Variables
    private List<TargetContact> listOfContacts;


    private String NoteStr;
    private double amtPerPerson;
    private int numOfSelectedUsers;

    //ViewObj holders
    private Button pick_contact_button_ViewObj;
    private Button reset_button_ViewObj;
    private Button send_button_ViewObj;
    private EditText AmtField_ViewObj;
    private EditText NoteField_ViewObj;
    private TextView TotalPPUPDField_ViewObj;
    private TextView NumSelectedUPDField_ViewObj;
    //private TableLayout ContactTBL_ViewObj;
    private ListView ContactList_ViewObj;


    private static final int PICK_CONTACT_REQUEST = 1;
    public static final String ACTION_SMS_SENT = "action sent";

    private final ContactAccessor myContactAccessor = ContactAccessor.getInstance();


     // max number of recipients
     public final int MAX_RECIP = 4;

    // initializing the amount to pay
     public double payTotal = 0;


    // number of contacts selected by user(also used as index for list arrays)
    public int contactsSelected = 0;



    //This looks like its main

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main);

        /** for managing multiple contact selections */

        Log.d("bob:", "made it here");


        /**
         *Field for entering the total amount to be paid out by customers 
         */

        initializeViewsAndVariables();

        //ViewObject is populated in mapViewObjects
        AmtField_ViewObj.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

/*
//this Section was moved to common method -    captureInputsAndUpdateAllValues()
                    try {

                        payTotal = Double.valueOf(AmtField_ViewObj.getText().toString());
                    } catch (Exception e) {

                        Log.i("String", "invalid input");
                        Log.i("String", e.getMessage());
                        Toast.makeText(PaymentNotifierActivity.this, "Please enter a value ", Toast.LENGTH_SHORT).show();
                    }

                    updateValuesAndDisplay();
*/

                    captureInputsAndUpdateAllValues();

                    return true;
                }
                return false;
            }
        });


        /*
        * Note Field Logic
        * */

        NoteField_ViewObj.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
/*
 //this Section was moved to common method -    captureInputsAndUpdateAllValues()
                    try {
                        NoteStr = (NoteField_ViewObj.getText().toString());
                    } catch (Exception e) {

                        Log.i("String", "invalid input");
                        Toast.makeText(PaymentNotifierActivity.this, "Please enter a valid Note ", Toast.LENGTH_SHORT).show();
                    }

                    updateValuesAndDisplay();
*/

                    captureInputsAndUpdateAllValues();

                    return true;
                }

                return false;
            }
        });

        
        //Pick Contact Button
        pick_contact_button_ViewObj.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                pickContact();
            }
        });


        // reset button
        reset_button_ViewObj.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                fullReset();

            }
        });



         //send button start
        send_button_ViewObj.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                sendNotifications();

                fullReset();
            }
        });


    }


    /**
     * Invoked when the contact picker activity is finished. The {@code contactUri} parameter
     * will contain a reference to the contact selected by the user. We will treat it as
     * an opaque URI and allow the SDK-specific ContactAccessor to handle the URI accordingly.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            loadContactInfo(data.getData());
        }
    }


/**
 * ============================================================================================    
 *Outside functions
 *
 */

    /**
     * Click handler for the Pick Contact button.  Invokes a contact picker activity.
     * The specific intent used to bring up that activity differs between versions
     * of the SDK, which is why we delegate the creation of the intent to ContactAccessor.
     */

    protected void pickContact() {
        startActivityForResult(myContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    }


    /**
     * Load contact information on a background thread.
     */
    private void loadContactInfo(Uri contactUri) {

        /*
         * We should always run database queries on a background thread. The database may be
         * locked by some process for a long time.  If we locked up the UI thread while waiting
         * for the query to come back, we might get an "Application Not Responding" dialog.
         */
        AsyncTask<Uri, Void, TargetContact> task = new AsyncTask<Uri, Void, TargetContact>() {

            @Override
            protected TargetContact doInBackground(Uri... uris) {
                return myContactAccessor.loadContact(getContentResolver(), uris[0]);
            }

            @Override
            protected void onPostExecute(TargetContact result) {
                bindView(result);
            }
        };

        task.execute(contactUri);
    }

    /**
     * Displays contact information: name and phone number.
     */
    protected void bindView(TargetContact contactInfo) {

        //2015 replacement code

        TargetContact tempContact = contactInfo;

        if (tempContact.getNumber() == "")
            Toast.makeText(PaymentNotifierActivity.this, "There is no phone number for this contact", Toast.LENGTH_SHORT).show();
        else
            listOfContacts.add(tempContact);


        updateValuesAndDisplay();

    }

    //2015

    private void sendNotifications() {
        SmsManager smsSender = SmsManager.getDefault();
        String messageContent;
        String sentComplete;


        for (int x = 0; x < listOfContacts.size(); x++) {
            TargetContact currentContact = listOfContacts.get(x);

            messageContent = "<Payment Notification> This is a message to inform you that you owe $: " + amtPerPerson;

            if (NoteStr != "")
                messageContent += " (for " + NoteStr + ")";

            messageContent += ".";

            try {
                smsSender.sendTextMessage(currentContact.getNumber(), null, messageContent,
                        PendingIntent.getBroadcast(PaymentNotifierActivity.this, 0, new Intent(ACTION_SMS_SENT), 0), null);
                sentComplete = "Sent SMS notification to Contact: " + currentContact.getName();

                Toast.makeText(PaymentNotifierActivity.this, sentComplete, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("smsSender", "failed to send to " + currentContact.getName());
                Toast.makeText(PaymentNotifierActivity.this, "failed to send to " + getPackageName(), Toast.LENGTH_SHORT).show();
            }
            Log.d("smsSender", messageContent);
        }

        Toast.makeText(PaymentNotifierActivity.this, "All Notifications Sent!", Toast.LENGTH_SHORT).show();
    }


    //Update values to display on the view
    private void updateValuesAndDisplay() {

        //update class variables
        //-------------------------------------------------------
        //update users
        numOfSelectedUsers = listOfContacts.size() + 1;

        //update total per person
        amtPerPerson = payTotal / (double) numOfSelectedUsers;

        //update display
        //------------------------------------------------------

        //update user count
        if (numOfSelectedUsers == 1)
            NumSelectedUPDField_ViewObj.setText("1 (Just You)");
        else
            NumSelectedUPDField_ViewObj.setText(String.valueOf(numOfSelectedUsers));


        //update total
        TotalPPUPDField_ViewObj.setText(String.valueOf(amtPerPerson));

        //Update NoteField
        NoteField_ViewObj.setText(NoteStr);

        //Use this method call to set up adapter for the current list of contacts
        updateContactList();

    }


    private void initializeViewsAndVariables() {
        //initialize these values
        payTotal = 0;
        amtPerPerson = 0;
        NoteStr = "";
        numOfSelectedUsers = 0;
        listOfContacts = new LinkedList<TargetContact>();

        //Set these objects to reference their corresponding objects in the view
        pick_contact_button_ViewObj = (Button) findViewById(R.id.pick_contact_button);
        reset_button_ViewObj = (Button) findViewById(R.id.reset_button);
        send_button_ViewObj = (Button) findViewById(R.id.send_button);
        AmtField_ViewObj = (EditText) findViewById(R.id.AmtField);
        TotalPPUPDField_ViewObj = (TextView) findViewById(R.id.TotalPPUPDField);
        NumSelectedUPDField_ViewObj = (TextView) findViewById(R.id.NumSelectedUPDField);
        // ContactTBL_ViewObj = (TableLayout) findViewById(R.id.ContactTBL);
        ContactList_ViewObj = (ListView) findViewById(R.id.ContactList);
        NoteField_ViewObj = (EditText) findViewById(R.id.NoteField);
    }


    //Reset All variables to base state
    private void fullReset() {
        payTotal = 0;

        //clear list
        listOfContacts.clear();

        //clear numSelected
        numOfSelectedUsers = 0;

        //clear amounts
        amtPerPerson = 0;

        //Clears Note
        NoteStr = "";

        //Reset amt field in view since it is not usually triggered
        AmtField_ViewObj.setText("");

        //call view update method to reflect changes in view
        updateValuesAndDisplay();


    }

    //Update the contacts DataSource
    private void updateContactList() {

        ArrayList<String> ContactInfoArray = new ArrayList<String>();

        for (int x = 0; x < listOfContacts.size(); x++) {
            ContactInfoArray.add(listOfContacts.get(x).getName() + " : " + listOfContacts.get(x).getNumber());
        }

        ArrayAdapter<String> contactListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, ContactInfoArray);

        ContactList_ViewObj.setAdapter(contactListAdapter);

    }


    private void captureInputsAndUpdateAllValues() {


        try {

            payTotal = Double.valueOf(AmtField_ViewObj.getText().toString());
        } catch (Exception e) {

            Log.i("String", "invalid input");
            Log.i("String", e.getMessage());
            Toast.makeText(PaymentNotifierActivity.this, "Please enter a value ", Toast.LENGTH_SHORT).show();
        }


        try {
            NoteStr = (NoteField_ViewObj.getText().toString());
        } catch (Exception e) {

            Log.i("String", "invalid input");
            Toast.makeText(PaymentNotifierActivity.this, "Please enter a valid Note ", Toast.LENGTH_SHORT).show();
        }

        updateValuesAndDisplay();

    }


}