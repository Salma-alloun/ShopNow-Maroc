package ma.fstm.ilisi.projects.webmvc.repository;
import ma.fstm.ilisi.projects.webmvc.bo.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrderId(int orderId);
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
}