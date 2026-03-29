---
description: Scaffold a new Room entity, DAO, and wire it into DatabaseProvider
---

Scaffold a new Room entity for the ChessBoard app.

The user wants to add an entity named: $ARGUMENTS

Steps:
1. Create `entity/<EntityName>Entity.kt` with `@Entity`, `@PrimaryKey`, and the fields the user described
2. Create `repository/<EntityName>Dao.kt` with appropriate `@Query`, `@Insert`, `@Update`, `@Delete` methods
3. Add the entity to the `@Database` entities list in `DatabaseProvider.kt`
4. Add the DAO to `DatabaseProvider`'s `AppDatabase` abstract class and expose it via `DatabaseProvider`'s public API methods
5. Increment the Room database version and add a migration (or `fallbackToDestructiveMigration` if the user says it's fine to wipe data)
6. Update the entity table in `CLAUDE.md`

Rules:
- All DB access from screens must go through `DatabaseProvider` — never expose the DAO directly to screens
- Use `Dispatchers.IO` for all DB suspend calls
- Keep field types simple — prefer primitives and String; use JSON strings for lists (see `gamesJson` pattern)
