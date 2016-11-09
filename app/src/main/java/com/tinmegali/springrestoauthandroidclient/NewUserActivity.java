package com.tinmegali.springrestoauthandroidclient;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.User;

import java.io.IOException;

import javax.inject.Inject;

public class NewUserActivity extends AppCompatActivity {

    private final String TAG = NewUserActivity.class.getSimpleName();

    private EditText name, salary, age;
    private Button btnAdd;

    private static final String KEY_ACCOUNT = "user_name";

    @Inject
    ApiController apiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ( (MyApplication) getApplication() ).getActivitiesComponent().inject(this);

        setContentView(R.layout.activity_new_user);

        name = (EditText) findViewById(R.id.name);
        salary = (EditText) findViewById(R.id.salary);
        age = (EditText) findViewById(R.id.age);
        btnAdd = (Button) findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();

            }
        });
    }

    private boolean checkFields() {
        if (name.getText().toString().isEmpty() ||
                salary.getText().toString().isEmpty() ||
                age.getText().toString().isEmpty()) {

            Toast.makeText(getApplicationContext(),
                    "Files cannot be blanck", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    private void addUser() {
        Log.d(TAG, "addUser");
        if (!checkFields())
            return;
        final User user = new User();
        user.setName(name.getText().toString());
        user.setAge(Integer.valueOf(age.getText().toString()));
        user.setSalary(Integer.valueOf(salary.getText().toString()));

        Log.d(TAG, "user");
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {

                try {
                    return apiController.addUser(user);
                } catch (RestUnauthorizedException e) {
                    e.printStackTrace();
                } catch (RestHttpException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(User user) {
                if (user != null)
                    Toast.makeText(
                            getApplicationContext(),
                            "User " + user.getName() + " added.",
                            Toast.LENGTH_SHORT
                    ).show();
                cleanFields();
            }
        }.execute();
    }

    private void cleanFields() {
        Log.d(TAG, "cleanFields");
        name.setText("");
        salary.setText("");
        age.setText("");
    }
}
