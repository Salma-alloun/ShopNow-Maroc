package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.dto.PaymentDTO;
public interface PaymentService {
    // Créer le PaymentIntent Stripe et sauvegarder en BDD
    String createPaymentIntent(int orderId, int userId) throws Exception;
    // Confirmer le paiement après succès Stripe
    PaymentDTO confirmPayment(int orderId, String paymentIntentId) throws Exception;
}