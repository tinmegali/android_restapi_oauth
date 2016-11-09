package com.tinmegali.springrestoauthandroidclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.OAuthManager;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.User;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import static android.view.View.GONE;

public class StarterActivity extends AppCompatActivity {

    private static final String TAG = StarterActivity.class.getSimpleName();

    @Inject
    ApiController apiController;

    @Inject
    SharedPreferences preferences;

    @Inject
    OAuthManager oAuthManager;

    private ProgressBar progressBar;

    private RecyclerView list;
    private UserAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private WorkerThread workerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((MyApplication) getApplication()).getActivitiesComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        workerThread = new WorkerThread(new Handler());
        workerThread.start();
        workerThread.prepareHandler();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        list = (RecyclerView) findViewById(R.id.list);

        layoutManager = new LinearLayoutManager(this);
        adapter = new UserAdapter();

        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( StarterActivity.this, NewUserActivity.class ));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load data
        loadUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workerThread.interrupt();
    }

    private void loadUsers() {
        Log.d(TAG, "loadUsers");

        // start loading data
        workerThread.loadUsers();

        // show PreLoader
        progressBar.setVisibility(View.VISIBLE);
    }

    private void deleteUser(String id) {
        Log.d(TAG, "deleteUser");
        progressBar.setVisibility(View.VISIBLE);
        workerThread.deleteUser(id);
    }

    private void configListAdapter(List<User> users) {
        Log.d(TAG, "configListAdapter");
        // configure list adapter
        adapter.setData(users);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(GONE);
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private class WorkerThread extends HandlerThread {

        private Handler handler;
        private Handler uiHandler;

        public WorkerThread(Handler uiHandler) {
            super("StartActivity_WThread");
            handler = new Handler();
            this.uiHandler = uiHandler;
        }

        public void prepareHandler() {
            handler = new Handler(getLooper());
        }

        private Runnable runLoadData = new Runnable() {
            @Override
            public void run() {
                try {
                    final List<User> users = apiController.getUsers();
                    uiHandler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    configListAdapter( users );
                                }
                            }
                    );

                } catch (RestUnauthorizedException e) {
                    e.printStackTrace();
                    handleExceptions(e);
                } catch (RestHttpException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        public void loadUsers() {
            handler.post( runLoadData );
        }

        public void deleteUser( final String id ) {
            handler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                apiController.deleteUser(id);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                loadUsers();
                                            }
                                        }
                                );
                            } catch (RestUnauthorizedException e) {
                                e.printStackTrace();
                                handleExceptions(e);
                            } catch (RestHttpException e) {
                                handleExceptions(e);
                                e.printStackTrace();
                            } catch (IOException e) {
                                handleExceptions(e);
                                e.printStackTrace();
                            }
                        }
                    }
            );
        }

        private void handleExceptions(Exception e) {
            Log.d(TAG, "handleExceptions");
            if (e instanceof RestHttpException) {
                final RestHttpException ex = (RestHttpException) e;
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                showToast(ex.getErrorHttp().getError()
                                        + ". Status: " + ex.getErrorHttp().getStatus());
                            }
                        }
                );

            } else if (e instanceof RestUnauthorizedException) {
                final RestUnauthorizedException ex = (RestUnauthorizedException) e;
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                showToast(ex.getErrorUnauthorized().getErrorDescription()
                                        + ". " + ex.getErrorUnauthorized().getError());
                            }
                        }
                );

            } else if (e instanceof IOException) {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                showToast("Some IO error occurred. ");
                            }
                        }
                );

            }
        }


    }

    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        public class UserViewHolder extends RecyclerView.ViewHolder {

            public TextView id, name;

            public UserViewHolder(View view) {
                super(view);
                this.id = (TextView) view.findViewById(R.id.user_id);
                this.name = (TextView) view.findViewById(R.id.name);

                LinearLayout container = (LinearLayout) view.findViewById(R.id.container);

                // nav to user detail on click
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = DetailActivity.makeIntent(StarterActivity.this, id.getText().toString());
                        startActivity(intent);
                    }
                });

                // open alert to delete on long click
                container.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(StarterActivity.this)
                                        .setMessage("Delete user " + name.getText().toString() + "?")
                                        // delete User
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.i(TAG, "deleteUser["+id.getText().toString()+"]");
                                                deleteUser( id.getText().toString() );

                                            }
                                        })
                                        .setNegativeButton("CANCEL", null);
                        builder.create().show();
                        return true;
                    }
                });
            }
        }

        private List<User> data;

        public void setData(List<User> data) {
            this.data = data;
        }

        @Override
        public int getItemCount() {
            if (data != null)
                return data.size();
            else return 0;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.vh_user_list, parent, false);
            UserViewHolder vh = new UserViewHolder(view);

            return vh;
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            if (data != null) {
                holder.id.setText(Integer.toString(data.get(position).getId()));
                holder.name.setText(data.get(position).getName());
            }
        }
    }
}
