package service;

import model.PaymentCard;
import repository.PaymentCardRepository;
import repository.impl.PaymentCardRepositoryImpl;

public class PaymentCardService extends BaseService<PaymentCard> {

    private final PaymentCardRepository paymentCardRepository;

    public PaymentCardService() {
        this(new PaymentCardRepositoryImpl());
    }

    public PaymentCardService(PaymentCardRepository paymentCardRepository) {
        super(paymentCardRepository);
        this.paymentCardRepository = paymentCardRepository;
    }
}