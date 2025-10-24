package com.fii.targ.gdlpda.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fii.targ.gdlpda.R;
import com.fii.targ.gdlpda.model.Task;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> taskList;
    private final OnTaskActionListener listener;

    // 构造函数
    public TaskAdapter(Context context, List<Task> taskList, OnTaskActionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // 设置任务号

        String taskNumber = task.getTaskNumber();
        if (taskNumber.length() > 12) {
            taskNumber = taskNumber.substring(taskNumber.length() - 12);
        }
        holder.taskNumber.setText("Task: " + taskNumber);

        // 设置状态和阶段
        holder.taskStatus.setText("" + task.getStatus());
        holder.taskStage.setText("" + task.getStage());

        // 设置起点到终点
        holder.taskStartPoint.setText(task.getStartPoint());
        holder.taskEndPoint.setText(task.getEndPoint());

        // 设置台车号和 AGV 号
        holder.taskTrolleyNumber.setText(task.getTrolleyNumber());
        holder.taskAgvNumber.setText(task.getAgvNumber());

        // 设置取消按钮点击事件
        holder.cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelTask(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // ViewHolder 内部类
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskNumber, taskStatus, taskStage, taskStartPoint, taskEndPoint, taskTrolleyNumber, taskAgvNumber;
        Button cancelButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            taskNumber = itemView.findViewById(R.id.task_number);
            taskStatus = itemView.findViewById(R.id.task_status);
            taskStage = itemView.findViewById(R.id.task_stage);
            taskStartPoint = itemView.findViewById(R.id.task_start_point);
            taskEndPoint = itemView.findViewById(R.id.task_end_point);
            taskTrolleyNumber = itemView.findViewById(R.id.task_trolley_number);
            taskAgvNumber = itemView.findViewById(R.id.task_agv_number);
            cancelButton = itemView.findViewById(R.id.cancel_button);
        }
    }

    // 任务操作监听器接口
    public interface OnTaskActionListener {
        void onCancelTask(Task task);
    }
}