package ch.cern.todo;

import static org.assertj.core.api.Assertions.assertThat;

import ch.cern.todo.dtos.TaskCategoryDto;
import ch.cern.todo.repository.TaskCategoryRepository;
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
class TaskCategoryResourceTests {

	@Autowired
	private TaskCategoryRepository repository;

	@LocalServerPort
	int randomServerPort;

	private TestRestTemplate testRestTemplate;
	private RestTemplate patchRestTemplate;
	private final String baseUrl = "http://localhost";
	private final int DEFAULT_CATEGORIES_COUNT = 3;

	@BeforeEach
	public void setUp() {
		testRestTemplate = new TestRestTemplate();
		patchRestTemplate = testRestTemplate.getRestTemplate();

		HttpClient httpClient = HttpClientBuilder.create().build();
		patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void get_categories_should_return_all_categories() throws Exception {
		List<TaskCategoryDto> expectedResult =
				List.of(
						TaskCategoryDto.builder()
								.categoryId(1)
								.name("Work")
								.description("Tasks related to work")
								.build(),
						TaskCategoryDto.builder()
								.categoryId(2)
								.name("Home")
								.description("Tasks related to the house")
								.build(),
						TaskCategoryDto.builder()
								.categoryId(3)
								.name("Other")
								.description("Other tasks")
								.build()
				);

		ResponseEntity<TaskCategoryDto[]> response = getCategories();
		TaskCategoryDto[] categories = response.getBody();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(repository.count()).isEqualTo(3);
		for (int i = 0; i< categories.length; i++) {
			assertThat(categories[i]).usingRecursiveComparison().isEqualTo(expectedResult.get(i));
		}
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void get_category_should_return_category_when_category_exists() throws Exception {
		TaskCategoryDto expectedResult = TaskCategoryDto.builder()
				.categoryId(1)
				.name("Work")
				.description("Tasks related to work")
				.build();

		ResponseEntity<TaskCategoryDto> response = getCategoryById(1);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void get_task_should_return_error_when_task_does_not_exist() throws Exception {
		ResponseEntity<TaskCategoryDto> response = getCategoryById(15);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_category_should_create_category_when_data_input_is_valid() throws Exception {
		JsonNode payload = getPayload("valid-category");
		TaskCategoryDto expectedResult = TaskCategoryDto.builder()
				.categoryId(4)
				.name("category")
				.description("This is a category")
				.build();

		ResponseEntity<TaskCategoryDto> response = createCategory(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT + 1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_category_should_create_category_when_optional_data_is_missing() throws Exception {
		JsonNode payload = getPayload("valid-category-no-description");
		TaskCategoryDto expectedResult = TaskCategoryDto.builder()
				.categoryId(4)
				.name("category")
				.build();

		ResponseEntity<TaskCategoryDto> response = createCategory(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT + 1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_category_should_return_error_when_mandatory_data_is_missing() throws Exception {
		JsonNode payload = getPayload("invalid-category-no-name");

		ResponseEntity<TaskCategoryDto> response = createCategory(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_category_should_return_error_when_data_is_too_long() throws Exception {
		JsonNode payload = getPayload("invalid-category-name-too-long");

		ResponseEntity<TaskCategoryDto> response = createCategory(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void create_category_should_return_error_when_category_exists_already() throws Exception {
		JsonNode payload = getPayload("valid-category");

		createCategory(payload);
		ResponseEntity<TaskCategoryDto> response = createCategory(payload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT + 1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void update_category_should_update_category_when_data_input_is_valid() throws Exception {
		JsonNode updatePayload = getPayload("valid-category-update");
		TaskCategoryDto expectedResult = TaskCategoryDto.builder()
				.categoryId(1)
				.name("category")
				.description("This is a category updated")
				.build();

		ResponseEntity<TaskCategoryDto> response = updateCategory(updatePayload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void update_category_should_update_category_when_optional_data_is_missing() throws Exception {
		JsonNode updatePayload = getPayload("valid-category-update-no-description");
		TaskCategoryDto expectedResult = TaskCategoryDto.builder()
				.categoryId(1)
				.name("category")
				.build();

		ResponseEntity<TaskCategoryDto> response = updateCategory(updatePayload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectedResult);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void update_category_should_return_error_when_mandatory_data_is_missing() throws Exception {
		JsonNode updatedPayload = getPayload("invalid-category-update-no-name");

		ResponseEntity<TaskCategoryDto> response = updateCategory(updatedPayload);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void delete_category_should_delete_category_when_category_exists() throws Exception {
		ResponseEntity response = deleteCategory(1);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT -1);
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	void delete_category_should_return_error_when_category_with_given_id_does_not_exist() throws Exception {
		ResponseEntity response = deleteCategory(15);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(repository.count()).isEqualTo(DEFAULT_CATEGORIES_COUNT);
	}

	private URI getUri(String endpoint) throws URISyntaxException {
		return new URI(baseUrl + ":" + randomServerPort + "/categories/" + endpoint);
	}

	private JsonNode getPayload(String fileName) throws IOException {
		final File file = new ClassPathResource("payloads/categories/" + fileName + ".json").getFile();
		final String fileContent = Files.readString(file.toPath());
		return new ObjectMapper().readTree(fileContent);
	}

	private ResponseEntity<TaskCategoryDto[]> getCategories() throws Exception {
		URI uri = getUri("");
		return testRestTemplate.getForEntity(uri, TaskCategoryDto[].class);
	}

	private ResponseEntity<TaskCategoryDto> getCategoryById(int id) throws Exception {
		URI uri = getUri("" + id);
		return testRestTemplate.getForEntity(uri, TaskCategoryDto.class);
	}

	private ResponseEntity<TaskCategoryDto> createCategory(JsonNode payload) throws Exception {
		URI uri = getUri("create");
		HttpEntity<JsonNode> request = new HttpEntity<>(payload);
		return testRestTemplate.postForEntity(uri, request, TaskCategoryDto.class);
	}

	private ResponseEntity<TaskCategoryDto> updateCategory(JsonNode payload) throws Exception {
		URI uri = getUri("update");
		HttpEntity<JsonNode> request = new HttpEntity<>(payload);
		return patchRestTemplate.exchange(uri, HttpMethod.PATCH, request, TaskCategoryDto.class);
	}

	private ResponseEntity deleteCategory(Integer id) throws Exception {
		URI uri = getUri("delete/"  + id);
		return testRestTemplate.exchange(uri, HttpMethod.DELETE, HttpEntity.EMPTY, void.class);
	}
}
