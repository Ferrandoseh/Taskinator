package com.ferran.taskinator.tabs;

public class MonthFragment extends TaskListFragment {

    protected void getTasks() {
        cursor = dbc.getMonthTasks();
    }
}
