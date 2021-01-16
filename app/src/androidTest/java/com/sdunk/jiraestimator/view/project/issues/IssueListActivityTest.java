package com.sdunk.jiraestimator.view.project.issues;

import android.content.Context;
import android.content.Intent;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.api.APIIdlingResource;
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.view.issues.IssueDetailActivity;
import com.sdunk.jiraestimator.view.issues.IssueDetailFragment;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.sdunk.jiraestimator.view.TestUtils.atPosition;

public class IssueListActivityTest {

    private IssueDatabase db;

    @Rule
    public ActivityTestRule<IssueListActivity> activityTestRule
            = new ActivityTestRule<>(IssueListActivity.class, false, false);

    APIIdlingResource apiIdlingResource;

    @Before
    public void setupTests() {
        apiIdlingResource = APIUtils.getApiIdlingResource();
        IdlingRegistry.getInstance().register(apiIdlingResource);
        Intents.init();
    }

    @After
    public void cleanup() {
        Intents.release();
    }

    @Test
    public void renderView_SelectIssue() {
        activityTestRule.launchActivity(new Intent());
        onView(ViewMatchers.withId(R.id.issue_list_layout)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Intents.intended(hasComponent(IssueDetailActivity.class.getName()));
    }


}
