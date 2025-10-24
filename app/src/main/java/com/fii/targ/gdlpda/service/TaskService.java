package com.fii.targ.gdlpda.service;

import android.util.Log;

import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskService {

    private static final String TAG = "TaskService";

    // 查询当前任务数据
    public static List<Task> getTasksFromServer(int count, boolean order) {
        try {
            QueryTaskRequest requestBody = new QueryTaskRequest(count, order);
            ApiResponse<TaskResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.QUERY_TASK, requestBody, TaskResponse.class);
            Log.d("getTasksFromServer", "" + response);
            if (response.isSuccess()) {
                TaskResponse taskResponse = response.getData();
                if (taskResponse != null && taskResponse.getData() != null) {
                    return convertToTaskList(taskResponse.getData());
                } else {
                    Log.e(TAG, "Task data is null or empty");
                    return null;
                }
            } else {
                Log.e(TAG, "Error fetching tasks: " + response.getErrorMessage());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 转换任务数据为 Task 模型列表
    public static List<Task> convertToTaskList(List<TaskResponse.TaskData> taskDataList) {
        List<Task> tasks = new ArrayList<>();
        if (taskDataList == null) return tasks;

        for (TaskResponse.TaskData taskData : taskDataList) {
            tasks.add(new Task(
                    taskData.getTaskCode(),
                    taskData.getStatus(),
                    taskData.getStage(),
                    taskData.getStartCell(),
                    taskData.getTargetCell(),
                    taskData.getCarCode(),
                    taskData.getAgvId()
            ));
        }
        return tasks;
    }

    // 取消任务
    public static boolean cancelTask(String taskCode) {
        try {
            CancelTaskRequest requestBody = new CancelTaskRequest(taskCode);
            ApiResponse<CancelTaskResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.CANCEL_TASK, requestBody, CancelTaskResponse.class);
            return response.isSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error canceling task", e);
            return false;
        }
    }

    public static class CancelTaskRequest {
        private String taskCode;

        public CancelTaskRequest(String taskCode) {
            this.taskCode = taskCode;
        }

        public String getTaskCode() {
            return taskCode;
        }
    }

    public static class CancelTaskResponse {
        private String code;      // 通讯状态码
        private String message;       // 通讯详细信息

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class QueryTaskRequest {
        private int count;

        private boolean order;

        public QueryTaskRequest(int count, boolean order) {
            this.count = count;
            this.order = order;
        }

    }

    // 内部类：任务响应数据模型
    public static class TaskResponse {

        private String code;      // 通讯状态码
        private String message;       // 通讯详细信息
        private List<TaskData> data; // 返回的任务数据列表

       
        public String getCode() {
            return code;
        }

        public String getMsg() {
            return message;
        }

        public List<TaskData> getData() {
            return data;
        }

        public static class TaskData {
            private String id;
            private String taskCode;
            private String taskName;
            private String description;
            private String status;
            private String endReason;
            private String stage;
            private String createTime;
            private String updateTime;
            private String createUser;
            private String updateUser;
            private String priority;
            private String startStationCode;
            private String targetStationCode;
            private String taskModelType;
            private String type;
            private String carCode;
            private String flProductSN;
            private String startCell;
            private String targetCell;
            private String agvId;

            public String getId() {
                return id;
            }

            public String getTaskCode() {
                return taskCode;
            }

            public String getTaskName() {
                return taskName;
            }

            public String getDescription() {
                return description;
            }

            public String getStatus() {
                return status;
            }

            public String getEndReason() {
                return endReason;
            }

            public String getStage() {
                return stage;
            }

            public String getCreateTime() {
                return createTime;
            }

            public String getUpdateTime() {
                return updateTime;
            }

            public String getCreateUser() {
                return createUser;
            }

            public String getUpdateUser() {
                return updateUser;
            }

            public String getPriority() {
                return priority;
            }

            public String getStartStationCode() {
                return startStationCode;
            }

            public String getTargetStationCode() {
                return targetStationCode;
            }

            public String getTaskModelType() {
                return taskModelType;
            }

            public String getType() {
                return type;
            }

            public String getCarCode() {
                return carCode;
            }

            public String getFlProductSN() {
                return flProductSN;
            }

            public String getStartCell() {
                return startCell;
            }

            public String getTargetCell() {
                return targetCell;
            }

            public String getAgvId() {
                return agvId;
            }
        }
    }
}