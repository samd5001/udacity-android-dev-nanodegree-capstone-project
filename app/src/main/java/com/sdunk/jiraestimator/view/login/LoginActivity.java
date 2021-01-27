package com.sdunk.jiraestimator.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.ActivityLoginBinding;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.project.ProjectDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.project.ProjectSelectActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginViewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(loginViewModel);

        binding.token.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.loading.setVisibility(View.VISIBLE);
                loginViewModel.login();
            }
            return false;
        });

        binding.loginButton.setOnClickListener(v -> {
            binding.loading.setVisibility(View.VISIBLE);
            loginViewModel.login();
        });

        loginViewModel.getUser().observe(this, loginUser -> {
            binding.url.setError(loginUser.urlIsValid() ? null : getString(R.string.invalid_url));
            binding.email.setError(loginUser.emailIsValid() ? null : getString(R.string.invalid_email));
            binding.token.setError(loginUser.tokenIsValid() ? null : getString(R.string.invalid_token));


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
                if (loginUser.userValid()) {
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
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.welcome, Toast.LENGTH_SHORT).show());
            Intent intent = new Intent();
            intent.setClass(this, ProjectSelectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}