package com.ferran.taskinator.tabs;

public class SomedayFragment extends TaskListFragment {

    protected void getTasks() {
        cursor = dbc.getSomedayTasks();
    }
}
