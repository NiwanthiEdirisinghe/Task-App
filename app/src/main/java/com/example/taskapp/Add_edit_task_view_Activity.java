package com.example.taskapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class Add_edit_task_view_Activity extends AppCompatActivity {
    private EditText editTitle, editDescription;
    private TextView textDueDate, textEditorTitle;
    private Button buttonChooseDate, buttonSaveTask, buttonGoHome;
    private DBHelper dbHelper;
    private Calendar calendar;
    private long taskId = -1;
    private SimpleDateFormat dateFormat;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DBHelper(this);

        editTitle = findViewById(R.id.edit_task_title);
        editDescription = findViewById(R.id.edit_task_description);
        textDueDate = findViewById(R.id.text_due_date);
        buttonChooseDate = findViewById(R.id.button_choose_date);
        buttonSaveTask = findViewById(R.id.button_save_task);
        textEditorTitle = findViewById(R.id.add_edit_page_title);
        buttonGoHome = findViewById(R.id.button_go_home);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        updateDateText();

        if (getIntent().hasExtra("task_id")) {
            taskId = getIntent().getLongExtra("task_id", -1);
            loadTaskData();
            textEditorTitle.setText("Edit Task");
        } else {
            textEditorTitle.setText("Add New Task");
        }

        buttonChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        textDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        buttonGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Add_edit_task_view_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateText() {
        textDueDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void loadTaskData() {
        Cursor cursor = dbHelper.getTaskById(taskId);

        if (cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("title");
            int descriptionIndex = cursor.getColumnIndex("description");
            int dueDateIndex = cursor.getColumnIndex("due_date");

            if (titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1) {
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex);
                String dueDate = cursor.getString(dueDateIndex);

                editTitle.setText(title);
                editDescription.setText(description);

                try {
                    calendar.setTime(Objects.requireNonNull(dateFormat.parse(dueDate)));
                    updateDateText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
    }

    private void saveTask() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String dueDate = textDueDate.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a title for the task", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;

        if (taskId == -1) {
            long newRowId = dbHelper.insertTask(title, description, dueDate);
            success = newRowId != -1;
        } else {
            success = dbHelper.updateTask(taskId, title, description, dueDate);
        }

        if (success) {
            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}