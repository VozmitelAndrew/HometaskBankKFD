data class Money(var currency: String, var amount: Double){
    override fun toString(): String {
        return "$amount $currency"
    }
}