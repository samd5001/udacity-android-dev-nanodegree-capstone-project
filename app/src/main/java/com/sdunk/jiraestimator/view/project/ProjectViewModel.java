package com.sdunk.jiraestimator.view.project;

import android.app.Application;

import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.user.ProjectDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.Project;
import com.sdunk.jiraestimator.model.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import lombok.Getter;

@Getter
public class ProjectViewModel extends AndroidViewModel {

    private LiveData<List<Project>> projects;
    private User user;

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        projects = ProjectDatabase.getInstance(getApplication()).projectDao().loadProjects();
        DBExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                user = UserDatabase.getInstance(getApplication()).userDao().getLoggedInUser();
            }
        });
    }
}
