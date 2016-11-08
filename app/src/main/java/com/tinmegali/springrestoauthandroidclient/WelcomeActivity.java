package com.tinmegali.springrestoauthandroidclient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class WelcomeActivity extends AppCompatActivity {

    private final String TAG = WelcomeActivity.class.getSimpleName();

    public static Intent makeIntent( Context context, Account account ) {
        Intent intent = new Intent(context, WelcomeActivity.class);

        Bundle extras = new Bundle();
        extras.putParcelable( KEY_ACCOUNT, account );

        intent.putExtras(extras);
        return intent;
    }

    private static final String KEY_ACCOUNT = "user_name";

    private Account account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        account = getIntent().getExtras().getParcelable(KEY_ACCOUNT);
        Log.d(TAG, "hello " + account.name);

        setContentView(R.layout.activity_welcome);
    }
}
