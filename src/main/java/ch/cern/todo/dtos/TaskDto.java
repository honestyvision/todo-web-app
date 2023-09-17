package ch.cern.todo.dtos;

import ch.cern.todo.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TaskDto {
  private Integer id;
  private Integer categoryId;
  private String name;
  private String description;
  private String deadline;

  public static TaskDto from(Task task) {
    return TaskDto.builder()
        .id(task.getId())
        .name(task.getName())
        .description(task.getDescription())
        .categoryId(task.getCategory().getCategoryId())
        .deadline(task.getDeadline().toLocalDateTime().toString())
        .build();
  }
}
