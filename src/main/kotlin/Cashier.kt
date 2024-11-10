class Cashier(private val bank: Bank) : Thread() {
    override fun run() {
        while (true) {
            val transaction = bank.transactionQueue.take()
            try {
                when (transaction) {
                    is DepositTransaction -> deposit(transaction)
                    is WithdrawTransaction -> withdraw(transaction)
                    is ExchangeTransaction -> exchangeCurrency(transaction)
                    is TransferTransaction -> transferFunds(transaction)
                    is PrintTransaction -> PrintTransaction(transaction)
                    else -> throw UnsupportedOperationException("Unsupported transaction type: ${transaction.type}")
                }
                bank.loggerTransactions.update(transaction.toString())
                LoggerUser.getInstance(transaction.idFrom).update("Success: $transaction")
            }
            catch (e: Exception){
                println("transaction error: $e")
            }
        }
    }

    //-------------------- Process the commands -------------------------

    private fun deposit(deposit: DepositTransaction) {
        val client = bank.clients[deposit.idFrom]!!
        synchronized(client) {
            val balance = getClientMoney(deposit.idFrom, deposit.currency)
            balance.amount += deposit.amount
        }
    }

    private fun withdraw(withdraw: WithdrawTransaction) {
        val client = bank.clients[withdraw.idFrom]!!
        synchronized(client) {
            val balance = getClientMoney(withdraw.idFrom, withdraw.currency)
            if (balance.amount >= withdraw.amount) {
                balance.amount -= withdraw.amount
            } else {
                throw InsufficientFundsException("Insufficient funds for withdrawal for client ${withdraw.idFrom}")
            }
        }
    }

    private fun exchangeCurrency(exchange: ExchangeTransaction) {
        val client = bank.clients[exchange.idFrom]!!
        synchronized(client) {
            val fromBalance = getClientMoney(exchange.idFrom, exchange.fromCurrency)
            val toBalance = getClientMoney(exchange.idFrom, exchange.toCurrency)
            val fromBalanceToRub: Double = rateToRub(exchange.fromCurrency)
            val toBalanceToRub = rateToRub(exchange.toCurrency)
            val neededAmount = exchange.amountTo * toBalanceToRub / fromBalanceToRub
            if (fromBalance.amount >= neededAmount) {
                fromBalance.amount -= neededAmount
                toBalance.amount += exchange.amountTo
            } else {
                throw InsufficientFundsException("Insufficient funds for currency exchange for client ${exchange.idFrom}")
            }
        }
    }

    private fun transferFunds(transfer: TransferTransaction) {
        val senderClient = bank.clients[transfer.idFrom]!!
        val receiverClient = bank.clients[transfer.idTo]!!
        synchronized(senderClient) {
            synchronized(receiverClient) {
                val senderBalance = getClientMoney(transfer.idFrom, transfer.currency)
                val receiverBalance = getClientMoney(transfer.idTo, transfer.currency)
                if (senderBalance.amount >= transfer.amount) {
                    senderBalance.amount -= transfer.amount
                    receiverBalance.amount += transfer.amount
                } else {
                    throw InsufficientFundsException("Insufficient funds for transfer from client ${transfer.idFrom} to client ${transfer.idTo}")
                }
            }
        }
    }

    private fun PrintTransaction(show: PrintTransaction) {
        val client = bank.clients[show.idFrom]!!
        synchronized(client) {
            println("${show.idFrom} balance: " + getClientMoney(show.idFrom).joinToString(", "))
        }
    }


    //-------------------- Utility -------------------------

    private fun rateToRub(currency: String): Double {
        if(currency == "RUB"){
            return 1.0
        }
        return bank.exchangeRates[currency] ?: throw CurrencyNotFoundException("Exchange rate for $currency not found")
    }

    private fun getClientMoney(clientId: Int, currency: String): Money {
        val client = bank.clients[clientId]
        checkNotNull(client) { throw UserNotFoundException("User with id $clientId does not exists") }
        return client.balance.find { it.currency == currency } ?:
        Money(currency, 0.0).also { client.balance.add(it) }
    }

    private fun getClientMoney(clientId: Int): ArrayList<Money> {
        val client = bank.clients[clientId]
        checkNotNull(client) { throw UserNotFoundException("User with id $clientId does not exist") }

        synchronized(client) {
            return ArrayList(client.balance)
        }
    }
}
