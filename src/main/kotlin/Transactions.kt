import kotlin.reflect.full.memberProperties

interface Transaction {
    val type: String
    val idFrom: Int

    fun testValidId(bank: Bank) {
        this::class.memberProperties
            .filter { it.name.contains("Id", ignoreCase = true) }
            .forEach { property ->
                try {
                    val idValue = property.call(this) as? Int

                    if (idValue != null && !bank.clients.containsKey(idValue)) {
                        throw UserNotFoundException("User with id $idValue does not exist")
                    }
                } catch (e: Exception) {
                    println("Error accessing Id ${property.name}")
                    return
                }
            }
    }
}

data class DepositTransaction(
    override val type: String = "deposit",
    override val idFrom: Int,
    val currency: String,
    val amount: Double
) : Transaction

data class WithdrawTransaction(
    override val type: String = "withdraw",
    override val idFrom: Int,
    val currency: String,
    val amount: Double
) : Transaction

data class ExchangeTransaction(
    override val type: String = "exchange",
    override val idFrom: Int,
    val fromCurrency: String,
    val toCurrency: String,
    val amountTo: Double
) : Transaction

data class TransferTransaction(
    override val type: String = "transfer",
    override val idFrom: Int,
    val idTo: Int,
    val currency: String,
    val amount: Double
) : Transaction

data class PrintTransaction(
    override val type: String = "print",
    override val idFrom: Int,
) : Transaction