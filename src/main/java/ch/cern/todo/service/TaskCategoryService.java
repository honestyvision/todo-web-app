package ch.cern.todo.service;

import ch.cern.todo.dtos.TaskCategoryDto;
import ch.cern.todo.entities.TaskCategory;
import ch.cern.todo.repository.TaskCategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskCategoryService {

  private TaskCategoryRepository taskCategoryRepository;

  public List<TaskCategoryDto> getCategories() {
    List<TaskCategoryDto> categories = new ArrayList<>();
    taskCategoryRepository.findAll().forEach(category -> {
      categories.add(TaskCategoryDto.from(category));
    });
    return categories;
  }

  public TaskCategoryDto getCategoryById(Integer id) {
    Optional<TaskCategory> category = taskCategoryRepository.findById(id);
    if (category.isPresent())
      return TaskCategoryDto.from(category.get());
    return null;
  }

  public TaskCategoryService(TaskCategoryRepository taskCategoryRepository) {
    this.taskCategoryRepository = taskCategoryRepository;
  }

  public TaskCategoryDto saveCategory(TaskCategoryDto taskCategoryDto) {
    TaskCategory newCategory = TaskCategory.from(taskCategoryDto);
    return TaskCategoryDto.from(taskCategoryRepository.save(newCategory));
  }

  public void deleteCategory(Integer id) {
    taskCategoryRepository.deleteById(id);
  }

}
