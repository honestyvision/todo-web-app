package ch.cern.todo;

import static org.assertj.core.api.Assertions.assertThat;

import ch.cern.todo.dtos.TaskDto;
import ch.cern.todo.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment= WebEnvironment.RANDOM_PORT)
class TaskResourceTests {

	@Autowired
	private TaskRepository repository;

	@LocalServerPort
	int randomServerPort;

	private TestRestTemplate testRestTemplate;
	private RestTemplate patchRestTemplate;
	final String baseUrl = "http://localhost";

	@BeforeEach
	public void setUp() {
		testRestTemplate = new TestRestTemplate();
		patchRestTemplate = testRestTemplate.getRestTemplate();

		HttpClient httpClient = HttpClientBuilder.create().build();
		patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void get_tasks_should_return_all_tasks() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);
		createTask(payload);
		List<TaskDto> expectedResult =
				List.of(
						buildTaskDto(
								1, "test", "This is a test", 1, "2023-10-01T00:00"),
						buildTaskDto(
								2, "test", "This is a test", 1, "2023-10-01T00:00")
				);

		ResponseEntity<TaskDto[]> response = getTasks();
		TaskDto[] tasks = response.getBody();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(repository.count()).isEqualTo(2);
		for (int i = 0; i< tasks.length; i++) {
			assertThat(tasks[i]).usingRecursiveComparison().isEqualTo(expectedResult.get(i));
		}
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void get_task_should_return_task_when_task_exists() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);
		TaskDto expectedResult = buildTaskDto(
				1,
				"test",
				"This is a test",
				1,
				"2023-10-01T00:00"
		);

		ResponseEntity<TaskDto> response = getTaskById(1);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void get_task_should_return_error_when_task_does_not_exist() throws Exception {
		ResponseEntity<TaskDto> response = getTaskById(1);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_task_should_create_task_when_data_input_is_valid() throws Exception {
		JsonNode payload = getPayload("valid-task");
		TaskDto expectedResult = buildTaskDto(
				1,
				"test",
				"This is a test",
				1,
				"2023-10-01T00:00"
		);

		ResponseEntity<TaskDto> response = createTask(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_task_should_create_task_when_optional_data_is_missing() throws Exception {
		JsonNode payload = getPayload("valid-task-no-description");
		TaskDto expectedResult = TaskDto.builder()
				.id(1)
				.name("test no description")
				.categoryId(2)
				.deadline("2023-10-12T00:00")
				.build();

		ResponseEntity<TaskDto> response = createTask(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_task_should_return_error_when_mandatory_data_is_missing() throws Exception {
		JsonNode payload = getPayload("invalid-task-no-name");

		ResponseEntity<TaskDto> response = createTask(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(0);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_task_should_return_error_when_given_data_is_too_long() throws Exception {
		JsonNode payload = getPayload("invalid-task-name-too-long");

		ResponseEntity<TaskDto> response = createTask(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(0);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void update_task_should_update_task_when_data_input_is_valid() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);

		JsonNode updatePayload = getPayload("valid-task-update");
		TaskDto expectedResult = buildTaskDto(
				1,
				"test updated",
				"This is a test updated",
				2,
				"2023-10-13T10:00"
		);

		ResponseEntity<TaskDto> response = updateTask(updatePayload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void update_task_should_update_task_when_optional_data_is_missing() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);

		JsonNode updatePayload = getPayload("valid-task-update-no-description");
		TaskDto expectedResult = TaskDto.builder()
				.id(1)
				.name("test updated no description")
				.categoryId(2)
				.deadline("2023-10-13T10:00")
				.build();

		ResponseEntity<TaskDto> response = updateTask(updatePayload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void update_task_should_return_error_when_mandatory_data_is_missing() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);

		JsonNode updatedPayload = getPayload("invalid-task-update-no-name");

		ResponseEntity<TaskDto> response = updateTask(updatedPayload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void delete_task_should_delete_task_when_task_exists() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);

		ResponseEntity response = deleteTask(1);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(repository.count()).isEqualTo(0);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void delete_task_should_return_error_when_task_with_given_id_does_not_exist() throws Exception {
		JsonNode payload = getPayload("valid-task");
		createTask(payload);

		ResponseEntity response = deleteTask(15);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(repository.count()).isEqualTo(1);
	}

	private URI getUri(String endpoint) throws URISyntaxException {
		return new URI(baseUrl + ":" + randomServerPort + "/tasks/" + endpoint);
	}

	private JsonNode getPayload(String fileName) throws IOException {
		final File file = new ClassPathResource("payloads/tasks/" + fileName + ".json").getFile();
		final String fileContent = Files.readString(file.toPath());
		return new ObjectMapper().readTree(fileContent);
	}

	private TaskDto buildTaskDto(int id, String name, String description, int categoryId, String deadline) {
		return TaskDto.builder()
				.id(id)
				.name(name)
				.description(description)
				.categoryId(categoryId)
				.deadline(deadline)
				.build();
	}

	private ResponseEntity<TaskDto[]> getTasks() throws Exception {
		URI uri = getUri("");
		return testRestTemplate.getForEntity(uri, TaskDto[].class);
	}

	private ResponseEntity<TaskDto> getTaskById(int id) throws Exception {
		URI uri = getUri("" + id);
		return testRestTemplate.getForEntity(uri, TaskDto.class);
	}

	private ResponseEntity<TaskDto> createTask(JsonNode payload) throws Exception {
		URI uri = getUri("create");
		HttpEntity<JsonNode> request = new HttpEntity<>(payload);
		return testRestTemplate.postForEntity(uri, request, TaskDto.class);
	}

	private ResponseEntity<TaskDto> updateTask(JsonNode payload) throws Exception {
		URI uri = getUri("update");
		HttpEntity<JsonNode> request = new HttpEntity<>(payload);
		return patchRestTemplate.exchange(uri, HttpMethod.PATCH, request, TaskDto.class);
	}

	private ResponseEntity deleteTask(Integer id) throws Exception {
		URI uri = getUri("delete/"  + id);
		return testRestTemplate.exchange(uri, HttpMethod.DELETE, HttpEntity.EMPTY, void.class);
	}
}
