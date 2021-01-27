package com.sdunk.jiraestimator.view.login;

import android.content.Intent;
import android.os.Bundle;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.databinding.ActivityLoginStartBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class LoginStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_start);

        ActivityLoginStartBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login_start);

        binding.startButton.setOnClickListener(view -> startActivity(new Intent().setClass(this, LoginActivity.class)));

    }
}