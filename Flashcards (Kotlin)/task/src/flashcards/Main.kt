package flashcards

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

var log = mutableListOf<String>()

fun println(message: String) {
    kotlin.io.println(message)
    log.add(message)
}

fun readln(): String {
    var s = kotlin.io.readln()
    log.add(s)
    return s
}

data class Card(var cardFront: String = "Empty", var cardBack: String = "Empty", var errors: Int = 0)

class Cards {
    var cardsList = mutableListOf<Card>()
    var importedCardsList = mutableListOf<Card>()
    fun getCard() {
        var cardFront: String
        var cardBack: String
        println("The card:")
        cardFront = readln()
        for (card in cardsList) {
            if (cardFront == card.cardFront) {
                println("The card \"$cardFront\" already exists.")
                return
            }
        }
        println("The definition of the card:")
        cardBack = readln()
        for (card in cardsList) {
            if (cardBack == card.cardBack) {
                println("The definition \"$cardBack\" already exists.")
                return
            }
        }
        cardsList.add(Card(cardFront, cardBack))
        println("The pair (\"$cardFront\":\"$cardBack\") has been added.")
    }

    fun removeCard() {
        var cardFront: String
        var resultText = ""
        println("Which card?")
        cardFront = readln()
        for (i in 0..cardsList.lastIndex) {
            if (cardsList[i].cardFront == cardFront) {
                cardsList.removeAt(i)
                resultText = "The card has been removed."
                break
            } else resultText = "Can't remove \"$cardFront\": there is no such card."
        }
        println(resultText)
    }

    fun askQuestions() {
        var asksCount: Int
        var wrongAnswerText = ""
        println("How many times to ask?")
        asksCount = readln().toInt()
        for (i in 0..asksCount - 1) {
            var rnd = Random.nextInt(0, cardsList.size)
            var askingCard = cardsList[rnd]
            println("Print the definition of \"${askingCard.cardFront}\":")
            var answer = readln()
            if (answer == askingCard.cardBack) println("Correct!")
            else {
                askingCard.errors += 1
                for (card in cardsList) {
                    if (answer == card.cardBack) {
                        wrongAnswerText =
                            "Wrong. The right answer is \"${askingCard.cardBack}\", but your definition is correct for \"${card.cardFront}\"."
                        break
                    } else wrongAnswerText = "Wrong. The right answer is \"${askingCard.cardBack}\"."
                }
                println(wrongAnswerText)
            }
        }
    }

    fun importCardsList(importFileName:String) {
        val gson = Gson()
        val cardsType = object : TypeToken<MutableList<Card>>() {}.type
        val jsonFile = File(importFileName)
        var jsonString: String
        if (File(importFileName).exists()) {
            jsonString = jsonFile.readText()
            importedCardsList = gson.fromJson(jsonString, cardsType)
            println("${importedCardsList.size} cards have been loaded .")
        } else println("File not found.")
        if (cardsList.size != 0) {
            while (importedCardsList.size > 0) {
                for (cardsCount in 0..cardsList.size - 1) {
                    for (impCardsCount in 0..importedCardsList.size - 1) {
                        if (importedCardsList[impCardsCount].cardFront == cardsList[cardsCount].cardFront) {
                            cardsList[cardsCount] = importedCardsList[impCardsCount]
                            importedCardsList.removeAt(impCardsCount)
                            break
                        } else {
                            cardsList.add(importedCardsList[impCardsCount])
                            importedCardsList.removeAt(impCardsCount)
                            break
                        }
                    }
                }
            }
        } else cardsList.addAll(importedCardsList)
    }

    fun exportCards(exportFileName:String) {
        val gson = Gson()
        val cardsType = object : TypeToken<MutableList<Card>>() {}.type
        val jsonFile = File(exportFileName)
        val jsonString = gson.toJson(cardsList, cardsType)
        jsonFile.writeText(jsonString)
        println("${cardsList.size} cards have been saved.")
    }

    fun logIt() {
        println("File name:")
        var file = File(readln()).bufferedWriter()
        println("The log has been saved.")
        log.forEach {
            file.appendLine(it)
        }
        file.close()
    }

    fun hardest() {
        var list_Of_Cards_indexes_With_Max_Errors = mutableListOf<Int>()
        var maxErrors = 0
        try {
            maxErrors = cardsList.maxOf { it.errors }
        } catch (e: Exception) {
            maxErrors = 0
        }
        var str = ""
        for (i in 0 until cardsList.size) if (cardsList[i].errors == maxErrors) {
            list_Of_Cards_indexes_With_Max_Errors.add(i)
            str += "\"${cardsList[i].cardFront}\", "
        }
        str = str.dropLast(2)
        if (maxErrors > 0) {
            if (list_Of_Cards_indexes_With_Max_Errors.size == 1) println("The hardest card is $str. You have $maxErrors errors answering it.")
            else println("The hardest cards are $str. You have $maxErrors errors answering them.")
        } else println("There are no cards with errors.")
    }

    fun reset() {
        cardsList.forEach {
            it.errors = 0
            println("Card statistics have been reset.")
        }
    }
}

fun main(args: Array<String>) {
    val argsMap = args.toList().chunked(2).associate { it[0] to it[1] }
    var importFileName: String? = null
    var exportFileName: String? = null
    var cards = Cards()
    try {
        importFileName = argsMap["-import"]
    } catch (e: Exception){ }
    try {
        exportFileName = argsMap["-export"]
    } catch (e: Exception){ }
    if (importFileName != null)
        cards.importCardsList(importFileName)
    while (true) {
        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        var inp = readln()
        when (inp.lowercase()) {
            "add" -> cards.getCard()
            "remove" -> cards.removeCard()
            "import" -> {
                println("File name:")
                cards.importCardsList(readln())
            }
            "export" -> {
                println("File name:")
                cards.exportCards(readln())
            }
            "ask" -> cards.askQuestions()
            "exit" -> {
                if (exportFileName != null)
                    cards.exportCards(exportFileName)
                println("Bye bye!")
                exitProcess((0))
            }
            "log" -> cards.logIt()
            "hardest card" -> cards.hardest()
            "reset stats" -> cards.reset()
            else -> println("The input action is invalid")
        }
    }
}