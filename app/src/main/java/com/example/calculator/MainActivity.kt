package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Calculator()
                }
            }
        }
    }
}

@Composable
fun Calculator(modifier: Modifier = Modifier) {
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = expression, fontSize = 32.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = result, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        val buttons = listOf(
            listOf("C", "del", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("", "0", ".", "=")
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { symbol ->
                        Button(
                            onClick = {
                                when (symbol) {
                                    "C" -> { expression = ""; result = "" }
                                    "del" -> if (expression.isNotEmpty()) {
                                        expression = expression.dropLast(1)
                                        result = ""
                                    }
                                    "=" -> result = solve(expression)
                                    else -> {
                                        expression += symbol
                                        result = ""
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp),
                        ) {
                            Text(text = symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun solve(input: String): String {
    val expression = input.replace(" ", "")

    if (expression.isBlank()) {
        return ""
    }

    return try {
        val parser = object {
            var index = 0

            fun parse(): Double {
                val value = parseExpression()
                if (index != expression.length) {
                    throw IllegalArgumentException("Unexpected character: ${expression[index]}")
                }
                return value
            }

            private fun parseExpression(): Double {
                var value = parseTerm()

                while (index < expression.length) {
                    when (expression[index]) {
                        '+' -> {
                            index++
                            value += parseTerm()
                        }

                        '-' -> {
                            index++
                            value -= parseTerm()
                        }

                        else -> return value
                    }
                }

                return value
            }

            private fun parseTerm(): Double {
                var value = parseFactor()

                while (index < expression.length) {
                    when (expression[index]) {
                        '*' -> {
                            index++
                            value *= parseFactor()
                        }

                        '/' -> {
                            index++
                            val divisor = parseFactor()
                            if (divisor == 0.0) {
                                throw ArithmeticException("Division by zero")
                            }
                            value /= divisor
                        }

                        else -> return value
                    }
                }

                return value
            }

            private fun parseFactor(): Double {
                var sign = 1.0

                while (index < expression.length) {
                    when (expression[index]) {
                        '+' -> index++
                        '-' -> {
                            sign *= -1
                            index++
                        }

                        else -> break
                    }
                }

                var value = parseNumber() * sign

                while (index < expression.length && expression[index] == '%') {
                    value /= 100.0
                    index++
                }

                return value
            }

            private fun parseNumber(): Double {
                val start = index
                var hasDecimalPoint = false

                while (index < expression.length) {
                    val current = expression[index]
                    when {
                        current.isDigit() -> index++
                        current == '.' && !hasDecimalPoint -> {
                            hasDecimalPoint = true
                            index++
                        }

                        else -> break
                    }
                }

                if (start == index || expression.substring(start, index) == ".") {
                    throw IllegalArgumentException("Invalid number")
                }

                return expression.substring(start, index).toDouble()
            }
        }

        formatResult(parser.parse())
    } catch (_: Exception) {
        "Error"
    }
}

private fun formatResult(value: Double): String {
    if (!value.isFinite()) {
        return "Error"
    }

    val rounded = value.toLong()
    return if (value == rounded.toDouble()) {
        rounded.toString()
    } else {
        value.toString().trimEnd('0').trimEnd('.')
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CalculatorTheme {
        Calculator()
    }
}