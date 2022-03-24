package task

import java.io.File
import java.io.InputStream
import java.util.LinkedList

const val EOF_SYMBOL = -1
const val ERROR_STATE = 0
const val SKIP_VALUE = 0

const val NEWLINE = '\n'.code

interface Automaton {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, symbol: Int): Int
    fun value(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object Example : Automaton {
    override val states = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40)
    override val alphabet = 0 .. 255
    override val startState = 1
    override val finalStates = setOf(2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 23, 24, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40)

    private val numberOfStates = states.maxOrNull()!! + 1
    private val numberOfSymbols = alphabet.maxOrNull()!! + 1
    private val transitions = Array(numberOfStates) {IntArray(numberOfSymbols)}
    private val values: Array<Int> = Array(numberOfStates) {0}

    private fun setTransition(from: Int, symbol: Char, to: Int) {
        transitions[from][symbol.code] = to
    }

    private fun setValue(state: Int, terminal: Int) {
        values[state] = terminal
    }

    override fun next(state: Int, symbol: Int): Int =
        if (symbol == EOF_SYMBOL) ERROR_STATE
        else {
            assert(states.contains(state))
            assert(alphabet.contains(symbol))
            transitions[state][symbol]
        }

    override fun value(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }

    init {
        val escapeChars = setOf(' ', '\r', '\n')
        for(ch in escapeChars){
            //all transitions for special characters
            setTransition(1, ch, 1) //at the start
            setTransition(2, ch, 14) //after float without .
            setTransition(4, ch, 14) //after float
            setTransition(14, ch, 14) //there can be multiple after floats
            setTransition(5, ch, 15) //after variable without numbers
            setTransition(6, ch, 15) //after variable
            setTransition(15, ch, 15) //there can be multiple after variable
            setTransition(7, ch, 7) //after plus
            setTransition(8, ch, 8) //after minus
            setTransition(9, ch, 9) //after times
            setTransition(10, ch, 10) //after divide
            setTransition(11, ch, 11) //after pow
            setTransition(12, ch, 12) //after lparen
            setTransition(13, ch, 13) //after rparen

            //added transitions for special characters for assignment 2
            setTransition(17, ch, 17) //after assign
            setTransition(18, ch, 18) //after semi

            //because reserved words can continue and turn into variable, I can't loop special characters on last state because that would produce a variable with spaces instead of reserved word
            setTransition(23, ch, 35)
            setTransition(35, ch, 35)

            setTransition(26, ch, 36)
            setTransition(36, ch, 36)

            setTransition(28, ch, 37)
            setTransition(37, ch, 37)

            setTransition(30, ch, 38)
            setTransition(38, ch, 38)

            setTransition(32, ch, 39)
            setTransition(39, ch, 39)

            //special characters after incomplete reserved word results into it being a variable
            setTransition(19, ch, 34)
            setTransition(20, ch, 34)
            setTransition(21, ch, 34)
            setTransition(22, ch, 34)
            setTransition(24, ch, 34)
            setTransition(25, ch, 34)
            setTransition(27, ch, 34)
            setTransition(29, ch, 34)
            setTransition(31, ch, 34)
            setTransition(34, ch, 34)
        }

        //float
        for(ch in '0' .. '9'){
            setTransition(1, ch, 2)
            setTransition(2, ch, 2)
        }
        setTransition(2, '.', 3)
        for(ch in '0' .. '9'){
            setTransition(3, ch, 4)
            setTransition(4, ch, 4)
        }

        //variable
        for(ch in ('a' .. 'z') + ('A'..'Z') - 'W' - 'f' - 't' - 'd'){ //-W,f,t,d because they can be start of reserved word
            setTransition(1, ch, 5)
        }
        for(ch in '0' .. '9'){
            setTransition(5, ch, 6)
            setTransition(6, ch, 6)
        }
        for(ch in ('a' .. 'z') + ('A'..'Z')){
            setTransition(5, ch, 5)
            setTransition(6, ch, 34)
            setTransition(34, ch, 34)
        }

        //other symbols
        setTransition(1, '+', 7)
        setTransition(1, '-', 8)
        setTransition(1, '*', 9)
        setTransition(1, '/', 10)
        setTransition(1, '^', 11)
        setTransition(1, '(', 12)
        setTransition(1, ')', 13)

        //added transitions for assignment 2
        setTransition(1, ':', 16)
        setTransition(16, '=', 17)
        setTransition(1, ';', 18)

        //write
        setTransition(1, 'W', 19)
        setTransition(19, 'R', 20)
        setTransition(20, 'I', 21)
        setTransition(21, 'T', 22)
        setTransition(22, 'E', 23)
        for(ch in ('a' .. 'z') + ('A'..'Z')){
            if(ch != 'R') setTransition(19, ch, 5)
            if(ch != 'I') setTransition(20, ch, 5)
            if(ch != 'T') setTransition(21, ch, 5)
            if(ch != 'E') setTransition(22, ch, 5)
            setTransition(23, ch, 5)
        }
        for(ch in '0' .. '9'){
            setTransition(19, ch, 6)
            setTransition(20, ch, 6)
            setTransition(21, ch, 6)
            setTransition(22, ch, 6)
            setTransition(23, ch, 6)
        }

        //for
        setTransition(1, 'f', 24)
        setTransition(24, 'o', 25)
        setTransition(25, 'r', 26)
        for(ch in ('a' .. 'z') + ('A'..'Z')){
            if(ch != 'o') setTransition(24, ch, 5)
            if(ch != 'r') setTransition(25, ch, 5)
            setTransition(26, ch, 5)
        }
        for(ch in '0' .. '9'){
            setTransition(24, ch, 6)
            setTransition(25, ch, 6)
            setTransition(26, ch, 6)
        }

        //to
        setTransition(1, 't', 27)
        setTransition(27, 'o', 28)
        for(ch in ('a' .. 'z') + ('A'..'Z')){
            if(ch != 'o') setTransition(27, ch, 5)
            setTransition(28, ch, 5)
        }
        for(ch in '0' .. '9'){
            setTransition(27, ch, 6)
            setTransition(28, ch, 6)
        }

        //do, done
        setTransition(1, 'd', 29)
        setTransition(29, 'o', 30)
        setTransition(30, 'n', 31)
        setTransition(31, 'e', 32)
        for(ch in ('a' .. 'z') + ('A'..'Z')){
            if(ch != 'o') setTransition(29, ch, 5)
            if(ch != 'n') setTransition(30, ch, 5)
            if(ch != 'e') setTransition(31, ch, 5)
            setTransition(32, ch, 5)
        }
        for(ch in '0' .. '9'){
            setTransition(29, ch, 6)
            setTransition(30, ch, 6)
            setTransition(31, ch, 6)
            setTransition(32, ch, 6)
        }

        setValue(2, 1)
        setValue(4, 1)
        setValue(5, 2)
        setValue(6, 2)
        setValue(7, 3)
        setValue(8, 4)
        setValue(9, 5)
        setValue(10, 6)
        setValue(11, 7)
        setValue(12, 8)
        setValue(13, 9)
        setValue(14, 1)
        setValue(15, 2)

        //added values assignment 2
        setValue(17, 10)
        setValue(18, 11)
        setValue(35, 12)
        setValue(23, 12)
        setValue(36, 13)
        setValue(26, 13)
        setValue(37, 14)
        setValue(28, 14)
        setValue(38, 15)
        setValue(30, 15)
        setValue(39, 16)
        setValue(32, 16)

        setValue(34, 2)
    }
}

