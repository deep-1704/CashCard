package example.CashCard.Repository;

import example.CashCard.Entities.CashCard;
import org.springframework.data.repository.CrudRepository;
public interface CashCardRepository extends CrudRepository<CashCard, Long> { }
