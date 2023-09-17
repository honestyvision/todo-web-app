package ch.cern.todo.repository;

import ch.cern.todo.entities.TaskCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCategoryRepository extends CrudRepository<TaskCategory, Integer> { }
