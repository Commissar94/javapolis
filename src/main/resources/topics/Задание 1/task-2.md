# Задание 2

Stage 2/5: The user is the gamemaster
Description
Our program should be able to display the grid at all stages of the game. Now we’re going to write a program that allows the user to enter a string representing the game state and correctly prints the 3x3 game grid based on this input. We’ll also add some boundaries around the game grid.

Objectives
In this stage, you will write a program that:

Reads a string of 9 symbols from the input and displays them to the user in a 3x3 grid. The grid can contain only X, O and _ symbols.
Outputs a line of dashes --------- above and below the grid, adds a pipe | symbol to the beginning and end of each line of the grid, and adds a space between all characters in the grid.
Examples
Examples below show how your output should look.
Notice that after Enter cells: comes the user input.

Example 1:

Enter cells: O_OXXO_XX
---------
| O _ O |
| X X O |
| _ X X |
---------
Example 2:

Enter cells: OXO__X_OX
---------
| O X O |
| _ _ X |
| _ O X |
---------
Example 3:

Enter cells: _XO__X___
---------
| _ X O |
| _ _ X |
| _ _ _ |
---------

package tictactoe

fun main() {

    print("Enter cells: ")
    val input = readLine()!!.toString()
    val lineOfDashes = "---------"

    println(lineOfDashes)
    println("| ${input[0]} ${input[1]} ${input[2]} |")
    println("| ${input[3]} ${input[4]} ${input[5]} |")
    println("| ${input[6]} ${input[7]} ${input[8]} |")
    println(lineOfDashes)
}