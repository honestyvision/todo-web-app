package ch.cern.todo.entities;

import ch.cern.todo.dtos.TaskCategoryDto;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class TaskCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer categoryId;

  @Column(nullable = false, length = 100, unique = true)
  private String name;

  @Column(length = 500)
  private String description;

  @OneToMany(mappedBy = "category")
  private Set<Task> tasks;

  public static TaskCategory from(Integer id) {
    return TaskCategory.builder()
        .categoryId(id).build();
  }

  public static TaskCategory from(TaskCategoryDto taskCategoryDto) {
    return TaskCategory.builder()
        .categoryId(taskCategoryDto.getCategoryId())
        .name(taskCategoryDto.getName())
        .description(taskCategoryDto.getDescription())
        .build();
  }
}
