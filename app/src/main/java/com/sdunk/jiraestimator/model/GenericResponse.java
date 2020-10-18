package com.sdunk.jiraestimator.model;

import java.util.List;

import lombok.Value;

@Value
public class GenericResponse<T> {
    private List<T> values;
}
