package com.sdunk.jiraestimator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.db.project.ProjectDAO;
import com.sdunk.jiraestimator.db.user.ProjectDatabase;
import com.sdunk.jiraestimator.db.user.UserDAO;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.Project;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;
import com.sdunk.jiraestimator.view.login.LoginActivity;
import com.sdunk.jiraestimator.view.login.LoginUser;
import com.sdunk.jiraestimator.view.project.ProjectSelectActivity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

public class SplashScreenActivity extends AppCompatActivity {

    private final MutableLiveData<LoginUser> mutableUserData = new MutableLiveData<>();

    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Executor diskIo = DBExecutor.getInstance().diskIO();

        mutableUserData.observe(this, (liveUser) -> {
            if (liveUser.getProjectList() != null) {
                diskIo.execute(() -> updateProjects(liveUser.getProjectList()));
            } else {
                diskIo.execute(this::handleUserUnableToConnect);
            }
        });

        diskIo.execute(this::checkUserAndStartActivity);
    }

    private void checkUserAndStartActivity() {
        user = UserDatabase.getInstance(getApplicationContext()).userDao().getLoggedInUser();

        if (user == null) {
            startNewActivity(LoginActivity.class);
        } else {


            LoginUser loginUser = new LoginUser(user.getJiraUrl(), user.getEmail(), user.getToken());

            APIUtils.getUserProjects(loginUser, mutableUserData);
        }
    }

    private void handleUserUnableToConnect() {
        UserDatabase.getInstance(getApplicationContext()).userDao().logoutUser();
        ProjectDatabase.getInstance(getApplicationContext()).projectDao().clearProjects();
        IssueDatabase.getInstance(getApplicationContext()).issueDAO().clearIssues();
        runOnUiThread(() -> Toast.makeText(SplashScreenActivity.this, R.string.unable_to_login, Toast.LENGTH_SHORT).show());
        startNewActivity(LoginActivity.class);
    }

    private void updateProjects(List<Project> projects) {
        ProjectDAO dao = ProjectDatabase.getInstance(getApplicationContext()).projectDao();
        dao.clearProjects();
        dao.insertProjects(projects);

        Optional<Project> currentProject = projects.stream().filter(project -> project.getKey().equals(user.getProjectKey())).findFirst();
        if (currentProject.isPresent()) {
            startNewActivity(IssueListActivity.class);
        } else {
            IssueDatabase.getInstance(getApplicationContext()).issueDAO().clearIssues();
            runOnUiThread(() -> Toast.makeText(

                    SplashScreenActivity.this, R.string.project_not_found, Toast.LENGTH_SHORT).show());
            startNewActivity(ProjectSelectActivity.class);
        }

    }

    private void startNewActivity(Class<?> clazz) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), clazz);
        startActivity(intent);
        finish();
    }
}