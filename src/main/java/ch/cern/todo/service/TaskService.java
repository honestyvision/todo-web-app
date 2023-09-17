package ch.cern.todo.service;

import ch.cern.todo.dtos.TaskDto;
import ch.cern.todo.entities.Task;
import ch.cern.todo.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private TaskRepository taskRepository;

  public TaskService (TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public List<TaskDto> getTasks() {
    List<TaskDto> tasks = new ArrayList<>();
    taskRepository.findAll().forEach(task -> {
      tasks.add(TaskDto.from(task));
    });
    return tasks;
  }

  public TaskDto getTaskById(Integer id) {
    Optional<Task> task = taskRepository.findById(id);
    if (task.isPresent())
      return TaskDto.from(task.get());
    return null;
  }

  public TaskDto saveTask(TaskDto taskDto) {
    Task newTask = Task.from(taskDto);
    return TaskDto.from(taskRepository.save(newTask));
  }

  public void deleteTask(Integer id) {
    taskRepository.deleteById(id);
  }

}
