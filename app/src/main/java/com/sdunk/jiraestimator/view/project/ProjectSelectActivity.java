package com.sdunk.jiraestimator.view.project;

import android.content.Intent;
import android.os.Bundle;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.api.JiraService;
import com.sdunk.jiraestimator.api.JiraServiceFactory;
import com.sdunk.jiraestimator.databinding.ActivityProjectSelectBinding;
import com.sdunk.jiraestimator.databinding.ProjectListItemBinding;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.db.user.UserDAO;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.GenericResponse;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.model.Project;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectSelectActivity extends AppCompatActivity {

    private ActivityProjectSelectBinding binding;

    private final ArrayList<Project> projects = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_project_select);
        binding.projectList.setLayoutManager(new LinearLayoutManager(this));
        binding.projectList.setAdapter(new GenericRVAdapter<Project, ProjectListItemBinding>(this, projects) {
            @Override
            public int getLayoutResId() {
                return R.layout.project_list_item;
            }

            @Override
            public void onBindData(Project project, int position, ProjectListItemBinding binding) {
                binding.setProject(project);
            }

            @Override
            public void onItemClick(Project project, int position) {
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
        startActivity(new Intent().setClass(this, IssueListActivity.class));
        finish();
    }
}