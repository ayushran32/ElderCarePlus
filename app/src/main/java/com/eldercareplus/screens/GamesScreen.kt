package com.eldercareplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.delay

data class Game(
    val id: String,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun GamesScreen(navController: NavController) {
    val games = listOf(
        Game("memory", "Memory Match", "Match pairs of cards", Color(0xFF4CAF50)),
        Game("puzzle", "Number Puzzle", "Slide to arrange numbers", Color(0xFF2196F3)),
        Game("math", "Quick Math", "Simple arithmetic", Color(0xFFFF9800))
    )
    
    var selectedGame by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Brain Games",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (selectedGame == null) {
            // Game selection
            Text(
                "Choose a game to keep your mind sharp!",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            games.forEach { game ->
                GameCard(game = game, onClick = { selectedGame = game.id })
                Spacer(Modifier.height(12.dp))
            }
        } else {
            // Show selected game
            when (selectedGame) {
                "memory" -> MemoryMatchGame(onBack = { selectedGame = null })
                "puzzle" -> NumberPuzzleGame(onBack = { selectedGame = null })
                "math" -> QuickMathGame(onBack = { selectedGame = null })
            }
        }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = game.color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(game.color, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Extension,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column {
                Text(
                    game.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    game.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MemoryMatchGame(onBack: () -> Unit) {
    val emojis = listOf("🌟", "🎈", "🌸", "🍎", "🎨", "🎵")
    val cards = (emojis + emojis).shuffled()
    var flippedIndices by remember { mutableStateOf(setOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(setOf<Int>()) }
    var canFlip by remember { mutableStateOf(true) }
    
    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            canFlip = false
            delay(1000)
            val indices = flippedIndices.toList()
            if (cards[indices[0]] == cards[indices[1]]) {
                matchedIndices = matchedIndices + flippedIndices
            }
            flippedIndices = emptySet()
            canFlip = true
        }
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Text("Back")
            }
            TextButton(onClick = {
                flippedIndices = emptySet()
                matchedIndices = emptySet()
            }) {
                Text("Restart")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards.indices.toList()) { index ->
                MemoryCard(
                    emoji = cards[index],
                    isFlipped = index in flippedIndices || index in matchedIndices,
                    onClick = {
                        if (canFlip && index !in flippedIndices && index !in matchedIndices && flippedIndices.size < 2) {
                            flippedIndices = flippedIndices + index
                        }
                    }
                )
            }
        }
        
        if (matchedIndices.size == cards.size) {
            Spacer(Modifier.height(20.dp))
            Text(
                "🎉 Congratulations! You matched all pairs!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MemoryCard(emoji: String, isFlipped: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isFlipped) emoji else "?",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NumberPuzzleGame(onBack: () -> Unit) {
    var tiles by remember { mutableStateOf(((1..8).toList() + listOf(0)).shuffled()) }
    
    fun canMove(index: Int): Boolean {
        val emptyIndex = tiles.indexOf(0)
        val row = index / 3
        val col = index % 3
        val emptyRow = emptyIndex / 3
        val emptyCol = emptyIndex % 3
        return (row == emptyRow && kotlin.math.abs(col - emptyCol) == 1) ||
               (col == emptyCol && kotlin.math.abs(row - emptyRow) == 1)
    }
    
    fun moveTile(index: Int) {
        if (canMove(index)) {
            val emptyIndex = tiles.indexOf(0)
            val newTiles = tiles.toMutableList()
            newTiles[emptyIndex] = tiles[index]
            newTiles[index] = 0
            tiles = newTiles
        }
    }
    
    val isSolved = tiles == (1..8).toList() + listOf(0)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Text("Back")
            }
            TextButton(onClick = { tiles = ((1..8).toList() + listOf(0)).shuffled() }) {
                Text("Shuffle")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(9) { index ->
                PuzzleTile(
                    number = tiles[index],
                    onClick = { moveTile(index) }
                )
            }
        }
        
        if (isSolved) {
            Spacer(Modifier.height(20.dp))
            Text(
                "🎉 Puzzle Solved!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PuzzleTile(number: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (number == 0) Color.Transparent else Color(0xFF2196F3)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (number == 0) 0.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (number != 0) {
                Text(
                    number.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun QuickMathGame(onBack: () -> Unit) {
    var num1 by remember { mutableStateOf((1..20).random()) }
    var num2 by remember { mutableStateOf((1..20).random()) }
    var score by remember { mutableStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    
    val correctAnswer = num1 + num2
    
    fun checkAnswer() {
        val answer = userAnswer.toIntOrNull()
        if (answer == correctAnswer) {
            score++
            feedback = "✓ Correct!"
            num1 = (1..20).random()
            num2 = (1..20).random()
            userAnswer = ""
        } else {
            feedback = "✗ Try again"
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Text("Back")
            }
            Text(
                "Score: $score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(Modifier.height(40.dp))
        
        Text(
            "$num1 + $num2 = ?",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF9800)
        )
        
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it.filter { char -> char.isDigit() } },
            label = { Text("Your Answer") },
            modifier = Modifier.fillMaxWidth(0.6f),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { checkAnswer() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("Check", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            feedback,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (feedback.startsWith("✓")) Color(0xFF4CAF50) else Color(0xFFE53935)
        )
    }
}
