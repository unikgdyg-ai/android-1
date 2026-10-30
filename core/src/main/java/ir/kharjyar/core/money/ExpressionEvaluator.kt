package ir.kharjyar.core.money

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * ماشین‌حساب داخلی فیلد مبلغ (پشتیبانی از عبارات ریاضی مثل "150000 + 45000 * 2")
 * تمام محاسبات با BigDecimal انجام و خروجی نهایی به صورت Long دقیق برگردانده می‌شود.
 */
object ExpressionEvaluator {

    fun evaluateToLong(expression: String): Long? {
        val normalized = MoneyFormatter.normalizeDigitsToEnglish(expression)
            .replace("×", "*")
            .replace("÷", "/")
            .trim()
        if (normalized.isEmpty()) return null

        return try {
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) return null
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn)
            result.setScale(0, RoundingMode.HALF_UP).longValueExact()
        } catch (e: Exception) {
            null
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "+-*/()" -> {
                    // مدیریت علامت منفی یکانی در ابتدای عبارت یا بعد از پرانتز باز
                    if (c == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "("))) {
                        val sb = StringBuilder("-")
                        i++
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                            sb.append(expr[i])
                            i++
                        }
                        if (sb.length == 1) return emptyList()
                        tokens.add(sb.toString())
                    } else {
                        tokens.add(c.toString())
                        i++
                    }
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> return emptyList()
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = ArrayDeque<String>()
        for (token in tokens) {
            when {
                token.toBigDecimalOrNull() != null -> output.add(token)
                token == "(" -> operators.addLast(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        output.add(operators.removeLast())
                    }
                    require(operators.isNotEmpty() && operators.last() == "(") { "Mismatched parentheses" }
                    operators.removeLast()
                }
                token in listOf("+", "-", "*", "/") -> {
                    while (operators.isNotEmpty() && precedence(operators.last()) >= precedence(token)) {
                        output.add(operators.removeLast())
                    }
                    operators.addLast(token)
                }
            }
        }
        while (operators.isNotEmpty()) {
            val op = operators.removeLast()
            require(op != "(") { "Mismatched parentheses" }
            output.add(op)
        }
        return output
    }

    private fun evaluateRpn(rpn: List<String>): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()
        for (token in rpn) {
            val number = token.toBigDecimalOrNull()
            if (number != null) {
                stack.addLast(number)
            } else {
                require(stack.size >= 2) { "Invalid expression" }
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a.add(b)
                    "-" -> a.subtract(b)
                    "*" -> a.multiply(b)
                    "/" -> {
                        require(b.compareTo(BigDecimal.ZERO) != 0) { "Division by zero" }
                        a.divide(b, 4, RoundingMode.HALF_UP)
                    }
                    else -> throw IllegalArgumentException("Unknown operator $token")
                }
                stack.addLast(res)
            }
        }
        require(stack.size == 1) { "Invalid expression state" }
        return stack.last()
    }
}
