data class Client(var bank: Bank, val id: Int, var balance: ArrayList<Money>) : Observer {

    fun requestDeposit(currency: String, amount: Double) {
        val transaction = DepositTransaction(idFrom = id, currency = currency, amount = amount)
        bank.addTransaction(transaction)
    }

    fun requestWithdraw(currency: String, amount: Double) {
        val transaction = WithdrawTransaction(idFrom = id, currency = currency, amount = amount)
        bank.addTransaction(transaction)
    }

    fun requestExchange(fromCurrency: String, toCurrency: String, amountTo: Double) {
        val transaction =
            ExchangeTransaction(idFrom = id, fromCurrency = fromCurrency, toCurrency = toCurrency, amountTo = amountTo)
        bank.addTransaction(transaction)
    }

    fun requestTransfer(idTo: Int, currency: String, amount: Double) {
        val transaction = TransferTransaction(idFrom = id, idTo = idTo, currency = currency, amount = amount)
        bank.addTransaction(transaction)
    }

    fun requestPrintBalance() {
        val transaction = PrintTransaction(idFrom = id)
        bank.addTransaction(transaction)
    }

    override fun update(message: String) {
        LoggerUser.getInstance(id).update(message)
    }
}
