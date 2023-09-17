package ch.cern.todo.resource;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import ch.cern.todo.dtos.TaskDto;
import ch.cern.todo.service.TaskService;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskResource {

  private TaskService taskService;

  public TaskResource(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping("/")
  public ResponseEntity getTasks() {
    try {
      List<TaskDto> tasks = taskService.getTasks();
      return ResponseEntity.ok(tasks);
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{taskId}")
  public ResponseEntity getTaskById(@PathVariable int taskId) {
    try {
      TaskDto task = taskService.getTaskById(taskId);

      if (task == null) return ResponseEntity.notFound().build();
      return ResponseEntity.ok(task);
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/create")
  public ResponseEntity createTask(@RequestBody TaskDto taskDto) {
    try {
      TaskDto createdTask = taskService.saveTask(taskDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    } catch(DataIntegrityViolationException exception) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @PatchMapping("/update")
  public ResponseEntity updateTask(@RequestBody TaskDto taskDto) {
    try {
      TaskDto createdTask = taskService.saveTask(taskDto);
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdTask);

    } catch(DataIntegrityViolationException exception) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/delete/{taskId}")
  public ResponseEntity deleteTask(@PathVariable Integer taskId) {
    try {
      taskService.deleteTask(taskId);
      return ResponseEntity.ok().build();

    } catch(EmptyResultDataAccessException exception) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }
}
