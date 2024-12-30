package org.example.project.CalcApp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CalcMainScreen(modifier: Modifier = Modifier) {

    Box(modifier = modifier.fillMaxSize()){
        val arrayOfElements = arrayOf(1,2,3,4,5,6,7,8,9,0,"+","-","*","/","(",")","C","=",".","00","AC")
        val inputString = remember { mutableStateOf("") }
        Column{
            Box(modifier = modifier.weight(1f).fillMaxSize().background(Color.Blue)){
                TextField(
                    value = inputString.value.toString(),
                    onValueChange = {
                        inputString.value = it
                    } ,
                    modifier = modifier.align(Alignment.BottomEnd).fillMaxWidth()
                )
            }
            Box(modifier = modifier.weight(2f).fillMaxSize().background(Color.Red),
                contentAlignment = Alignment.Center){
                LazyVerticalStaggeredGrid(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    columns = StaggeredGridCells.Fixed(3),
                    contentPadding = PaddingValues(10.dp),
                    modifier = modifier.fillMaxSize().background(Color.Red).align(Alignment.Center)
                ){
                    items(arrayOfElements){
                        Button(onClick = {
                            if(it.equals("C")){
                              inputString.value = inputString.value.dropLast(1)
                            }
                            else if(it.equals("AC")){
                                inputString.value = ""
                            }
                            else if(it.equals("=")){
                                inputString.value = evaluate(inputString.value).toString()
                            }
                            else {
                                inputString.value += it.toString()
                                }
                            }
                        ){
                            Text(text = it.toString())
                        }
                    }
                }
            }
        }
    }
}


fun evaluate(str: String): Double {

    data class Data(val rest: List<Char>, val value: Double)

    return object : Any() {

        fun parse(chars: List<Char>): Double {
            return getExpression(chars.filter { it != ' ' })
                .also { if (it.rest.isNotEmpty()) throw RuntimeException("Unexpected character: ${it.rest.first()}") }
                .value
        }

        private fun getExpression(chars: List<Char>): Data {
            var (rest, carry) = getTerm(chars)
            while (true) {
                when {
                    rest.firstOrNull() == '+' -> rest = getTerm(rest.drop(1)).also { carry += it.value }.rest
                    rest.firstOrNull() == '-' -> rest = getTerm(rest.drop(1)).also { carry -= it.value }.rest
                    else                      -> return Data(rest, carry)
                }
            }
        }

        private fun getTerm(chars: List<Char>): Data {
            var (rest, carry) = getFactor(chars)
            while (true) {
                when {
                    rest.firstOrNull() == '*' -> rest = getTerm(rest.drop(1)).also { carry *= it.value }.rest
                    rest.firstOrNull() == '/' -> rest = getTerm(rest.drop(1)).also { carry /= it.value }.rest
                    else                      -> return Data(rest, carry)
                }
            }
        }

        private fun getFactor(chars: List<Char>): Data {
            return when (val char = chars.firstOrNull()) {
                '+'              -> getFactor(chars.drop(1)).let { Data(it.rest, +it.value) }
                '-'              -> getFactor(chars.drop(1)).let { Data(it.rest, -it.value) }
                '('              -> getParenthesizedExpression(chars.drop(1))
                in '0'..'9', '.' -> getNumber(chars) // valid first characters of a number
                else             -> throw RuntimeException("Unexpected character: $char")
            }
        }

        private fun getParenthesizedExpression(chars: List<Char>): Data {
            return getExpression(chars)
                .also { if (it.rest.firstOrNull() != ')') throw RuntimeException("Missing closing parenthesis") }
                .let { Data(it.rest.drop(1), it.value) }
        }

        private fun getNumber(chars: List<Char>): Data {
            val s = chars.takeWhile { it.isDigit() || it == '.' }.joinToString("")
            return Data(chars.drop(s.length), s.toDouble())
        }

    }.parse(str.toList())

}
