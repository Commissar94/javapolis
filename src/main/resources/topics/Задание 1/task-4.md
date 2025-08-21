# Задание 4

Stage 4/5: First move_
Description
It’s time to make our game interactive! Now we’re going to add the ability for a user to make a move.

To do this, we need to divide the grid into cells.

Suppose the top left cell has the coordinates (1, 1) and the bottom right cell has the coordinates (3, 3) like in this table:

(1, 1) (1, 2) (1, 3)
(2, 1) (2, 2) (2, 3)
(3, 1) (3, 2) (3, 3)

The program should ask the user to enter the coordinates of the cell where they want to make a move.

In this stage, the user plays as X, not O. Keep in mind that the first coordinate goes from left to right and the second coordinate goes from top to bottom. Also note that coordinates start with 1 and can be 1, 2, or 3.

What happens if the user enters incorrect coordinates? The user could enter symbols instead of numbers, or enter coordinates representing occupied cells or cells that aren’t even on the grid. You need to check the user's input and catch possible exceptions.

Objectives
The program should work as follows:

Get the 3x3 grid from the input as in the previous stages.
Output this 3x3 grid as in the previous stages.
Prompt the user to make a move.
The user should input 2 numbers that represent the cell where they want to place their X. (the 9 symbols representing the field will be the first line of input, and the 2 coordinate numbers will be the second line of input)
Analyze user input and show messages in the following situations:
This cell is occupied! Choose another one! if the cell is not empty.
You should enter numbers! if the user enters non-numeric symbols in the coordinates input.
Coordinates should be from 1 to 3! if the user enters coordinates outside the game grid.
Update the grid to include the user's move and print the updated grid to the console.
The program should also check the user’s input. If the input is unsuitable, the program should tell the user why their input was wrong, and prompt them to enter coordinates again.

To summarize, you need to output the game grid based on the first line of input, and then ask the user to enter a move. Keep asking until the user enters coordinates that represent an empty cell on the grid, update the grid to include that move, and then output it to the console. You should output the field only 2 times: once before the user’s move, and once after the user has entered a legal move.

Do not delete the code you already wrote that analyzes the game state; you will need it in the final step of this project.

Examples
The examples below show how your program should work.

Notice that after Enter cells: and Enter the coordinates: comes the user input.

Example 1:

Enter cells: X_X_O____
---------
| X   X |
|   O   |
|       |
---------
Enter the coordinates: 3 1
---------
| X   X |
|   O   |
| X     |
---------
Example 2:

Enter cells: _XXOO_OX_
---------
|   X X |
| O O   |
| O X   |
---------
Enter the coordinates: 1 1
---------
| X X X |
| O O   |
| O X   |
---------
Example 3:

Enter cells: _XXOO_OX_
---------
|   X X |
| O O   |
| O X   |
---------
Enter the coordinates: 3 3
---------
|   X X |
| O O   |
| O X X |
---------
Example 4:

Enter cells: _XXOO_OX_
---------
|   X X |
| O O   |
| O X   |
---------
Enter the coordinates: 2 3
---------
|   X X |
| O O X |
| O X   |
---------
Example 5:

Enter cells: _XXOO_OX_
---------
|   X X |
| O O   |
| O X   |
---------
Enter the coordinates: 3 1
This cell is occupied! Choose another one!
Enter the coordinates: 1 1
---------
| X X X |
| O O   |
| O X   |
---------
Example 6:

Enter cells: _XXOO_OX_
---------
|   X X |
| O O   |
| O X   |
---------
Enter the coordinates: one
You should enter numbers!
Enter the coordinates: one one
You should enter numbers!
Enter the coordinates: 1 1
---------
| X X X |
| O O   |
| O X   |
---------
Example 7:

Enter cells: _XXOO_OX_
---------
|   X X |
| O O   |
| O X   |
---------
Enter the coordinates: 4 1
Coordinates should be from 1 to 3!
Enter the coordinates: 1 4
Coordinates should be from 1 to 3!
Enter the coordinates: 1 1
---------
| X X X |
| O O   |
| O X   |
---------


package tictactoe

