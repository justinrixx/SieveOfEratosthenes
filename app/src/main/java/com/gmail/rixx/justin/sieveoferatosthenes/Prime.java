package com.gmail.rixx.justin.sieveoferatosthenes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


public class Prime extends ActionBarActivity {

    private static int SIEVE_SIZE;
    private static String LOG_TAG = "Sieve of Eratosthenes";
    private static String SIEVE_FNAME = "sieve.txt";

    private boolean sieveDone = false;

    private ProgressBar bar;

    boolean[] sieve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime);
        if (savedInstanceState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SIEVE_SIZE = Integer.parseInt(prefs.getString("sieve_size", "1000000"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get the shared preferences to generate the correct sieve size
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int size = Integer.parseInt(prefs.getString("sieve_size", "1000000"));

        // check if the file exists
        File file = new File(getApplicationContext().getFilesDir(), SIEVE_FNAME);

        if (file.exists() && SIEVE_SIZE == size) {
            // read file
            new ReadSieveTask().execute();

        } else {
            // set the size
            SIEVE_SIZE = size;

            // regenerate it
            generateSieve();

            // write the file
            new WriteSieveTask().execute();
        }
    }

    /**
     * Generate the look up table for primality checking
     */
    private void generateSieve() {
        sieve = new boolean[SIEVE_SIZE];

        bar = (ProgressBar) findViewById(R.id.progress_bar);

        new GenerateSieveTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_prime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if the number that was input is prime
     * @param view the button pressed
     */
    public void checkPrime(View view) {
        String s = ((EditText) findViewById(R.id.edit_text_number)).getText().toString();

        if (!sieveDone) {
            Toast.makeText(this, "Sieve is not ready", Toast.LENGTH_LONG).show();
        } else if (s.equals("")) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_LONG).show();
        }
        else {
            int toCheck = Integer.parseInt(s);

            // make sure it's a valid number
            if (toCheck >= SIEVE_SIZE) {
                Toast.makeText(this, "Max number is " + (SIEVE_SIZE - 1), Toast.LENGTH_LONG).show();
                return;
            }

            if (sieve[toCheck]) {
                Toast.makeText(this, "Prime", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not prime", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Regenerate the sieve as per request by the user
     * @param view the button pressed
     */
    public void regenerate(View view) {

        // confirm the action with a dialog
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Confirm this action");
        alert.setMessage("Are you sure you want to re-generate the sieve? " +
                "This can be time-consuming and use a lot of computing resources");

        // set up the positive button
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // regenerate it
                generateSieve();

                // write the file
                new WriteSieveTask().execute();
            }
        });

        // set up the negative button
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled. Do nothing
            }
        });

        AlertDialog dialog = alert.create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }


    /**
     * Generate the sieve. This sieve is used as a look up table to determine
     * if a number is prime
     */
    public class GenerateSieveTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            // set everything to true
            for (int i = 0; i < SIEVE_SIZE; i++) {
                sieve[i] = true;
            }

            // initial conditions
            sieve[0] = false;
            sieve[1] = false;


            for (int i = 2; i < SIEVE_SIZE; i++) {

                // only loop all the way up if the number is prime
                if (sieve[i]) {
                    for (int iMult = i + i; iMult < SIEVE_SIZE; iMult += i) {
                        sieve[iMult] = false;
                    }
                }

                Double progress = 100.0 / (SIEVE_SIZE / i);
                publishProgress(progress.intValue());
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            bar.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            bar.setVisibility(View.GONE);

            sieveDone = true;

            Toast.makeText(getApplicationContext(), "Sieve generated", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            bar.setProgress(values[0]);
        }

    }

    /**
     * Write the sieve to a file (filename given)
     */
    private class WriteSieveTask extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            File file = new File(getApplicationContext().getFilesDir(), SIEVE_FNAME);

            try {
                PrintWriter writer = new PrintWriter(file);

                // write out the entire sieve
                for (int i = 0; i < SIEVE_SIZE; i++) {
                    if (sieve[i]) {
                        writer.println("true");
                    } else {
                        writer.println("false");
                    }
                }

                writer.close();

            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "Error in WriteSieveTask: ", e);
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Read the sieve from a file
     */
    private class ReadSieveTask extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            File file = new File(getApplicationContext().getFilesDir(), SIEVE_FNAME);

            try {

                // allocate and make sure it's the right size
                sieve = new boolean[SIEVE_SIZE];

                BufferedReader reader = new BufferedReader(new FileReader(file));

                String temp;
                int i = 0;
                while (null != (temp = reader.readLine())) {
                    sieve[i] = Boolean.parseBoolean(temp.trim());
                    i++;
                }

                reader.close();

            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "Sieve file does not exist: ", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Unexpected IO Exception: ", e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            sieveDone = true;
        }
    }
}
