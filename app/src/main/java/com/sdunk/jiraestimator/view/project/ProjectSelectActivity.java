package com.sdunk.jiraestimator.view.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.databinding.ActivityProjectSelectBinding;
import com.sdunk.jiraestimator.databinding.ProjectListItemBinding;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.DBUtils;
import com.sdunk.jiraestimator.db.user.UserDAO;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.Project;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;
import com.sdunk.jiraestimator.view.login.LoginActivity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ProjectSelectActivity extends AppCompatActivity {

    private final ArrayList<Project> projects = new ArrayList<>();
    private ActivityProjectSelectBinding binding;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.project_list_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            DBExecutor.getInstance().diskIO().execute(() -> DBUtils.clearLoggedInData(this));
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_project_select);
        binding.projectList.setLayoutManager(new LinearLayoutManager(this));
        binding.projectList.setAdapter(new GenericRVAdapter<Project, ProjectListItemBinding>(projects) {
            @Override
            public int getLayoutResId() {
                return R.layout.project_list_item;
            }

            @Override
            public void onBindData(Project project, ProjectListItemBinding binding) {
                binding.setProject(project);
            }

            @Override
            public void onItemClick(Project project) {
                DBExecutor.getInstance().diskIO().execute(() -> ProjectSelectActivity.this.selectProject(project));
            }
        });
        setupDBObserver();
    }

    private void setupDBObserver() {
        new ViewModelProvider(this).get(ProjectViewModel.class).getProjects().observe(this, dbProjects -> {
            projects.clear();
            projects.addAll(dbProjects);
            if (binding.projectList.getAdapter() != null) {
                binding.projectList.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private void selectProject(Project project) {
        UserDAO userDAO = UserDatabase.getInstance(getApplicationContext()).userDao();
        User loggedInUser = userDAO.getLoggedInUser();

        loggedInUser.setProjectKey(project.getKey());
        userDAO.updateUser(loggedInUser);
        Intent intent = new Intent();
        intent.setClass(this, IssueListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}