import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BankTest {
    private lateinit var bank: Bank

    @BeforeEach
    fun setUp() {
        bank = Bank(3)
    }

    @Test
    fun `test add client`() {
        val initialBalance = arrayListOf(Money("USD", 100.0))
        bank.addClient(1, initialBalance)

        assertTrue(bank.clients.containsKey(1))
        assertEquals(initialBalance, bank.clients[1]?.balance)
    }

    @Test
    fun `test exchange rate initialization`() {
        bank.addCurrency("USD", 70.0)
        assertEquals(70.0, bank.exchangeRates["USD"])
    }

    @Test
    fun `test exchange rate update`() {
        bank.exchangeRates["USD"] = 70.0
        Thread.sleep(6000)
        assertNotEquals(
            70.0,
            bank.exchangeRates["USD"],
            "Ошибка: курс не изменился. Возможно, вы изменили initial delay в exchanger?"
        )
    }

    @Test
    fun `test cashiers initialization`() {
        assertEquals(3, bank.cashiers.size) // проверка, что созданы 3 кассира
    }

    @Test
    fun `test client observer added`() {
        val initialBalance = arrayListOf(Money("USD", 100.0))
        bank.addClient(1, initialBalance)

        assertTrue(bank.clients[1] != null) // клиент добавлен в наблюдатели
    }

    @Test
    fun `test add client log`() {
        val initialBalance = arrayListOf(Money("USD", 100.0))
        for (i in 2..6) {
            bank.addClient(i, initialBalance)
        }

        assertTrue(bank.clients.size == 5)
    }

    @Test
    fun `test exchange`() {
        val initialBalance = arrayListOf(Money("RUB", 100.0))
        bank.addClient(2, initialBalance)
        bank.exchangeRates["EUR"] = 80.0
        bank.clients[2]!!.requestExchange("RUB", "EUR", 1.0)

        Thread.sleep(100)

        assertTrue(bank.clients[2]!!.balance.contains(Money("EUR", 1.0)))
    }

    @Test
    fun `test illegal transfer`() {
        val initialBalance = arrayListOf(Money("RUB", 100.0))
        bank.addClient(3, initialBalance)
        bank.exchangeRates["EUR"] = 80.0
        bank.clients[3]!!.requestExchange("RUB", "EUR", 10000.0)

        Thread.sleep(100)

        assertTrue(bank.clients[3]?.balance?.find { it.currency == "EUR" }!!.amount == 0.0)
    }

    @Test
    fun `test deposit`() {
        bank.addClient(2)
        bank.exchangeRates["EUR"] = 80.0
        bank.clients[2]!!.requestDeposit("RUB", 1000.0)

        Thread.sleep(100)

        assertTrue(bank.clients[2]?.balance?.find { it.currency == "RUB" }!!.amount == 1000.0)
    }
}
