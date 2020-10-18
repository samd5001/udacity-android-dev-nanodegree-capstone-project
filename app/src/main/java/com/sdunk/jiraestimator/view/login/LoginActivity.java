package com.sdunk.jiraestimator.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.sdunk.jiraestimator.view.project.ProjectSelectActivity;
import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.ProjectDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.databinding.ActivityLoginBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String URL_ERROR = "Not a valid Jira Cloud URL";
    private static final String EMAIL_ERROR = "Not a valid email";
    private static final String TOKEN_ERROR = "Token / password must be entered";

    private LoginViewModel loginViewModel;

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginViewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(loginViewModel);

        binding.token.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    binding.loading.setVisibility(View.VISIBLE);
                    loginViewModel.login();
                }
                return false;
            }
        });

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loading.setVisibility(View.VISIBLE);
                loginViewModel.login();
            }
        });

        loginViewModel.getUser().observe(this, new Observer<LoginUser>() {
            @Override
            public void onChanged(LoginUser loginUser) {
                binding.url.setError(loginUser.urlIsValid() ? null : URL_ERROR);
                binding.email.setError(loginUser.emailIsValid() ? null : EMAIL_ERROR);
                binding.token.setError(loginUser.tokenIsValid() ? null : TOKEN_ERROR);


                if (!loginUser.urlIsValid()) {
                    binding.url.requestFocus();
                } else if (!loginUser.emailIsValid()) {
                    binding.email.requestFocus();
                } else if (!loginUser.tokenIsValid()) {
                    binding.token.requestFocus();
                }

                if (loginUser.getProjectList() != null) {
                    DBExecutor.getInstance().diskIO().execute(LoginActivity.this::insertUserAndProject);
                } else {
                    binding.loading.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, R.string.login_api_error_message, Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void insertUserAndProject() {
        LoginUser loginUser = loginViewModel.getUser().getValue();
        if (loginUser != null) {
            User user = new User(loginUser.getUrl(), loginUser.getEmail(), loginUser.getToken());
            UserDatabase.getInstance(getApplicationContext()).userDao().loginUser(user);
            ProjectDatabase.getInstance(getApplicationContext()).projectDao().insertProjects(loginUser.getProjectList());

            startActivity(new Intent().setClass(this, ProjectSelectActivity.class));
            finish();
        }
    }
}