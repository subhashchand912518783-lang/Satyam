package com.example.data.model

data class Quote(
    val text: String,
    val author: String,
    val category: String = "Habits"
)

object MotivationalQuotes {
    val quotes = listOf(
        Quote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle"),
        Quote("Small daily improvements over time lead to stunning results.", "Robin Sharma"),
        Quote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
        Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        Quote("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln"),
        Quote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
        Quote("The secret of your future is hidden in your daily routine.", "Mike Murdock"),
        Quote("Don't break the chain. Keep the streak alive every single day.", "Jerry Seinfeld"),
        Quote("First we make our habits, then our habits make us.", "Charles C. Noble"),
        Quote("Consistency isn't about being perfect. It's about showing up even on hard days.", "Alex Morgan"),
        Quote("One day or day one. You decide.", "Paulo Coelho"),
        Quote("Action is the foundational key to all success.", "Pablo Picasso")
    )

    fun getRandomQuote(): Quote = quotes.random()
}
