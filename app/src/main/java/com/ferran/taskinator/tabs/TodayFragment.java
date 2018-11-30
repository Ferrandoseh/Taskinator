package com.ferran.taskinator.tabs;

public class TodayFragment extends TaskListFragment {

    protected void getTasks() {
        cursor = dbc.getDayTasks();
    }
}
