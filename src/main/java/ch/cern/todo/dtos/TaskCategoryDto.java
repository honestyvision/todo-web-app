package ch.cern.todo.dtos;

import ch.cern.todo.entities.TaskCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TaskCategoryDto {
  private Integer categoryId;
  private String name;
  private String description;

  public static TaskCategoryDto from(TaskCategory taskCategory) {
    return TaskCategoryDto.builder()
        .categoryId(taskCategory.getCategoryId())
        .name(taskCategory.getName())
        .description(taskCategory.getDescription())
        .build();
  }
}
