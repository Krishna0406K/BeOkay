package com.vishal.manodost.data.model

data class Game(
    val id: Int,
    val name: String,
    val description: String,
    val url: String,
    val benefit: String,
    val icon: String
)

object GamesData {
    val games = listOf(
        Game(
            id = 1,
            name = "2048 Puzzle",
            description = "Addictive number puzzle game",
            url = "https://play2048.co/",
            benefit = "Improves Focus",
            icon = "🎯"
        ),
        Game(
            id = 2,
            name = "Sudoku",
            description = "Classic logic puzzle",
            url = "https://sudoku.com/",
            benefit = "Reduces Anxiety",
            icon = "🧩"
        ),
        Game(
            id = 3,
            name = "Solitaire",
            description = "Classic card game for relaxation",
            url = "https://www.solitr.com/",
            benefit = "Stress Relief",
            icon = "🃏"
        ),
        Game(
            id = 4,
            name = "Minesweeper",
            description = "Strategic puzzle game",
            url = "https://minesweeper.online/",
            benefit = "Improves Logic",
            icon = "💣"
        ),
        Game(
            id = 5,
            name = "Tetris",
            description = "Classic block-stacking game",
            url = "https://tetris.com/play-tetris",
            benefit = "Mood Boost",
            icon = "🎮"
        )
    )
}
