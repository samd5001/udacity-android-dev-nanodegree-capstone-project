package com.sdunk.jiraestimator.view.project;

import android.content.Context;
import android.content.Intent;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.api.APIIdlingResource;
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.db.user.ProjectDatabase;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.model.Project;
import com.sdunk.jiraestimator.model.User;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.Collections;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
public class ProjectSelectActivityTest {

    @Before
    public void setupTests() {
        Intents.init();
    }

    @Test
    public void renderView_SelectProject() {
            ActivityScenario.launch(ProjectSelectActivity.class);
            onView(ViewMatchers.withId(R.id.project_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
            Intents.intended(hasComponent(IssueListActivity.class.getName()));
    }

    @After
    public void cleanupTests() {
        Intents.release();
    }

}
