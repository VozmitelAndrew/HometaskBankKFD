import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class Bank(amountOfCashiers: Int) {
    fun addClient(id: Int, initialBalance: ArrayList<Money> = arrayListOf(Money("RUB", 0.0))) {
        clients[id] = Client(this, id, initialBalance)
        loggerTransactions.update("Client $id added with initial balance: $initialBalance")
        addObserver(clients[id]!!)
    }

    private fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    private fun notifyObservers(message: String) {
        loggerExchange.update(message)
        observers.forEach {
            it.update(message)
        }
    }

    fun addCurrency(name: String, value: Double) {
        exchangeRates[name] = value
    }

    fun addTransaction(transaction: Transaction) {
        transactionQueue.put(transaction)
        //loggerTransactions.update("Transaction added: $transaction")
    }

    private fun getRandomExchangeRate(deviation: Int): Double {
        return ((100 - deviation..100 + deviation).random() / 100.0)
    }


    private val loggerExchange = LoggerExchange()
    val loggerTransactions = LoggerTransactions()

    //все курсы обмена по виду - сколько 1 единица стоит в рублях
    internal var exchangeRates = ConcurrentHashMap<String, Double>()

    internal var clients = ConcurrentHashMap<Int, Client>()
    internal var cashiers = ArrayList<Cashier>()
    internal var transactionQueue = LinkedBlockingQueue<Transaction>()
    private var observers = mutableListOf<Observer>()

    init {
        repeat(amountOfCashiers) {
            cashiers.add(Cashier(this))
        }

        cashiers.forEach { it.start() }

        val executor = ScheduledThreadPoolExecutor(1)
        executor.scheduleAtFixedRate({
            exchangeRates.keys.forEach { currency ->
                val oldRate = exchangeRates[currency] ?: 0.0
                val newRate = oldRate * getRandomExchangeRate(10)
                    .toBigDecimal()
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
                exchangeRates[currency] = newRate
                //так можно, но не нужно (мне кажется что логи становятся слишком грязными)
                notifyObservers("Currency rate changed for $currency: $oldRate -> $newRate")

                //немного лучше так
                //loggerExchange.update("Currency rate changed for $currency: $oldRate -> $newRate")
            }
        }, 5, 15, TimeUnit.SECONDS)
    }
}