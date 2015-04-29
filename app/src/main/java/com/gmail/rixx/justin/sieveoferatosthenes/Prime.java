package com.gmail.rixx.justin.sieveoferatosthenes;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


public class Prime extends ActionBarActivity {

    private static final int SIEVE_SIZE = 1000000;
    private static String LOG_TAG = "Sieve of Eratosthenes";

    private boolean sieveDone = false;

    private ProgressBar bar;

    boolean[] sieve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime);
        if (savedInstanceState == null) {
            generateSieve();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if the number that was input is prime
     * @param view
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

            if (sieve[toCheck]) {
                Toast.makeText(this, "Prime", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not prime", Toast.LENGTH_SHORT).show();
            }
        }
    }


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
}
