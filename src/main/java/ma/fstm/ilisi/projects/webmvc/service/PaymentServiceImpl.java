package ma.fstm.ilisi.projects.webmvc.service;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import ma.fstm.ilisi.projects.webmvc.bo.Order;
import ma.fstm.ilisi.projects.webmvc.bo.Payment;
import ma.fstm.ilisi.projects.webmvc.dto.PaymentDTO;
import ma.fstm.ilisi.projects.webmvc.repository.OrderRepository;
import ma.fstm.ilisi.projects.webmvc.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ma.fstm.ilisi.projects.webmvc.AppConfig;
import java.util.Date;
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
   
	private String stripeSecretKey = AppConfig.STRIPE_SECRET_KEY;
    @Autowired private OrderRepository   orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderService      orderService;
    @Override
    public String createPaymentIntent(int orderId, int userId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        // LOG TEMPORAIRE
        System.out.println("=== STRIPE DEBUG ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("User ID commande: " + order.getUser().getId());
        System.out.println("User ID session: " + userId);
        System.out.println("Total: " + order.getTotal());
        System.out.println("Stripe Key: " + stripeSecretKey.substring(0, 10) + "...");
        System.out.println("===================");
        
        
        // Sécurité : la commande appartient bien au user
        if (order.getUser().getId() != userId) {
            throw new RuntimeException("Accès refusé");
        }
        Stripe.apiKey = stripeSecretKey;
        // Montant en centimes
        long amountInCentimes = (long) (order.getTotal() * 100);
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCentimes)
                .setCurrency("eur")
                .setDescription("Commande #" + orderId + " - E-Shop Maroc")
                .putMetadata("orderId", String.valueOf(orderId))
                .putMetadata("userId",  String.valueOf(userId))
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        // Sauvegarder le Payment en BDD avec statut PENDING
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(new Payment());
        payment.setOrder(order);
        payment.setStripePaymentIntentId(intent.getId());
        payment.setAmount(order.getTotal());
        payment.setCurrency("EUR");
        payment.setStatus("PENDING");
        payment.setPaymentMethod("CARD");
        payment.setPaymentDate(new Date());
        paymentRepository.save(payment);
        // Retourner le clientSecret pour le JS Stripe
        return intent.getClientSecret();
    }
    @Override
    public PaymentDTO confirmPayment(int orderId, String paymentIntentId) throws Exception {
        Stripe.apiKey = stripeSecretKey;
        // Vérifier le statut côté Stripe
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        if (!"succeeded".equals(intent.getStatus())) {
            throw new RuntimeException("Paiement non abouti : " + intent.getStatus());
        }
        // Mettre à jour Payment en BDD
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
        payment.setStatus("SUCCEEDED");
        payment.setPaymentDate(new Date());
        paymentRepository.save(payment);
        // Mettre à jour la commande → PAID
        orderService.updateStatus(orderId, "PAID");
        return convertToDTO(payment);
    }
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        if (payment.getOrder() != null) {
            dto.setOrderId(payment.getOrder().getId());
        }
        return dto;
    }
}