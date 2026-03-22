# ChessBoard Project Context

## Project Structure
- Android app (:app module)
- Kotlin + Jetpack Compose
- Room database for persistence
- Training screen for chess openings

## Key Dependencies
- Kotlin 2.0.21
- AGP 8.13.2
- KSP 2.0.21-1.0.28 (fixed)
- Room 2.6.1
- Compose BOM 2024.09.00

## Key Files
- `app/src/main/java/com/example/chessboard/ui/TrainingScreen.kt` - Training UI
- `app/src/main/java/com/example/chessboard/ui/ChessBoard.kt` - Board component
- `app/src/main/java/com/example/chessboard/database/DataBaseProvider.kt` - DB setup
- `gradle/libs.versions.toml` - Version catalog

## Recent Changes
- Fixed KSP version: 2.0.21-1.0.27 → 2.0.21-1.0.28
