package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Conversation;
import vn.edu.iuh.fit.bookshop_be.models.Customer;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    @Override
    Optional<Conversation> findById(Integer integer);

    @Query("select c from Conversation c where c.customer = ?1")
    Conversation findByCustomer(Customer customer);

}
