package repository.impl;

import entity.PaymentCard;
import repository.PaymentCardRepository;

public class PaymentCardRepositoryImpl extends BaseRepositoryImpl<PaymentCard> implements PaymentCardRepository {

    public PaymentCardRepositoryImpl() {
        super(PaymentCard.class);
    }
}