fun name(value: Int) =
    when (value) {
        1 -> "float"
        2 -> "variable"
        3 -> "plus"
        4 -> "minus"
        5 -> "times"
        6 -> "divide"
        7 -> "pow"
        8 -> "lparen"
        9 -> "rparen"
        10 -> "assign"
        11 -> "semi"
        12 -> "write"
        13 -> "for"
        14 -> "to"
        15 -> "do"
        16 -> "done"
        else -> throw Error("Invalid value")
    }

const val FLOAT = 1
const val VARIABLE = 2
const val PLUS = 3
const val MINUS = 4
const val TIMES = 5
const val DIVIDE = 6
const val POW = 7
const val LPAREN = 8
const val RPAREN = 9
const val ASSIGN = 10
const val SEMI = 11
const val WRITE = 12
const val FOR = 13
const val TO = 14
const val DO = 15
const val DONE = 16

data class Token(val value: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: Automaton, private val stream: InputStream) {
    private var state = automaton.startState
    private var last: Int? = null
    private var buffer = LinkedList<Byte>()
    private var row = 1
    private var column = 1

    private fun updatePosition(symbol: Int) {
        if (symbol == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    private fun getValue(): Int {
        var symbol = last ?: stream.read()
        state = automaton.startState

        while (true) {
            updatePosition(symbol)
            val nextState = automaton.next(state, symbol)
            if (nextState == ERROR_STATE) {
                if (automaton.finalStates.contains(state)) {
                    last = symbol
                    return automaton.value(state)
                } else throw Error("Invalid pattern at ${row}:${column}")
            }
            state = nextState
            if(symbol != ' '.code && symbol != '\n'.code && symbol != '\r'.code){
                buffer.add(symbol.toByte())
            }
            symbol = stream.read()
        }
    }

    fun eof(): Boolean =
        last == EOF_SYMBOL

    fun getToken(): Token? {
        if (eof()) return null

        val startRow = row
        val startColumn = column
        buffer.clear()

        val value = getValue()
        return if (value == SKIP_VALUE)
            getToken()
        else
            Token(value, String(buffer.toByteArray()), startRow, startColumn)
    }
}

fun printTokens(scanner: Scanner) {
    val token = scanner.getToken()
    if (token != null) {
        print("${name(token.value)}(\"${token.lexeme}\") ")
        printTokens(scanner)
    }
}

class Recognizer(private val scanner: Scanner) {
    private var last: Token? = null

    fun recognize(): Boolean {
        last = scanner.getToken()
        val status = recognizeG()
        return if (last == null) status
        else false
    }

    fun recognizeE(): Boolean = recognizeT() && recognizeEE()

    fun recognizeEE(): Boolean {
        return when(last?.value) {
            PLUS -> recognizeTerminal(PLUS) && recognizeT() && recognizeEE()
            MINUS -> recognizeTerminal(MINUS) && recognizeT() && recognizeEE()
            else -> true
        }
    }

    fun recognizeT(): Boolean = recognizeX() && recognizeTT()

    fun recognizeTT(): Boolean {
        return when(last?.value) {
            TIMES -> recognizeTerminal(TIMES) && recognizeX() && recognizeTT()
            DIVIDE -> recognizeTerminal(DIVIDE) && recognizeX() && recognizeTT()
            else -> true
        }
    }

    fun recognizeX(): Boolean = recognizeY() && recognizeXX()

    fun recognizeXX(): Boolean {
        return when(last?.value) {
            POW -> recognizeTerminal(POW) && recognizeX()
            else -> true
        }
    }

    fun recognizeY(): Boolean {
        return when(last?.value) {
            MINUS -> recognizeTerminal(MINUS) && recognizeF()
            PLUS -> recognizeTerminal(PLUS) && recognizeF()
            LPAREN, FLOAT, VARIABLE -> recognizeF()
            else -> false
        }
    }

    fun recognizeF(): Boolean {
        return when(last?.value) {
            LPAREN -> recognizeTerminal(LPAREN) && recognizeE() && recognizeTerminal(RPAREN)
            FLOAT -> recognizeTerminal(FLOAT)
            VARIABLE -> recognizeTerminal(VARIABLE)
            else -> false
        }
    }

    fun recognizeG(): Boolean {
        return when(last?.value) {
            FOR -> recognizeTerminal(FOR) && recognizeH() && recognizeTerminal(TO) && recognizeE() && recognizeTerminal(DO) && recognizeG() && recognizeTerminal(DONE) && recognizeI()
            VARIABLE -> recognizeH() && recognizeI()
            WRITE -> recognizeTerminal(WRITE) && recognizeE() && recognizeI()
            else -> true
        }
    }

    fun recognizeH(): Boolean = recognizeTerminal(VARIABLE) && recognizeTerminal(ASSIGN) && recognizeE()

    fun recognizeI(): Boolean {
        if(last?.value == null){
            return true
        }
        return when(last?.value) {
            SEMI -> recognizeTerminal(SEMI) && recognizeG()
            DONE -> recognizeDone()
            else -> false
        }
    }

    private fun recognizeTerminal(value: Int): Boolean =
        if (last?.value == value) {
            last = scanner.getToken()
            true
        } else {
            false
        }

    private fun recognizeDone(): Boolean =
        last?.value == DONE
}

fun readFile(stream: InputStream): String {
    val buffer = LinkedList<Byte>()

    while (true) {
        val symbol = stream.read()
        if (symbol == -1) break
        buffer.add(symbol.toByte())
    }
    return String(buffer.toByteArray())
}

fun main(args: Array<String>) {
    if(args.isNotEmpty()) {
        val scanner = Scanner(
            Example,
            readFile(File(args[0]).inputStream()).byteInputStream()
        )
        if (Recognizer(scanner).recognize()) {
            print("accept")
        } else {
            print("reject")
        }
    }else{
        println("No file specified.")
    }
}

