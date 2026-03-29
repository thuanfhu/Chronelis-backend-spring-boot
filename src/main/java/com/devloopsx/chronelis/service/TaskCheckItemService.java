package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.checkitem.CreateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.request.checkitem.ReorderTaskCheckItemsRequest;
import com.devloopsx.chronelis.dto.request.checkitem.UpdateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.response.checkitem.TaskCheckItemResponse;

import java.util.List;

public interface TaskCheckItemService {
    TaskCheckItemResponse createCheckItem(CreateTaskCheckItemRequest request);

    TaskCheckItemResponse updateCheckItem(Long checkItemId, UpdateTaskCheckItemRequest request);

    TaskCheckItemResponse toggleCheckItem(Long checkItemId);

    void deleteCheckItem(Long checkItemId);

    List<TaskCheckItemResponse> listByTask(Long taskId);

    void reorderCheckItems(ReorderTaskCheckItemsRequest request);
}