fun main() {

    print("Enter cells: ")

    val input = readLine()!!.toCharArray()
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

    printCells(lineOfDashes, input) //выводим поле в первый раз

    var inputs = 2
    var wrongNumber = true

while (inputs > 1) {

    print("Enter the coordinates: ")
    var coordinatesInput = readLine()!!.toCharArray()
    var xCoordinate = coordinatesInput[0]
    var yCoordinate = coordinatesInput[2]

    if ((xCoordinate == '1' || xCoordinate == '2' || xCoordinate == '3') && (yCoordinate == '1' || yCoordinate == '2' || yCoordinate == '3')) {
        wrongNumber = false
    }

    if (xCoordinate == '1' && !wrongNumber) {
        if (yCoordinate == '1' && input[0] == '_') {
            input[0] = 'X'
            printCells(lineOfDashes, input)
            spaceCount--
            inputs=1
        } else if (yCoordinate == '2' && input[1] == '_') {
            input[1] = 'X'
            inputs=1
            printCells(lineOfDashes, input)
            spaceCount--
        } else if (yCoordinate == '3' && input[2] == '_') {
            input[2] = 'X'
            inputs=1
            printCells(lineOfDashes, input)
            spaceCount--
        } else if ((input[0] != '_' && yCoordinate == '1') || (input[1] != '_' && yCoordinate == '2') || (input[2] != '_' && yCoordinate == '3')) {
            println("This cell is occupied! Choose another one!")
            inputs++
        } else {
            println("You should enter numbers!")
            inputs++
        }
    } else if (xCoordinate == '2' && !wrongNumber) {
        if (yCoordinate == '1' && input[3] == '_') {
            input[3] = 'X'
            printCells(lineOfDashes, input)
            inputs=1
            spaceCount--
        } else if (yCoordinate == '2' && input[4] == '_') {
            input[4] = 'X'
            inputs=1
            spaceCount--
            printCells(lineOfDashes, input)
        } else if (yCoordinate == '3' && input[5] == '_') {
            input[5] = 'X'
            inputs=1
            spaceCount--
            printCells(lineOfDashes, input)
        } else if ((input[3] != '_' && yCoordinate == '1') || (input[4] != '_' && yCoordinate == '2') || (input[5] != '_' && yCoordinate == '3')) {
            println("This cell is occupied! Choose another one!")
            inputs++
        } else {
            println("You should enter numbers!")
            inputs++
        }

    } else if (xCoordinate == '3' && !wrongNumber) {
        if (yCoordinate == '1' && input[6] == '_') {
            input[6] = 'X'
            inputs=1
            spaceCount--
            printCells(lineOfDashes, input)
        } else if (yCoordinate == '2' && input[7] == '_') {
            input[7] = 'X'
            inputs=1
            spaceCount--
            printCells(lineOfDashes, input)
        } else if (yCoordinate == '3' && input[8] == '_') {
            input[8] = 'X'
            inputs=1
            spaceCount--
            printCells(lineOfDashes, input)
        } else if ((input[6] != '_' && yCoordinate == '1') || (input[7] != '_' && yCoordinate == '2') || (input[8] != '_' && yCoordinate == '3')) {
            println("This cell is occupied! Choose another one!")
            inputs++
        } else {
            println("You should enter numbers!")
            inputs++
        }

    } else if (xCoordinate in 'A'..'Z' || yCoordinate in 'A'..'Z' || xCoordinate in 'a'..'z' || yCoordinate in 'a'..'z') {
        print("You should enter numbers!")
        inputs++
    } else {
        println("Coordinates should be from 1 to 3!")
        inputs++
    }
}

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


//    if (winningSides == 1) {
//        println(resultMessage)
//    } else if (winningSides == 0 && spaceCount > 0 && !impossible) {
//        println("Game not finished")
//    } else if (winningSides == 0 && spaceCount == 0) {
//        println("Draw")
//    } else {
//        println("Impossible")
//    }
}

fun printCells(lineOfDashes: String, input: CharArray) {
println(lineOfDashes)
println("| ${input[0]} ${input[1]} ${input[2]} |")
println("| ${input[3]} ${input[4]} ${input[5]} |")
println("| ${input[6]} ${input[7]} ${input[8]} |")
println(lineOfDashes)
}