package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.dto.payment.CreditPackage;
import com.paysecure.ai_report_tool_backend.dto.payment.PaymentRequest;
import com.paysecure.ai_report_tool_backend.dto.payment.PaymentResponse;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.Payment;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.enums.PaymentStatus;
import com.paysecure.ai_report_tool_backend.model.enums.TransactionType;
import com.paysecure.ai_report_tool_backend.repository.PaymentRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    @Value("${paysecure.api-key:}")
    private String paysecureApiKey;

    @Value("${paysecure.base-url:https://api.paysecure.net}")
    private String paysecureBaseUrl;

    private final PaymentRepository paymentRepository;
    private final CreditService creditService;
    private final OkHttpClient httpClient;
    private final Gson gson;

    // Available credit packages
    private static final List<CreditPackage> CREDIT_PACKAGES = List.of(
            new CreditPackage("starter", "Starter Pack", 10, BigDecimal.valueOf(9.99), "Perfect for trying out"),
            new CreditPackage("standard", "Standard Pack", 50, BigDecimal.valueOf(39.99), "Most popular choice"),
            new CreditPackage("professional", "Professional Pack", 100, BigDecimal.valueOf(69.99), "Best value"),
            new CreditPackage("enterprise", "Enterprise Pack", 500, BigDecimal.valueOf(299.99), "For heavy users")
    );

    public PaymentService(
            PaymentRepository paymentRepository,
            CreditService creditService
    ) {
        this.paymentRepository = paymentRepository;
        this.creditService = creditService;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public List<CreditPackage> getCreditPackages() {
        return CREDIT_PACKAGES;
    }

    public CreditPackage getPackage(String packageId) {
        return CREDIT_PACKAGES.stream()
                .filter(p -> p.id().equals(packageId))
                .findFirst()
                .orElseThrow(() -> new ApiException("Package not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public PaymentResponse initiatePurchase(User user, PaymentRequest request) {
        CreditPackage pkg = getPackage(request.packageId());

        // Create payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(pkg.price());
        payment.setCurrency("USD");
        payment.setCreditsGranted(pkg.credits());
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // If Paysecure is configured, call their API
        if (isPaysecureConfigured()) {
            try {
                String paymentUrl = createPaysecureTransaction(payment, pkg);
                return new PaymentResponse(
                        payment.getId(),
                        "pending",
                        paymentUrl,
                        null
                );
            } catch (Exception e) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new ApiException("Payment initiation failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        // Demo mode: auto-complete the payment
        return completePurchaseDemo(payment);
    }

    @Transactional
    public PaymentResponse confirmPayment(String paymentId, String externalPaymentId) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new ApiException("Payment not found", HttpStatus.NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApiException("Payment already processed", HttpStatus.BAD_REQUEST);
        }

        payment.setExternalPaymentId(externalPaymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(Instant.now());
        paymentRepository.save(payment);

        // Grant credits
        creditService.addCredits(
                payment.getUser(),
                payment.getCreditsGranted(),
                TransactionType.PURCHASE,
                payment.getId().toString(),
                "Purchased " + payment.getCreditsGranted() + " credits"
        );

        return new PaymentResponse(
                payment.getId(),
                "completed",
                null,
                "Successfully purchased " + payment.getCreditsGranted() + " credits"
        );
    }

    public List<Payment> getUserPayments(User user) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    private boolean isPaysecureConfigured() {
        return paysecureApiKey != null && !paysecureApiKey.isBlank();
    }

    private String createPaysecureTransaction(Payment payment, CreditPackage pkg) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("amount", pkg.price().doubleValue());
        requestBody.addProperty("currency", "USD");
        requestBody.addProperty("reference_id", payment.getId().toString());
        requestBody.addProperty("description", "Purchase " + pkg.credits() + " credits - " + pkg.name());

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(paysecureBaseUrl + "/transactions")
                .addHeader("Authorization", "Bearer " + paysecureApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Paysecure API error: " + response.code());
            }

            JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
            payment.setExternalPaymentId(jsonResponse.get("transaction_id").getAsString());
            payment.setResponseJson(jsonResponse.toString());
            paymentRepository.save(payment);

            return jsonResponse.get("payment_url").getAsString();
        }
    }

    // Demo mode - auto-complete payment without real payment provider
    private PaymentResponse completePurchaseDemo(Payment payment) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(Instant.now());
        payment.setExternalPaymentId("DEMO-" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(payment);

        // Grant credits
        creditService.addCredits(
                payment.getUser(),
                payment.getCreditsGranted(),
                TransactionType.PURCHASE,
                payment.getId().toString(),
                "Purchased " + payment.getCreditsGranted() + " credits (Demo Mode)"
        );

        return new PaymentResponse(
                payment.getId(),
                "completed",
                null,
                "Successfully purchased " + payment.getCreditsGranted() + " credits"
        );
    }
}
