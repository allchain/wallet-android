package com.mycelium.wapi.wallet.eth;

import com.mycelium.wapi.wallet.SendRequest;
import com.mycelium.wapi.wallet.coins.CryptoCurrency;
import com.mycelium.wapi.wallet.coins.Value;

public class EthSendRequest extends SendRequest<EthTransaction> {
    private Value amount;

    private EthAddress destination;

    public EthSendRequest(CryptoCurrency type, EthAddress destination, Value amount) {
        super(type);

        this.destination = destination;
        this.amount = amount;
    }

    public static EthSendRequest to(EthAddress destination, Value amount) {
        EthSendRequest req = new EthSendRequest(destination.getCoinType(), destination, amount);
        return req;
    }

    public Value getAmount() {
        return amount;
    }

    public EthAddress getDestination() {
        return destination;
    }

}