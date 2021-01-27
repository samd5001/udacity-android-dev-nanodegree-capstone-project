package com.sdunk.jiraestimator.model;

import java.util.List;

import lombok.Value;

@Value
public class IssueResponse {

    List<JiraIssue> issues;
}
