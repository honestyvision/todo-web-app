package ch.cern.todo.entities;

import static ch.cern.todo.utils.DateUtils.getSqlTimeStamp;

import ch.cern.todo.dtos.TaskDto;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoryId")
  private TaskCategory category;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(nullable = false)
  private Timestamp deadline;

  public static Task from(TaskDto taskDto) {
    TaskBuilder builder = Task.builder()
        .name(taskDto.getName())
        .category(TaskCategory.from(taskDto.getCategoryId()))
        .description(taskDto.getDescription())
        .deadline(getSqlTimeStamp(taskDto.getDeadline()));

    if (taskDto.getId() != null) {
      builder.id(taskDto.getId());
    }
    return builder.build();
  }
}
