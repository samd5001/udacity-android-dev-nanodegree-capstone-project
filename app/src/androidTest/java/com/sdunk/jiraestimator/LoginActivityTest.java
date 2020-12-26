package com.sdunk.jiraestimator;

import android.content.Intent;
import android.widget.TextView;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.sdunk.jiraestimator.db.user.UserDAO;
import com.sdunk.jiraestimator.db.user.UserDatabase;
import com.sdunk.jiraestimator.view.login.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    @Rule
    public ActivityTestRule<LoginActivity> activityTestRule
            = new ActivityTestRule<>(LoginActivity.class, false, false);

    @Test
    public void renderView_RendersLogin() {
        activityTestRule.launchActivity(new Intent());

        onView(allOf(instanceOf(TextView.class), withParent(withResourceName("action_bar"))))
                .check(matches(withText("Jira Estimator - Login")));
        onView(withId(R.id.url)).check(matches(isDisplayed()));
        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.token)).check(matches(isDisplayed()));
        onView(withId(R.id.loading)).check(matches(not(isDisplayed())));
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }

    @Test
    public void doLogin_Error() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.url)).perform(typeText("hopefullyinvalidbecauseitwouldbeweirdifnot.atlassian.net"));
        onView(withId(R.id.email)).perform(typeText("test@test.com"));
        onView(withId(R.id.token)).perform(typeText("token"));
        onView(withId(R.id.login_button)).perform(click());

        onView(withText(R.string.login_api_error_message)).inRoot(withDecorView(not(activityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void doLogin_Success() {
        activityTestRule.launchActivity(new Intent());
        UserDAO dao = UserDatabase.getInstance(activityTestRule.getActivity()).userDao();
        onView(withId(R.id.url)).perform(typeText(activityTestRule.getActivity().getString(R.string.test_login_url)));
        onView(withId(R.id.email)).perform(typeText(activityTestRule.getActivity().getString(R.string.test_login_email)));
        onView(withId(R.id.token)).perform(typeText(activityTestRule.getActivity().getString(R.string.test_login_token)));
        onView(withId(R.id.login_button)).perform(click());

        await().until(() -> dao.getLoggedInUser() != null);
    }

    @Test
    public void showErrors_ShowsAllErrors() {
        activityTestRule.launchActivity(new Intent());

        onView(withId(R.id.login_button)).perform(click());

        onView(withId(R.id.url)).check(matches(hasErrorText("Not a valid Jira Cloud URL")));
        onView(withId(R.id.email)).check(matches(hasErrorText("Not a valid email")));
        onView(withId(R.id.token)).check(matches(hasErrorText("Token / password must be entered")));
    }

    @Test
    public void showErrors_ShowsUrlError() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.email)).perform(typeText("test@test.com"));
        onView(withId(R.id.token)).perform(typeText("token"));

        ViewInteraction loginButton = onView(withId(R.id.login_button));
        ViewInteraction url = onView(withId(R.id.url));

        loginButton.perform(click());

        url.check(matches(hasErrorText("Not a valid Jira Cloud URL")))
                .perform(typeText("This is invalid"));

        loginButton.perform(click());

        url.check(matches(hasErrorText("Not a valid Jira Cloud URL")))
                .perform(replaceText("test.atlassian.net"));
    }

    @Test
    public void showErrors_ShowsEmailError() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.url)).perform(typeText("test.atlassian.net"));
        onView(withId(R.id.token)).perform(typeText("token"));

        ViewInteraction loginButton = onView(withId(R.id.login_button));
        ViewInteraction email = onView(withId(R.id.email));

        loginButton.perform(click());

        email.check(matches(hasErrorText("Not a valid email")))
                .perform(typeText("This is invalid"));

        loginButton.perform(click());

        email.check(matches(hasErrorText("Not a valid email")))
                .perform(replaceText("test@test.net"));
    }

    @Test
    public void showErrors_ShowsTokenError() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.url)).perform(typeText("test.atlassian.net"));
        onView(withId(R.id.email)).perform(typeText("test@test.com"));
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.token)).check(matches(hasErrorText("Token / password must be entered")))
                .perform(typeText("token"));
    }
}