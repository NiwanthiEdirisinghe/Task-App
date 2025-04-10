package com.example.taskapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TaskDetailActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_task_detail);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }

    private TextView textTitle, textDueDate, textDescription;
    private Button buttonEdit, buttonDelete, buttonGoHome;

    private DBHelper dbHelper;
    private long taskId = -1;
    private static final int REQUEST_CODE_EDIT_TASK = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DBHelper(this);

        textTitle = findViewById(R.id.text_detail_title);
        textDueDate = findViewById(R.id.text_detail_due_date);
        textDescription = findViewById(R.id.text_detail_description);
        buttonEdit = findViewById(R.id.button_edit_task);
        buttonDelete = findViewById(R.id.button_delete_task);
        buttonGoHome = findViewById(R.id.button_go_home);

        if (getIntent().hasExtra("task_id")) {
            taskId = getIntent().getLongExtra("task_id", -1);
            //loadTaskData();
        } else {
            Toast.makeText(this, "Error: No task ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditTask();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteTask();
            }
        });


        buttonGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        this.loadTaskData();
    }

    private void loadTaskData() {
        Cursor cursor = dbHelper.getTaskById(taskId);

        if (cursor != null && cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("title");
            int descriptionIndex = cursor.getColumnIndex("description");
            int dueDateIndex = cursor.getColumnIndex("due_date");

            if (titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1) {
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex);
                String dueDate = cursor.getString(dueDateIndex);

                textTitle.setText(title);
                textDescription.setText(description);
                textDueDate.setText(dueDate);
            } else {
                Toast.makeText(this, "Error: Could not find columns in database", Toast.LENGTH_SHORT).show();
                finish();
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openEditTask() {
        Intent intent = new Intent(TaskDetailActivity.this, Add_edit_task_view_Activity.class);
        intent.putExtra("task_id", taskId);
        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    private void confirmDeleteTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            boolean success = dbHelper.deleteTask(taskId);
            if (success) {
                Toast.makeText(TaskDetailActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(TaskDetailActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_TASK && resultCode == RESULT_OK) {
            loadTaskData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}