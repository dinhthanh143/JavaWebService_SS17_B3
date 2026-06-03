package com.example.project.b3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProjectManagementController {

    // Giả định TaskService được tự động tiêm (Inject) thông qua Bean đặt tên là "taskService"
    @Autowired
    private TaskService taskService;

    /**
     * Quy tắc 1: Chỉ Product Owner (PO) mới có thể tạo dự án.
     */
    @PostMapping("/projects")
    @PreAuthorize("hasRole('PO')")
    public String createProject() {
        return "Dự án mới đã được tạo bởi Product Owner.";
    }

    /**
     * Quy tắc 2: Chỉ Scrum Master (SM) mới có thể quản lý sprint.
     */
    @PutMapping("/sprints/{id}")
    @PreAuthorize("hasRole('SM')")
    public String manageSprint(@PathVariable UUID id) {
        return "Sprint " + id + " đã được quản lý bởi Scrum Master.";
    }

    /**
     * Quy tắc 3: Chỉ DEV hoặc QA mới có thể cập nhật trạng thái tác vụ của chính họ.
     * Giải thích SpEL:
     * - (hasRole('DEV') or hasRole('QA')): Kiểm tra vai trò phù hợp.
     * - and: Kết hợp điều kiện bắt buộc.
     * - @taskService.isTaskOwner(#taskId, authentication.name): Gọi hàm check chính chủ từ TaskService,
     * truyền #taskId từ @PathVariable và tên tài khoản đang login hiện tại.
     */
    @PutMapping("/tasks/{taskId}/status")
    @PreAuthorize("(hasRole('DEV') or hasRole('QA')) and @taskService.isTaskOwner(#taskId, authentication.name)")
    public String updateTaskStatus(@PathVariable UUID taskId) {
        // Logic cập nhật trạng thái task
        return "Trạng thái tác vụ " + taskId + " đã được cập nhật.";
    }

    /**
     * Quy tắc 4: Chỉ người tạo task (Task Owner) HOẶC Product Owner (PO) mới được xóa task.
     * Giải thích SpEL:
     * - Nếu user đăng nhập có Role là 'PO' -> Cho phép luôn không cần check tiếp (Luật đoản mạch 'or').
     * - Nếu không phải PO, hệ thống kiểm tra vế sau: Gọi taskService kiểm tra xem tài khoản login có phải người tạo không.
     */
    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('PO') or @taskService.isTaskOwner(#taskId, authentication.name)")
    public String deleteTask(@PathVariable UUID taskId) {
        return "Tác vụ " + taskId + " đã được xóa.";
    }
}