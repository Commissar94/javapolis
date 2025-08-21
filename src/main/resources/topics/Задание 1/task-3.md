# Задание 3

Stage 3/5: What's up on the field
Description
In this stage, we’re going to analyze the game state to determine if either player has already won the game or it is still ongoing, if the game is a draw, or if the user has entered an impossible game state (two winners, or with one player having made too many moves).

Objectives
In this stage, your program should:

Take a string entered by the user and print the game grid as in the previous stage.
Analyze the game state and print the result. Possible states:
Game not finished when neither side has three in a row but the grid still has empty cells.
Draw when no side has a three in a row and the grid has no empty cells.
X wins when the grid has three X’s in a row.
O wins when the grid has three O’s in a row.
Impossible when the grid has three X’s in a row as well as three O’s in a row, or there are a lot more X's than O's or vice versa (the difference should be 1 or 0; if the difference is 2 or more, then the game state is impossible).
In this stage, we will assume that either X or O can start the game.

You can choose whether to use a space or underscore _ to print empty cells.

Examples
The examples below show outputs and analysis results for different game states. Your program should work in the same way.

Notice that after Enter cells: comes the user input.

Example 1:

Enter cells: XXXOO__O_
---------
| X X X |
| O O _ |
| _ O _ |
---------
X wins
Example 2:

Enter cells: XOXOXOXXO
---------
| X O X |
| O X O |
| X X O |
---------
X wins
Example 3:

Enter cells: XOOOXOXXO
---------
| X O O |
| O X O |
| X X O |
---------
O wins
Example 4:

Enter cells: XOXOOXXXO
---------
| X O X |
| O O X |
| X X O |
---------
Draw
Example 5:

Enter cells: XO_OOX_X_
---------
| X O   |
| O O X |
|   X   |
---------
Game not finished
Example 6:

Enter cells: XO_XO_XOX
---------
| X O _ |
| X O _ |
| X O X |
---------
Impossible
Example 7:

Enter cells: _O_X__X_X
---------
|   O   |
| X     |
| X   X |
---------
Impossible
Example 8:

Enter cells: _OOOO_X_X
---------
|   O O |
| O O   |
| X   X |
---------
Impossible


package tictactoe

fun main() {

    print("Enter cells: ")

    val input = readLine()!!.toString()
    val lineOfDashes = "---------"
    var xCount = 0
    var oCount = 0
    var spaceCount = 0
    var impossible = false
    var winningSides = 0
    var resultMessage = ""

    for (i in 0..8) {
        if (input[i] == 'X') {
            xCount++
        } else if (input[i] == 'O') {
            oCount++
        } else {
            spaceCount++
        }
    }

    if (xCount >= oCount + 2 || oCount >= xCount + 2) {
        impossible = true
    }

    println(lineOfDashes)
    println("| ${input[0]} ${input[1]} ${input[2]} |")
    println("| ${input[3]} ${input[4]} ${input[5]} |")
    println("| ${input[6]} ${input[7]} ${input[8]} |")
    println(lineOfDashes)

    if (input[0] == input[1] && input[0] == input[2]) {   //победа в первой линии
        resultMessage = "${input[0]} wins"
        winningSides++
    }
    if (input[3] == input[4] && input[3] == input[5]) {   //победа во второй линии
        resultMessage = "${input[3]} wins"
        winningSides++
    }
    if (input[6] == input[7] && input[6] == input[8]) {   //победа в третьей линии
        resultMessage = "${input[6]} wins"
        winningSides++
    }
    if (input[0] == input[4] && input[0] == input[8]) {   //победа в первой диагонали
        resultMessage = "${input[0]} wins"
        winningSides++
    }
    if (input[6] == input[4] && input[6] == input[2]) {   //победа во второй диагонали
        resultMessage = "${input[6]} wins"
        winningSides++
    }
    if (input[0] == input[3] && input[0] == input[6]) {   //победа в первом столбике
        resultMessage = "${input[0]} wins"
        winningSides++
    }
    if (input[1] == input[4] && input[1] == input[7]) {   //победа во втором столбике
        resultMessage = "${input[1]} wins"
        winningSides++
    }
    if (input[2] == input[5] && input[2] == input[8]) {   //победа во третьем столбике
        resultMessage = "${input[2]} wins"
        winningSides++
    }

    if (winningSides == 1) {
        println(resultMessage)
    } else if (winningSides == 0 && spaceCount > 0 && !impossible) {
        println("Game not finished")
    } else if (winningSides == 0 && spaceCount == 0) {
        println("Draw")
    }
    else {
        println("Impossible")
    }
}
