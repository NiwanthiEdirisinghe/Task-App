package com.example.taskapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener  {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DBHelper dbHelper;
    private TextView emptyView;
    private Button addTaskButton;

    private static final int REQUEST_CODE_ADD_TASK = 1;
    private static final int REQUEST_CODE_VIEW_TASK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        recyclerView = findViewById(R.id.recycler_tasks);
        emptyView = findViewById(R.id.text_empty_view);
        addTaskButton = findViewById(R.id.button_add_task);

        taskList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this, this);
        recyclerView.setAdapter(taskAdapter);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Add_edit_task_view_Activity.class);
                startActivity(intent);
            }
        });

        loadTasksFromDatabase();

    }

    private void loadTasksFromDatabase() {
        taskList.clear();

        Cursor cursor = dbHelper.getAllTasks();

        if (cursor.getCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("id");
                int titleIndex = cursor.getColumnIndex("title");
                int descriptionIndex = cursor.getColumnIndex("description");
                int dueDateIndex = cursor.getColumnIndex("due_date");

                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1) {
                    long id = cursor.getLong(idIndex);
                    String title = cursor.getString(titleIndex);
                    String description = cursor.getString(descriptionIndex);
                    String dueDate = cursor.getString(dueDateIndex);

                    Task task = new Task(id, title, description, dueDate);
                    taskList.add(task);
                }
            }
        }

        cursor.close();
        taskAdapter.notifyDataSetChanged();
    }

    public void onTaskClick(int position) {
        Task task = taskList.get(position);
        Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
        intent.putExtra("task_id", task.getId());
        startActivityForResult(intent, REQUEST_CODE_VIEW_TASK);
    }

    public void onTaskLongClick(int position) {
        // Show quick options dialog (Edit/Delete)
        Task task = taskList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(task.getTitle());
        String[] options = {"Edit", "Delete"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Edit
                Intent intent = new Intent(MainActivity.this, Add_edit_task_view_Activity.class);
                intent.putExtra("task_id", task.getId());
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
            } else if (which == 1) {
                // Delete confirmation
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
                deleteBuilder.setTitle("Delete Task");
                deleteBuilder.setMessage("Are you sure you want to delete this task?");

                deleteBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
                    boolean success = dbHelper.deleteTask(task.getId());
                    if (success) {
                        loadTasksFromDatabase();
                        Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                    }
                });

                deleteBuilder.setNegativeButton("Cancel", null);
                deleteBuilder.show();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_ADD_TASK || requestCode == REQUEST_CODE_VIEW_TASK)
                && resultCode == RESULT_OK) {
            loadTasksFromDatabase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDatabase();
    }
}