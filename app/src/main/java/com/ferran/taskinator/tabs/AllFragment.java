package com.ferran.taskinator.tabs;

public class AllFragment extends TaskListFragment {

    protected void getTasks() {
        cursor = dbc.getAllTasks();
    }
}
