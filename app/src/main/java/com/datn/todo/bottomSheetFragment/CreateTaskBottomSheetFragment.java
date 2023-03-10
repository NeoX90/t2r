package com.datn.todo.bottomSheetFragment;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.datn.todo.R;
import com.datn.todo.activity.MainActivity;
import com.datn.todo.broadcastReceiver.AlarmBroadcastReceiver;
import com.datn.todo.database.DatabaseClient;
import com.datn.todo.model.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CreateTaskBottomSheetFragment extends BottomSheetDialogFragment {

    EditText addTaskTitle;
    EditText addTaskDescription;
    EditText taskDate;
    EditText taskTime;
    Button addTask;
    TextView title;
    TextView description;
    int taskId;
    boolean isEdit;
    Task task;
    int mYear, mMonth, mDay;
    int mHour, mMinute;
    setRefreshListener setRefreshListener;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    MainActivity activity;
    public static int count = 0;

    public void setTaskId(int taskId, boolean isEdit, setRefreshListener setRefreshListener, MainActivity activity) {
        this.taskId = taskId;
        this.isEdit = isEdit;
        this.activity = activity;
        this.setRefreshListener = setRefreshListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility", "SuspiciousIndentation"})
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_create_task, null);
        dialog.setContentView(contentView);
        alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

        taskTime = dialog.findViewById(R.id.taskTime);
        taskTime.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                timePickerDialog = new TimePickerDialog(getActivity(),
                        (view12, hourOfDay, minute) -> {
                            taskTime.setText(hourOfDay + ":" + minute);
                            timePickerDialog.dismiss();
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
            return true;
        });

        taskDate = dialog.findViewById(R.id.taskDate);
        taskDate.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(getActivity(), (view1, year, monthOfYear, dayOfMonth) -> {
                    taskDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    datePickerDialog.dismiss();
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
            return true;
        });

        addTask = dialog.findViewById(R.id.addTask);
        addTask.setOnClickListener(view -> {
            if (validateFields()) createTask();
        });

        addTaskDescription = dialog.findViewById(R.id.addTaskDescription);
        addTaskTitle = dialog.findViewById(R.id.addTaskTitle);
        title = dialog.findViewById(R.id.tv_title);
        description = dialog.findViewById(R.id.tv_description);

        if (isEdit) {
            showTaskFromId();
            title.setText("S???a ghi ch??");
            description.setText("Th??m th???t n???i dung b??n d?????i ????? ch???nh s???a ghi ch??");
            addTask.setText("S???a");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view_, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view_, savedInstanceState);

//        if (isEdit) {
//            showTaskFromId();
//        }
    }

    public boolean validateFields() {
        if (addTaskTitle.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Vui l??ng nh???p ti??u ?????", Toast.LENGTH_SHORT).show();
            return false;
        } else if (addTaskDescription.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Vui l??ng nh???p n???i dung", Toast.LENGTH_SHORT).show();
            return false;
        } else if (taskDate.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Vui l??ng nh???p ng??y", Toast.LENGTH_SHORT).show();
            return false;
        } else if (taskTime.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Vui l??ng nh???p gi???", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void createTask() {
        class saveTaskInBackend extends AsyncTask<Void, Void, Void> {
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                Task createTask = new Task();
                createTask.setTaskTitle(addTaskTitle.getText().toString());
                createTask.setTaskDescrption(addTaskDescription.getText().toString());
                createTask.setDate(taskDate.getText().toString());
                createTask.setTime(taskTime.getText().toString());

                if (!isEdit)
                    DatabaseClient.getInstance(getActivity()).getAppDatabase().dataBaseAction().insertDataIntoTaskList(createTask);
                else
                    DatabaseClient.getInstance(getActivity()).getAppDatabase().dataBaseAction().updateAnExistingRow(taskId, addTaskTitle.getText().toString(), addTaskDescription.getText().toString(), taskDate.getText().toString(), taskTime.getText().toString(), false);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createAnAlarm();
                }
                setRefreshListener.refresh();
                Toast.makeText(getActivity(), "ghi ch?? c???a b???n ???? ???????c th??m", Toast.LENGTH_SHORT).show();
                dismiss();

            }
        }
        saveTaskInBackend st = new saveTaskInBackend();
        st.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createAnAlarm() {
        try {
            String[] items1 = taskDate.getText().toString().split("-");
            String dd = items1[0];
            String month = items1[1];
            String year = items1[2];

            String[] itemTime = taskTime.getText().toString().split(":");
            String hour = itemTime[0];
            String min = itemTime[1];

            Calendar cur_cal = new GregorianCalendar();
            cur_cal.setTimeInMillis(System.currentTimeMillis());

            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
            cal.set(Calendar.MINUTE, Integer.parseInt(min));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.DATE, Integer.parseInt(dd));

            Intent alarmIntent = new Intent(activity, AlarmBroadcastReceiver.class);
            alarmIntent.putExtra("TITLE", addTaskTitle.getText().toString());
            alarmIntent.putExtra("DESC", addTaskDescription.getText().toString());
            alarmIntent.putExtra("DATE", taskDate.getText().toString());
            alarmIntent.putExtra("TIME", taskTime.getText().toString());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, count, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                count++;

//                PendingIntent intent = PendingIntent.getBroadcast(activity, count, alarmIntent, 0);
//                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() - 600000, intent);
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() - 600000, intent);
//                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTaskFromId() {
        class showTaskFromId extends AsyncTask<Void, Void, Void> {
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                task = DatabaseClient.getInstance(getActivity()).getAppDatabase().dataBaseAction().selectDataFromAnId(taskId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setDataInUI();
            }
        }
        showTaskFromId st = new showTaskFromId();
        st.execute();
    }

    private void setDataInUI() {
        addTaskTitle.setText(task.getTaskTitle());
        addTaskDescription.setText(task.getTaskDescrption());
        taskDate.setText(task.getDate());
        taskTime.setText(task.getTime());
    }

    public interface setRefreshListener {
        void refresh();
    }
}
