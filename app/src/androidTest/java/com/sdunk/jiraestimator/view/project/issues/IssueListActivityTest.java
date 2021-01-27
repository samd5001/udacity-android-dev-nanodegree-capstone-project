package com.sdunk.jiraestimator.view.project.issues;

import android.content.Intent;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.api.APIIdlingResource;
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.view.issues.IssueDetailActivity;
import com.sdunk.jiraestimator.view.issues.IssueListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

public class IssueListActivityTest {

    @Rule
    public final ActivityTestRule<IssueListActivity> activityTestRule
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
