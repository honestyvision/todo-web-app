package ch.cern.todo.resource;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import ch.cern.todo.dtos.TaskCategoryDto;
import ch.cern.todo.service.TaskCategoryService;
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
@RequestMapping("/categories")
public class TaskCategoryResource {

  private TaskCategoryService taskCategoryService;

  public TaskCategoryResource(TaskCategoryService taskCategoryService) {
    this.taskCategoryService = taskCategoryService;
  }

  @GetMapping("/")
  public ResponseEntity getCategories() {
    try {
      List<TaskCategoryDto> categories = taskCategoryService.getCategories();
      return ResponseEntity.ok(categories);
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{categoryId}")
  public ResponseEntity getCategoryById(@PathVariable int categoryId) {
    try {
      TaskCategoryDto category = taskCategoryService.getCategoryById(categoryId);

      if (category == null) return ResponseEntity.notFound().build();
      return ResponseEntity.ok(category);
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/create")
  public ResponseEntity createCategory(@RequestBody TaskCategoryDto taskCategoryDto) {
    try {
      TaskCategoryDto createdCategory = taskCategoryService.saveCategory(taskCategoryDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    } catch(DataIntegrityViolationException exception) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @PatchMapping("/update")
  public ResponseEntity updateCategory(@RequestBody TaskCategoryDto taskCategoryDto) {
    try {
      TaskCategoryDto createdCategory = taskCategoryService.saveCategory(taskCategoryDto);
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdCategory);

    } catch(DataIntegrityViolationException exception) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/delete/{categoryId}")
  public ResponseEntity deleteCategory(@PathVariable Integer categoryId) {
    try {
      taskCategoryService.deleteCategory(categoryId);
      return ResponseEntity.ok().build();

    } catch(EmptyResultDataAccessException exception) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    catch (final Exception exception) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }
  }
}
