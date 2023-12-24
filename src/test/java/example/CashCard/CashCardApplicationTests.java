package example.CashCard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import example.CashCard.Entities.CashCard;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test // Get end-point
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("deep1","abc123").getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());

		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);
	}

	@Test // Post end-point
	@DirtiesContext
	void shouldCreateANewCashCard(){
		CashCard newCashCard = new CashCard(null,250.00,null);
		ResponseEntity<Void> response = restTemplate.withBasicAuth("deep1","abc123").postForEntity("/cashcards",newCashCard,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = response.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("deep1","abc123").getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isNotNull();
		Number amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	void shouldReturnAllCasCardsWhenListIsRequested(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("deep1","abc123").getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99,100,101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45,1.00,150.00);
	}

	@Test
	void shouldReturnAPageOfCashCards(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("deep1","abc123").getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.00);
	}

	@Test
	void shouldReturnAPageOfCashCardWithDefaultParameters(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("deep1","abc123").getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(1.00, 123.45, 150.00);

	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD-USER", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth("sarah1", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hank-owns-no-cards","qrs456")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn(){
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("deep1", "abc123")
				.getForEntity("/cashcards/102", String.class); // kumar2's data
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard(){
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("deep1", "abc123")
				.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> updatedResponse = restTemplate
				.withBasicAuth("deep1","abc123")
				.getForEntity("/cashcards/99",String.class);
		assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(updatedResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	void shouldNotUpdateACashCardThatDoesNotExist(){
		CashCard unknownCard = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("deep1", "abc123")
				.exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateACashCardThatIsOwnedBySomeOneElse(){
		CashCard kumarsCard = new CashCard(null,33.33,null);
		HttpEntity<CashCard> request = new HttpEntity<>(kumarsCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("deep1", "abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}


}











