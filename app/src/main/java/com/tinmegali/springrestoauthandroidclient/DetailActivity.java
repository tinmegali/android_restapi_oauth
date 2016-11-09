package com.tinmegali.springrestoauthandroidclient;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.User;

import java.io.IOException;

import javax.inject.Inject;

public class DetailActivity extends AppCompatActivity {

    public static Intent makeIntent(Context context, String userId ) {
        Intent intent = new Intent( context, DetailActivity.class );

        Bundle extras = new Bundle();
        extras.putString( KEY_USER_ID, userId );
        intent.putExtras(extras);
        return intent;
    }

    private static final String KEY_USER_ID = "user_id";
    private String userId;

    private TextView id, name, salary, age;

    @Inject
    ApiController apiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ( (MyApplication) getApplication()).getActivitiesComponent().inject(this);

        setContentView(R.layout.activity_detail);
        userId = getIntent().getExtras().getString(KEY_USER_ID);

        id = (TextView) findViewById(R.id.user_id);
        name = (TextView) findViewById(R.id.name);
        salary = (TextView) findViewById(R.id.salary);
        age = (TextView) findViewById(R.id.age);


        // Load data
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                try {
                    User user = apiController.getUser( userId );
                    return user;
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
                if ( user != null ) {
                    id.setText(user.getId().toString());
                    name.setText(user.getName());
                    salary.setText(user.getSalary().toString());
                    age.setText(user.getAge().toString());
                }
            }
        }.execute();
    }
}
