# Udacity Android Capstone Project

## JIRA Estimator

This application is the final submission for the Udacity Android Nanodegree.

The application allows you to login and view issues from a Jira cloud project. 

It also allows for local estimation of story points, through the [Nearby Communications API.](https://developers.google.com/nearby/connections/overview)

## Prerequisites
To use the app you will need the following
* A Jira Cloud account and instance (available for free [here.](https://www.atlassian.com/software/jira))
* A Jira project on the cloud instance.
* Some Jira issues created
* An API auth token to login with (instructions [here.](https://confluence.atlassian.com/cloud/api-tokens-938839638.html))
* Make the story point field editable (detailed below).

### Enabling Story points field updates through REST
Follow these steps to setup your Jira instance for REST communication:
* Login to your Jira Instance.
* Click the settings cog in the top right corner.
* Click Issues.
* Click custom fields in the left sidebar.
* Type story into the filter box.
* When loaded click `Story Points`.
* Click screens in the modal window.
* Check every checkbox on the screens list.
* Click update. 

## Features
### Estimation screens
When estimating a story an app user can host or join a session. On the estimate session list screen any nearby devices hosting will show as an email address. You will only see users that are logged in with a different account to your own and are on the same issue.

After starting a session a grid of cards following the fibonacci sequence will appear to all users. After everyone has picked their estimate if there is a winner the issue will be updated via the Jira REST API. Otherwise more votes will happen. 

A vote session can be done by a single user.

### Widget
The app includes a widget. It provides quick info for an Issue. To populate the widget, login to the app and on an issue detail screen tap the `Show in widget` checkbox

### Instrumented Tests
The app comes with some instrumented tests. The tests require a Jira cloud instance with at least one project and one key, the URL for the cloud instance, a login email and API auth token to pass.

To provide these values replace the TEST_URL, TEST_EMAIL and TEST_TOKEN placeholder values in `app/build.gradle`.