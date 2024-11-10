import kotlin.system.exitProcess

fun main() {
    val bank = Bank(2);

    bank.addClient(2)
    val initialBalance = arrayListOf(Money("RUB", 100.0))
    bank.addClient(3, initialBalance)

    bank.exchangeRates["EUR"] = 80.0

    bank.clients[2]!!.requestDeposit("RUB", 1000.0)
    bank.clients[2]!!.requestPrintBalance()
    bank.clients[2]!!.requestExchange("RUB", "EUR", 1.0)

    bank.clients[2]!!.requestPrintBalance()


    bank.clients[3]!!.requestDeposit("EUR", 1000.0)
    bank.clients[3]!!.requestExchange("RUB", "EUR", 10000.0)

    Thread.sleep(6000)

    exitProcess(0)
}